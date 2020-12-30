import model.OsAddressItem
import model.Stop
import java.sql.*
import java.util.*
import kotlin.collections.ArrayList

class Mysql(configItem: ConfigItem) {
    companion object {
        lateinit var conn: Connection
        var stops: ArrayList<Stop> = ArrayList()
        var osAddressItems: ArrayList<OsAddressItem> = ArrayList()
        lateinit var configItem: ConfigItem

        fun getStopsDb(): ArrayList<Stop> {
            val items: ArrayList<Stop> = ArrayList()
            try {
                conn.createStatement().use { stmt ->
                    stmt.executeQuery("SELECT * FROM stops").use { rs ->
                        while (rs.next()) {
                            val stop = Stop(
                                rs.getString("name"),
                                rs.getDouble("lat"),
                                rs.getDouble("lon"),
                                rs.getInt("radius"),
                                true
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

        fun getOsAddressDb(): ArrayList<OsAddressItem> {
            val items: ArrayList<OsAddressItem> = ArrayList()
            try {
                conn.createStatement().use { stmt ->
                    stmt.executeQuery("SELECT * FROM osData").use { rs ->
                        while (rs.next()) {
                            val addressItem = OsAddressItem(
                                rs.getInt("id"),
                                rs.getString("name"),
                                rs.getDouble("lat"),
                                rs.getDouble("lon"),
                                rs.getTimestamp("dateFetched")
                            )
                            items.add(addressItem)
                        }
                    }
                }
            } catch (e: SQLException) {
                e.printStackTrace()
            }

            return items
        }
    }

    init {
        conn = DriverManager.getConnection(configItem.mysqlServer, configItem.mysqlUsername, configItem.mysqlPassword)
        val updateTimer = Timer()
        updateTimer.scheduleAtFixedRate(checkMysqlConnection(), 2000, 60000)
        val updateTimerStops = Timer()
        updateTimerStops.scheduleAtFixedRate(updateTimerStops(), 21600000, 21600000)
        stops = getStopsDb()
        osAddressItems = getOsAddressDb()
    }

    fun disconnect() {
        conn.close()
    }

    fun addStop(stop: Stop): Boolean {
        var added = false
        try {
            val insert = "INSERT INTO stops VALUES(NULL, ?, ?, ?, ?)"
            conn.prepareStatement(insert).use { ps ->
                ps.setString(1, stop.name)
                ps.setDouble(2, stop.lat)
                ps.setDouble(3, stop.lon)
                ps.setInt(4, stop.radius)
                ps.execute()
            }
            stops.add(stop)
            added = true
        } catch (exception: SQLException) {
            exception.printStackTrace()
        }
        return added
    }

    fun addOsAddress(item: OsAddressItem): Boolean{
        var added = false
        try {
            val insert = "INSERT INTO osData VALUES(NULL, ?, ?, ?, ?)"
            conn.prepareStatement(insert).use { ps ->
                ps.setString(1, item.name)
                ps.setDouble(2, item.lat)
                ps.setDouble(3, item.lon)
                ps.setTimestamp(4, item.dateFetched)
                ps.execute()
            }
            osAddressItems.add(item)
            added = true
        } catch (exception: SQLException) {
            exception.printStackTrace()
        }
        return added
    }

    fun getData(startTime: Long, endTime: Long): ArrayList<LocationItem> {
        val data = ArrayList<LocationItem>()
        conn.createStatement().use { stmt ->
            stmt.executeQuery(
                "SELECT * FROM data WHERE date BETWEEN '${Mqtt.getMysqlDateString(startTime)}' AND '${
                    Mqtt.getMysqlDateString(
                        endTime
                    )
                }'"
            ).use { rs ->
                while (rs.next()) {
                    data.add(LocationItem(rs.getTimestamp("date"), rs.getDouble("lat"), rs.getDouble("lon")))
                }
            }
        }

        return data
    }

    fun getStops(): ArrayList<Stop> {
        return stops
    }

    fun getOsAddressItems(): ArrayList<OsAddressItem>{
        return osAddressItems
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
            osAddressItems = getOsAddressDb()
        }
    }
}