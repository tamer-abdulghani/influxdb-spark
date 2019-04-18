package io.influxapp

import java.time.format.{DateTimeFormatter, DateTimeFormatterBuilder}

import com.paulgoldbaum.influxdbclient.{InfluxDB, Series}

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success, Try}

trait InfluxDbClient {

  val formatter: DateTimeFormatter = new DateTimeFormatterBuilder()
    .append(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    .appendPattern("[.SSSSSSSSS]")
    .appendOffset("+HH:mm", "Z")
    .toFormatter()

  def queryInfluxDb(host: String, port: Int, db: String, influxQuery: String): List[Series] = {
    val client = InfluxDB.connect(host, port)
    val database = client.selectDatabase(db)

    Try(Await.result(database.query(influxQuery), Duration.Inf)) match {
      case Success(value) => {
        client.close()
        value.series
      }
      case Failure(exception) => {
        client.close()
        println(exception.getMessage)
        List[Series]()
      }
    }
  }
}