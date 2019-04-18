package io.influxapp.writer

import com.github.fsanaulla.chronicler.core.model.{InfluxCredentials, InfluxWriter}
import com.github.fsanaulla.chronicler.macros.Influx
import com.github.fsanaulla.chronicler.spark.ds._
import com.github.fsanaulla.chronicler.urlhttp.shared.InfluxConfig
import io.influxapp.BasicApp
import io.influxapp.model.Point
import org.apache.spark.sql.{Dataset, SparkSession}

object InfluxWriter extends BasicApp {

  def main(args: Array[String]) {
    // Only for Windows OS
    System.setProperty("hadoop.home.dir", "C:\\winutils\\")

    val spark: SparkSession = SparkSession
      .builder()
      .config("spark.master", "local")
      .appName("Influxdb")
      .getOrCreate()
    spark.sparkContext.setLogLevel("ERROR")

    var df: Dataset[Point] = null
    /*
    // Remove if you are testing with test.sh
    if (args != null && args(0) == "ais") {
      df = readAisCSV(sparkSession)
    }
    */
    df = readAisCSV(spark)

    df.show(false)

    implicit val influxConf: InfluxConfig = InfluxConfig("influxdb", 8086, Some(InfluxCredentials("admin", "password")))
    implicit val wr: InfluxWriter[Point] = Influx.writer[Point]

    // ToDO: Fix writing to influx
    df.saveToInfluxDB(influx_db, influx_ms)

  }

  def readAisCSV(spark: SparkSession): Dataset[Point] = {
    spark
      .read
      .option("inferSchema", "true")
      .option("header", "true")
      .csv("resources/ais-sample-data*")
      .transform(withAisDataFrame(spark))
  }
}