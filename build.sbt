ThisBuild / version := "0.0.1-SNAPSHOT"
ThisBuild / scalaVersion := "2.11.8"
ThisBuild / organization := "io.influxdb"

val sparkSQL = "org.apache.spark" %% "spark-sql" % "2.3.1"
val sparkMLlib = "org.apache.spark" %% "spark-mllib" % "2.3.1"
val spark = Seq(sparkSQL, sparkMLlib)

val scalaTest = "org.scalatest" %% "scalatest" % "2.2.5"

lazy val data = (project in file("."))
  .settings(
    name := "influxdb-spark",
    libraryDependencies ++= spark,
    libraryDependencies += scalaTest % Test,
    libraryDependencies += "com.github.fsanaulla" %% "chronicler-spark-ds" % "0.3.1",
    libraryDependencies += "com.github.fsanaulla" %% "chronicler-macros" % "0.5.5",
    libraryDependencies += "com.paulgoldbaum" %% "scala-influxdb-client" % "0.6.1",
    libraryDependencies += "net.sf.geographiclib" % "GeographicLib-Java" % "1.49"
  )