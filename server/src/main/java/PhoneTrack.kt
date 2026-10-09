import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import model.ConfigItem
import java.sql.PreparedStatement
import java.sql.SQLException
import java.sql.Types
import java.util.*

fun Route.addPhoneTrackLocation(mysql: Mysql, configItem: ConfigItem) {
    route("/add") {
        get {
            try {
                val timestamp = call.parameters["timestamp"]!!.toLong()
                val date = Date(timestamp * 1000)
                val alt = call.parameters["alt"]!!

                val ps: PreparedStatement =
                    mysql.conn.prepareStatement("INSERT INTO data VALUES (NULL, ?, ?, ?, ?, ?, ?, ?)")
                ps.setTimestamp(1, java.sql.Timestamp(date.time))
                ps.setDouble(2, call.parameters["lat"]!!.toDouble())
                ps.setDouble(3, call.parameters["lon"]!!.toDouble())
                if (alt == "") {
                    ps.setNull(4, Types.DOUBLE)
                } else {
                    ps.setDouble(4, alt.toDouble())
                }

                ps.setString(5, call.parameters["acc"]!!)
                ps.setString(6, call.parameters["batt"]!!)
                ps.setString(7, call.parameters["tid"]!!)
                ps.execute()

                Lightwave(configItem).sendLocation(
                    timestamp = timestamp,
                    lat = call.parameters["lat"]!!.toDouble(),
                    lon = call.parameters["lon"]!!.toDouble(),
                    alt = if (alt == "") null else alt.toDouble(),
                    acc = call.parameters["acc"]!!,
                    batt = call.parameters["batt"]!!,
                    tid = call.parameters["tid"]!!
                )

                call.respondText("added")
            } catch (e: Exception) {
                e.printStackTrace()
                try {
                    println(mysql.conn.isValid(3000))
                } catch (ex: SQLException) {
                    ex.printStackTrace()
                }
                call.respondText("Insert failed", status = HttpStatusCode.InternalServerError)
            }
        }
    }
}