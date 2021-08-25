package model

import java.sql.Timestamp

data class Location(
    var locationId: Int,
    var startDate: Timestamp,
    var endDate: Timestamp,
    var stopId: Int,
)