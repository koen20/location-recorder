package data

import model.LocationItem
import java.sql.Connection
import java.sql.PreparedStatement

interface LocationDataDao {
    fun getData(startTime: Long, endTime: Long = 7289648397): ArrayList<LocationItem>
}

class LocationDataDaoImpl(private val conn: Connection) : LocationDataDao {

    override fun getData(startTime: Long, endTime: Long): ArrayList<LocationItem> {
        val data = ArrayList<LocationItem>()
        val ps: PreparedStatement = conn.prepareStatement("SELECT * FROM data WHERE date BETWEEN '?' AND '?'")
        ps.setString(1, Mqtt.getMysqlDateString(startTime))
        ps.setString(2, Mqtt.getMysqlDateString(endTime))

        ps.executeQuery().use { rs ->
            while (rs.next()) {
                data.add(LocationItem(rs.getTimestamp("date"), rs.getDouble("lat"), rs.getDouble("lon")))
            }
        }

        return data
    }
}