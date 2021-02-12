package model

import java.sql.Timestamp

data class LocationName(val id: Int, val startDate: Timestamp, val endDate: Timestamp, val osDataId: Int, val savedLocationId: Int, val stopName: String, val lat: Double, val lon: Double)
