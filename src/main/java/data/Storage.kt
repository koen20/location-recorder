import data.LocationDaoImpl
import data.LocationDataDaoImpl
import data.StopDaoImpl
import java.sql.*

class Mysql(configItem: ConfigItem) {
    lateinit var conn: Connection
    var locationDataDao = LocationDataDaoImpl(conn)
    var LocationDao = LocationDaoImpl(conn)
    var stopDao = StopDaoImpl(conn)

    init {
        try {
            conn =
                DriverManager.getConnection(configItem.mysqlServer, configItem.mysqlUsername, configItem.mysqlPassword)
        } catch (e: Exception) {
            println("Failed to connect to database $e")
        }
    }

    fun disconnect() {
        try {
            conn.close()
        } catch (e: Exception) {

        }
    }
}