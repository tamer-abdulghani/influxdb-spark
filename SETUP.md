
## Setup documentation

### Dependencies
```
    libraryDependencies += "com.github.fsanaulla" %% "chronicler-spark-ds" % "0.2.8",
    libraryDependencies += "com.github.fsanaulla" %% "chronicler-macros" % "0.4.6"
```
and in spark submit command:

```
    --packages com.github.fsanaulla:chronicler-spark-ds_2.11:0.2.8,com.github.fsanaulla:chronicler-macros_2.11:0.4.6 \
``` 
   
### Modules 

Up to the moment, the connector is working this way: 
    
1. define the entity in **separate file**:
    ```
    case class Point(
                       @tag id1: String,
                       @tag id2: String,
                       @tag id3: String,
                       @field latitude: Double,
                       @field longitude: Double,
                       @timestampEpoch timestamp: Long
                     )
    ``` 

2. Read dataframe read with spark and cast to your entity

3. Save to influxdb

   ```
     implicit val influxConf: InfluxConfig = InfluxConfig("influxdb", 8086, Some(InfluxCredentials("admin", "password")), gzipped = false)
     implicit val wr: InfluxWriter[Point] = Influx.writer[Point]
     df.saveToInfluxDB("my_influx_db", "my_ms")
    ```
    
    Or manually handling the operation: 

    This is workaround way to save to influxdb where we loop all partitions and all rows one by one and write to influxdb which is taking huge time.

    ```
    implicit val influxConf: InfluxConfig = InfluxConfig("influxdb", 8086, Some(InfluxCredentials("admin", "password")), gzipped = false)
    implicit val wr: InfluxWriter[Point] = Influx.writer[Point]
    
    df.rdd.foreachPartition { partition =>
        partition.foreach(row => {
            val io = InfluxIO(influxConf)
            val meas = io.measurement[Point]("my_influx_db", "my_ms")
            meas.write(row)
                io.close()
            })
        }
    ```
        
4. Read from influxdb
Using the connector `com.paulgoldbaum.influxdbclient` we connect to db using the following instruction:

    ```
      val client = InfluxDB.connect('host', 8086)
      val database = client.selectDatabase('my_influx_db')
    
      Try(Await.result(database.query('select * from my_ms'), Duration.Inf)) match {
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
    ```

