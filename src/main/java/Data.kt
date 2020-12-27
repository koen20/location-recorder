import org.json.JSONArray
import org.json.JSONObject
import spark.Request
import spark.Response

class Data {
    fun getData(request: Request, response: Response): String {
        val startTime = request.queryParams("startTime").toLong()
        val endTime = request.queryParams("endTime").toLong()
        return getDataTime(startTime, endTime).toString()
    }

    companion object {
        fun getDataTime(startTime: Long, endTime: Long): JSONArray {
            val jsonArray = JSONArray()
            Mysql.conn.createStatement().use { stmt ->
                stmt.executeQuery("SELECT * FROM data WHERE date BETWEEN '${Mqtt.getMysqlDateString(startTime)}' AND '${Mqtt.getMysqlDateString(endTime)}'").use { rs ->
                    while (rs.next()) {
                        jsonArray.put(JSONObject().apply {
                            put("date", rs.getTimestamp("date"))
                            put("lat", rs.getDouble("lat"))
                            put("lon", rs.getDouble("lon"))
                        })
                    }
                }
            }

            return jsonArray
        }
    }

}
