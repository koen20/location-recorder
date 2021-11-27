package data

import model.LocationItem
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.Timestamp

interface LocationDataDao {
    fun getData(startTime: Long, endTime: Long = 7289648000000): ArrayList<LocationItem>
}

class LocationDataDaoImpl(private val conn: Connection) : LocationDataDao {

    override fun getData(startTime: Long, endTime: Long): ArrayList<LocationItem> {
        val data = ArrayList<LocationItem>()
        val ps: PreparedStatement = conn.prepareStatement("SELECT * FROM data WHERE date BETWEEN ? AND ?")
        ps.setTimestamp(1, Timestamp(startTime))
        ps.setTimestamp(2, Timestamp(endTime))

        ps.executeQuery().use { rs ->
            while (rs.next()) {
                data.add(LocationItem(rs.getTimestamp("date"), rs.getDouble("lat"), rs.getDouble("lon")))
            }
        }

        return data
    }
}