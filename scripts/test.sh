#!/usr/bin/env bash

docker-compose -f scripts/docker-compose.yml up -d

docker exec -it influxdb influx -execute 'create database my_influx_db'

if [ "${1}" = "cluster" ]
then
    MODE="cluster"
    TOTAL_EXECUTOR_CORES=4
else
    MODE="standalone"
    TOTAL_EXECUTOR_CORES=2
fi

echo "===> Build sbt project"
# ToDo: Use docker to run following SBT command
sbt clean test package; mv target/scala-2.11/influxdb-spark*.jar target/influxdb-spark.jar

submit_spark_job() {
    echo "  => submit $1"
    docker run --rm \
       --network my-network \
       -w /opt/work \
       -v ${PWD}:/opt/work \
       -v ${HOME}/.m2:/root/.m2 \
       -v ${HOME}/.ivy2:/root/.ivy2 \
       --link spark-master \
       -p "4040:4040" \
       spark:2.3.1 \
       spark-submit \
           --class "$1" \
           --deploy-mode "client" \
           --master "spark://spark-master:7077" \
           --driver-memory "4g" \
           --executor-memory "2g" \
           --executor-cores "2" \
           --total-executor-cores "${TOTAL_EXECUTOR_CORES}" \
           --supervise \
           --packages com.github.fsanaulla:chronicler-spark-ds_2.11:0.2.9,com.github.fsanaulla:chronicler-macros_2.11:0.4.6,com.paulgoldbaum:scala-influxdb-client_2.11:0.6.1,net.sf.geographiclib:GeographicLib-Java:1.49 \
           /opt/work/target/influxdb-spark.jar \
           $2
}

echo "===> InfluxDB submit job"
submit_spark_job $1 $2