package io.influxapp.model

import com.github.fsanaulla.chronicler.macros.annotations.{field, tag, timestamp}

case class Point(
                  @tag id1: String,
                  @field latitude: Double,
                  @field longitude: Double,
                  @timestamp timestamp: Long
                )
