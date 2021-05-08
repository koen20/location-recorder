import data.LocationDaoImpl
import data.LocationDataDaoImpl
import data.StopDaoImpl
import java.sql.Connection
import java.sql.DriverManager

class Mysql(configItem: ConfigItem) {
    lateinit var conn: Connection
    lateinit var locationDataDao: LocationDataDaoImpl
    lateinit var locationDao: LocationDaoImpl
    lateinit var stopDao: StopDaoImpl

    init {
        try {
            conn =
                DriverManager.getConnection(configItem.mysqlServer, configItem.mysqlUsername, configItem.mysqlPassword)
            locationDataDao = LocationDataDaoImpl(conn)
            locationDao = LocationDaoImpl(conn)
            stopDao = StopDaoImpl(conn)
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