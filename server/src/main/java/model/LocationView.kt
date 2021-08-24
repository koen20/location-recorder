package model

import java.sql.Timestamp

data class LocationView(
    var locationId: Int,
    var start: Timestamp,
    var end: Timestamp,
    var lat: Double,
    var lon: Double,
    var stopId: Int,
    var location: String?
)
