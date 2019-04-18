package io.influxapp.model

import com.github.fsanaulla.chronicler.macros.annotations.{field, tag, timestamp}

case class PointTransformed(
                             @tag id1: String,
                             @field latitude: Double,
                             @field longitude: Double,
                             @field distance: Double,
                             @timestamp timestamp: Long
                           )
