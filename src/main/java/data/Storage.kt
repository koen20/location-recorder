import data.LocationDaoImpl
import data.LocationDataDaoImpl
import data.StopDaoImpl
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.util.*

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

        Timer().scheduleAtFixedRate(object : TimerTask() {
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
        }, 10000, 60000)
    }

    fun disconnect() {
        try {
            conn.close()
        } catch (e: Exception) {

        }
    }
}