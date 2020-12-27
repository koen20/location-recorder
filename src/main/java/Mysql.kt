import java.sql.*
import java.util.*
import kotlin.collections.ArrayList

class Mysql(configItem: ConfigItem) {
    companion object {
        lateinit var conn: Connection
        var stops: ArrayList<Stop> = ArrayList()
        lateinit var configItem: ConfigItem

        fun getStopsDb(): ArrayList<Stop> {
            stops.clear()
            try {
                val stmt = conn.createStatement()
                val rs = stmt.executeQuery("SELECT * FROM stops")
                while (rs.next()) {
                    val stop =
                        Stop(rs.getString("name"), rs.getDouble("lat"), rs.getDouble("lon"), rs.getInt("radius"), true)
                    stops.add(stop)
                }
            } catch (e: SQLException) {
                e.printStackTrace()
            }
            return stops
        }
    }

    init {
        conn = DriverManager.getConnection(configItem.mysqlServer, configItem.mysqlUsername, configItem.mysqlPassword)
        val updateTimer = Timer()
        updateTimer.scheduleAtFixedRate(checkMysqlConnection(), 2000, 60000)
        val updateTimerStops = Timer()
        updateTimerStops.scheduleAtFixedRate(updateTimerStops(), 21600000, 21600000)
        stops = getStopsDb()
    }


    fun AddStop(stop: Stop): Boolean {
        var added = false
        try {
            val insert = "INSERT INTO stops VALUES(NULL, ?, ?, ?, ?)"
            val ps = conn.prepareStatement(insert)
            ps.setString(1, stop.name)
            ps.setDouble(2, stop.lat)
            ps.setDouble(3, stop.lon)
            ps.setInt(4, stop.radius)
            ps.execute()
            ps.close()
            added = true
        } catch (exception: SQLException) {
            exception.printStackTrace()
        }
        return added
    }

    fun updateStops(){
        stops = getStopsDb()
    }

    fun getStops():ArrayList<Stop>{
        return stops
    }

    private class checkMysqlConnection : TimerTask() {
        override fun run() {
            try {
                if (!conn.isValid(3000)) {
                    conn.close()
                    conn = DriverManager.getConnection(
                        configItem.mysqlServer,
                        configItem.mysqlUsername, configItem.mysqlPassword
                    )
                }
            } catch (e: SQLException) {
                e.printStackTrace()
            }
        }
    }

    private class updateTimerStops : TimerTask() {
        override fun run() {
            stops = getStopsDb()
        }
    }
}