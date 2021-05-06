package data

import model.Stop
import java.sql.Connection
import java.sql.SQLException

interface StopDao {
    fun addStop(stop: Stop): Boolean
    fun updateStop(stop: Stop): Boolean
    fun getStops(): ArrayList<Stop>
    fun deleteStop(stopId: Int): Boolean
}

class StopDaoImpl(private val conn: Connection) : StopDao {

    override fun addStop(stop: Stop): Boolean {
        var added = false
        try {
            val insert = "INSERT INTO stop VALUES(NULL, ?, ?, ?, ?, ?, ?, ?)"
            conn.prepareStatement(insert).use { ps ->
                ps.setString(1, stop.name)
                ps.setDouble(2, stop.lat)
                ps.setDouble(3, stop.lon)
                ps.setString(4, stop.city)
                ps.setString(5, stop.country)
                ps.setString(6, stop.customName)
                ps.setTimestamp(7, stop.dateFetched)
                ps.execute()
            }
            added = true
        } catch (exception: SQLException) {
            exception.printStackTrace()
        }
        return added
    }

    override fun updateStop(stop: Stop): Boolean {
        var added = false
        try {
            conn.prepareStatement("UPDATE stop SET name = ?, lat = ?, lon = ?, city = ?, country = ?, customName = ?, dateFetched = ? WHERE stopId = ?")
                .use { ps ->
                    ps.setString(1, stop.name)
                    ps.setDouble(2, stop.lat)
                    ps.setDouble(3, stop.lon)
                    ps.setString(4, stop.city)
                    ps.setString(5, stop.country)
                    ps.setString(6, stop.customName)
                    ps.setTimestamp(7, stop.dateFetched)
                    ps.execute()
                }
            added = true
        } catch (exception: SQLException) {
            exception.printStackTrace()
        }
        return added
    }

    override fun getStops(): ArrayList<Stop> {
        val items: ArrayList<Stop> = ArrayList()
        try {
            conn.createStatement().use { stmt ->
                stmt.executeQuery("SELECT * FROM stop").use { rs ->
                    while (rs.next()) {
                        val stop = Stop(
                            rs.getInt("stopId"),
                            rs.getString("name"),
                            rs.getDouble("lat"),
                            rs.getDouble("lon"),
                            rs.getString("city"),
                            rs.getString("country"),
                            rs.getString("customName"),
                            rs.getTimestamp("dateFetched")
                        )
                        items.add(stop)
                    }
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        return items
    }

    override fun deleteStop(stopId: Int): Boolean {
        TODO("Not yet implemented")
    }
}