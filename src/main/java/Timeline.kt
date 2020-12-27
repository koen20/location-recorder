import org.json.JSONArray
import org.json.JSONObject
import spark.Request
import spark.Response
import java.math.BigDecimal
import java.math.RoundingMode
import java.sql.SQLException
import java.sql.Timestamp
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class Timeline(val configItem: ConfigItem, val mysql: Mysql) {
    fun getDataDate(request: Request, response: Response): String {
        var res = ""
        val dt: Date

        val sdf = SimpleDateFormat("yyyy-MM-dd")

        try {
            dt = sdf.parse(request.queryParams("date"))
            res = getData(dt.time / 1000, (dt.time + 86400000) / 1000).toString()
        } catch (e: ParseException) {
            println("asodf")
            e.printStackTrace()
        }
        println(res)
        return res
    }

    fun getData(start: Long, end: Long): JSONObject{
        val jsonArray = JSONArray()
        val jsonArrayAll = JSONArray()
        var jsonArrayRoutes = JSONArray()
        val jsonObjectRes = JSONObject()

        try {
            val stmt = Mysql.conn.createStatement()
            val rs = stmt.executeQuery(
                """SELECT * FROM data WHERE date BETWEEN '${Mqtt.getMysqlDateString(start)}' AND '${
                    Mqtt.getMysqlDateString(end)
                }'"""
            )
            var lat = 0.0
            var lon = 0.0
            var time: Long = 0
            var multiple = true
            var added = true
            var count = 0
            var latTot = 0.0
            var lonTot = 0.0
            var firstTime: Timestamp? = null
            var endTime: Timestamp? = null
            while (rs.next()) {
                if (distance(lat, rs.getDouble("lat"), lon, rs.getDouble("lon"), 0.0, 0.0) >= configItem.radiusLocation) {
                    time = rs.getTimestamp("date").time
                    multiple = false
                    if (!added) {
                        jsonArray.put(add(latTot, lonTot, count, firstTime!!, endTime!!))
                        added = true
                    }
                    count = 0
                    latTot = 0.0
                    lonTot = 0.0
                    firstTime = rs.getTimestamp("date")
                    lat = rs.getDouble("lat")
                    lon = rs.getDouble("lon")
                }
                if (distance(lat, rs.getDouble("lat"), lon, rs.getDouble("lon"), 0.0, 0.0) < configItem.radiusLocation) {
                    if (rs.getTimestamp("date").time - time > 420000 && !multiple) {
                        multiple = true
                        added = false
                    }
                    count += 1
                    latTot += rs.getDouble("lat")
                    lonTot += rs.getDouble("lon")
                    endTime = rs.getTimestamp("date")
                }
                jsonArrayAll.put(JSONObject().apply {
                    put("date", rs.getTimestamp("date").time)
                    put("lat", rs.getDouble("lat"))
                    put("lon", rs.getDouble("lon"))
                })
            }
            if (!added) {
                jsonArray.put(add(latTot, lonTot, count, firstTime!!, endTime!!))
            }
            rs.close()
            stmt.close()
            jsonArrayRoutes = Routes().getRouteFromStop(jsonArray, jsonArrayAll)
        } catch (exception: SQLException) {
            exception.printStackTrace()
        }

        jsonObjectRes.put("routes", jsonArrayRoutes)
        jsonObjectRes.put("stops", jsonArray)

        return jsonObjectRes
    }

    fun add(latTot: Double, lonTot: Double, count: Int, firstTime: Timestamp, endTime: Timestamp): JSONObject {
        val stop = Address().getAddressName(
            round(latTot / count, 5),
            round(lonTot / count, 5),
            configItem,
            mysql
        )

        return JSONObject().apply {
            put("start", firstTime.time)
            put("end", endTime.time)
            put("location", stop.name)
            put("locationUserAdded", stop.isUserAdded)
            put("lat", round(latTot / count, 5))
            put("lon", round(lonTot / count, 5))
        }
    }

    companion object {
        fun distance(
            lat1: Double, lat2: Double, lon1: Double,
            lon2: Double, el1: Double, el2: Double
        ): Double {
            val R = 6371 // Radius of the earth
            val latDistance = Math.toRadians(lat2 - lat1)
            val lonDistance = Math.toRadians(lon2 - lon1)
            val a = (Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                    + (Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                    * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2)))
            val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
            var distance = R * c * 1000 // convert to meters
            val height = el1 - el2
            distance = Math.pow(distance, 2.0) + Math.pow(height, 2.0)
            return Math.sqrt(distance)
        }

        fun round(value: Double, places: Int): Double {
            require(places >= 0)
            var bd = BigDecimal(value)
            bd = bd.setScale(places, RoundingMode.HALF_UP)
            return bd.toDouble()
        }
    }
}