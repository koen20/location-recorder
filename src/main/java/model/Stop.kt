package model

import java.sql.Timestamp

data class Stop(
    var stopId: Int,
    var name: String?,
    var lat: Double,
    var lon: Double,
    var city: String,
    var country: String,
    var customName: String?,
    var dateFetched: Timestamp?
)