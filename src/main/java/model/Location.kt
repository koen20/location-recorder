package model

import java.sql.Timestamp

class Location (val id: Int, val startDate: Timestamp, val endDate: Timestamp, val osDataId: Int, val savedLocationId: Int)