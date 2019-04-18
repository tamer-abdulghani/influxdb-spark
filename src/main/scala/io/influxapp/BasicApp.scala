package io.influxapp

import java.time.format.DateTimeFormatter
import java.time.{LocalDateTime, ZoneOffset}
import java.util

import com.paulgoldbaum.influxdbclient.Series
import io.influxapp.model.Point
import io.influxapp.model.PointTransformed
import io.influxapp.reader.InfluxReader.formatter
import net.sf.geographiclib.{Geodesic, GeodesicMask}
import org.apache.spark.sql.api.java.UDF4
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}

trait BasicApp {

  val influx_db = "my_influx_db"
  val influx_ms = "ms"
  val influx_ms_transformed = "ms_transformed"

  def withAisDataFrame(spark: SparkSession)(df: DataFrame): Dataset[Point] = {
    import spark.implicits._
    df.select(
      col("mmsi").cast(StringType) as "id1",
      col("latitude").cast(DoubleType),
      col("longitude").cast(DoubleType),
      col("timestamp").cast(LongType).multiply(1000000000) as "timestamp"
    ).as[Point]
  }

  def mapToArray(series: List[Series], influxDbFormatter: DateTimeFormatter): Array[Point] = {
    var list: util.ArrayList[Point] = new util.ArrayList[Point]
    if (series.nonEmpty)
      series.foreach(s => {
        s.records.map(x => list.add(Point(
          s.tags.apply(0).toString,
          x("latitude").toString.toDouble,
          x("longitude").toString.toDouble,
          LocalDateTime.parse(x("time").toString, formatter).toEpochSecond(ZoneOffset.UTC)
        )))
      })
    else
      new Array[Point](0)

    list.toArray(Array.ofDim[Point](list.size))
  }

  def withDistanceTransformer(spark: SparkSession)(df: Dataset[Point]): Dataset[PointTransformed] = {
    import spark.implicits._

    val window = Window
      .partitionBy("id1")
      .rowsBetween(-1, 0)
    spark.udf.register("distance", distance, DataTypes.DoubleType)

    val result = df
      .withColumn(
        "distance",
        callUDF(
          "distance",
          first("latitude").over(window),
          first("longitude").over(window),
          last("latitude").over(window),
          last("longitude").over(window)
        )
      )
    result.as[PointTransformed]
  }

  private val distance = new UDF4[Double, Double, Double, Double, Double]() {
    @throws[Exception]
    override def call(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double = {
      val g = Geodesic.WGS84.Inverse(lat1, lon1, lat2, lon2, GeodesicMask.DISTANCE)
      val distance = g.s12
      distance
    }
  }
}
