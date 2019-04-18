package io.influxapp.reader

import com.github.fsanaulla.chronicler.core.model.{InfluxCredentials, InfluxWriter}
import com.github.fsanaulla.chronicler.macros.Influx
import com.github.fsanaulla.chronicler.spark.ds._
import com.github.fsanaulla.chronicler.urlhttp.shared.InfluxConfig
import io.influxapp.model.Point
import io.influxapp.model.PointTransformed
import io.influxapp.{BasicApp, InfluxDbClient}
import org.apache.spark.sql.SparkSession

object InfluxReader extends BasicApp with InfluxDbClient {

  def main(args: Array[String]): Unit = {
    // Only for Windows OS
    System.setProperty("hadoop.home.dir", "C:\\winutils\\")

    val spark: SparkSession = SparkSession
      .builder()
      .config("spark.master", "local")
      .appName("Influxdb")
      .getOrCreate()
    spark.sparkContext.setLogLevel("ERROR")


    //val startDate = "2017-09-01T18:00:00Z"
    //val endDate = "2017-09-01T22:00:00Z"

    val startDate = "2019-02-04T18:00:00Z"
    val endDate = "2019-02-04T22:00:00Z"
    val result = queryInfluxDb("influxdb", 8086, influx_db, "SELECT * FROM " + influx_ms + " where time >= '" + startDate + "' and time <= '" + endDate + "' group by id1, id2")

    val array = mapToArray(result, formatter)

    import spark.implicits._
    var df = spark
      .createDataFrame(array)
      .toDF()
      .as[Point]
      .transform(withDistanceTransformer(spark))

    implicit val influxConf: InfluxConfig = InfluxConfig("influxdb", 8086, Some(InfluxCredentials("admin", "password")))
    implicit val wr: InfluxWriter[PointTransformed] = Influx.writer[PointTransformed]
    df.saveToInfluxDB(influx_db, influx_ms_transformed)
  }
}
