# Run the cluster 

* Navigate to scripts directory 
* Run `docker-compose -f docker-compose.yml up -d`


# Influx Spark Integration

* Using this library [chronicler-spark](https://github.com/fsanaulla/chronicler-spark)
* Include **transformed** AIS csv files under this path: `resources/ais/` and update csv file name [here](/src/main/scala/io/influxapp/writer/InfluxWriter.scala#L36)
* Include AIS csv files under this path: `resources/ais/` and update csv file name [here](/src/main/scala/io/influxapp/writer/InfluxWriter.scala#L47)

## Test for AIS context

To test the application with AIS context:

- Writer: to read data from `csv` and write them to `influxdb` for `ais` context:
```
./scripts/test.sh io.influxapp.writer.InfluxWriter ais
```

- Reader: to read data again from `influxdb` and (do some transformations and) write them back to `influxdb` for `ais` context:
```
./scripts/test.sh io.influxapp.reader.InfluxReader ais
```

For more details about using the library, you can read [setup documentation](/SETUP.md)