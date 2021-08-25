package model

import java.sql.Timestamp

data class Route(
    var routeId: Int,
    var startDate: Timestamp,
    var endDate: Timestamp,
    var startLocationId: Int,
    var stopLocationId: Int,
    var distance: Double,
    var time: Double,
)
