import spark.Request
import spark.Response
import java.lang.Exception
import java.sql.SQLException

class PhoneTrack {

    fun addData(request: Request, response: Response): String {
        try {
            val tst = Mqtt.getMysqlDateString(request.queryParams("timestamp").toLong())
            val stmt = Mysql.conn.createStatement()
            val alt = request.queryParams("alt")
            if (alt == "") {
                stmt.executeUpdate(
                    "INSERT INTO data VALUES (NULL, '" + tst + "', '"
                            + request.queryParams("lat") + "', '" + request.queryParams("lon") + "'" +
                            ", NULL, '" + request.queryParams("acc")
                            + "',  '" + request.queryParams("batt") + "', '" + request.queryParams("tid") + "')"
                )
            } else {
                stmt.executeUpdate(
                    ("INSERT INTO data VALUES (NULL, '" + tst + "', '"
                            + request.queryParams("lat") + "', '" + request.queryParams("lon") + "'" +
                            ", '" + alt + "', '" + request.queryParams("acc")
                            + "',  '" + request.queryParams("batt") + "', '" + request.queryParams("tid") + "')")
                )
            }
            stmt.close()
        } catch (e: Exception) {
            e.printStackTrace()
            try {
                println(Mysql.conn.isValid(3000))
            } catch (ex: SQLException) {
                ex.printStackTrace()
            }
            response.status(500)
        }
        return ""
    }
}