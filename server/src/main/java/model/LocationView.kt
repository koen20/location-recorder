package model

import java.sql.Timestamp

data class LocationView(
    var locationId: Int,
    var start: Long,
    var end: Long,
    var lat: Double,
    var lon: Double,
    var stopId: Int,
    var location: String?
)
