import model.ConfigItem
import model.Stop
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.sql.Timestamp
import java.util.*

class Address {
    fun getAddressName(lat: Double, lon: Double, configItem: ConfigItem, mysql: Mysql, disableInsert: Boolean): Stop {
        val stops: ArrayList<Stop> = mysql.stopDao.getStops()
        for (i in stops.indices) {
            if (Timeline.distance(lat, stops[i].lat, lon, stops[i].lon, 0.0, 0.0) < configItem.radiusLocation) {
                return stops[i]
            }
        }

        var fetched = Stop(0, "", lat, lon, "", "", null, null)
        if (!disableInsert) {
            try {
                fetched = getAddress(lat, lon, configItem)
            } catch (e: Exception) {
                println("Failed to get address from Openstreetmap")
                fetched = Stop(0, null, lat, lon, "", "", null, Timestamp(Date().time))
            }

            //add fetched address to db

            mysql.stopDao.addStop(fetched)
            fetched = checkDbAddress(mysql, lat, lon, configItem.radiusLocation)!!
            return fetched
        }
        println("Insert disabled ${fetched.name}")

        return fetched
    }

    //get addresses from Mysql.kt and return if items exists within radius
    fun checkDbAddress(mysql: Mysql, lat: Double, lon: Double, radius: Int): Stop? {
        val addresses = mysql.stopDao.getStops()
        addresses.forEach {
            if (Timeline.distance(lat, it.lat, lon, it.lon, 0.0, 0.0) < radius) {
                return it
            }
        }
        return null
    }

    @Throws(Exception::class)
    fun getAddress(lat: Double, lon: Double, configItem: ConfigItem): Stop {
        val obj = URL(
            configItem.reverseGeocodeAddress.replace("LON", lon.toString() + "").replace("LAT", lat.toString() + "")
        )
        val con = obj.openConnection() as HttpURLConnection
        con.requestMethod = "GET"
        con.setRequestProperty("User-Agent", "location-recorder-recorder")
        val `in` = BufferedReader(
            InputStreamReader(con.inputStream)
        )
        var inputLine: String?
        val response = StringBuffer()
        while (`in`.readLine().also { inputLine = it } != null) {
            response.append(inputLine)
        }
        `in`.close()
        val jsonObject =
            JSONObject(response.toString()).getJSONArray("features").getJSONObject(0).getJSONObject("properties")

        var name = ""
        var city = ""
        var country = ""
        if (jsonObject.has("street")) {
            name = jsonObject.getString("street")
            if (jsonObject.has("housenumber")) {
                name = name + " " + jsonObject.getString("housenumber")
            }
            if (jsonObject.has("city")) {
                city = jsonObject.getString("city")
            }
            if (jsonObject.has("country")) {
                country = jsonObject.getString("country")
            }
        } else if (jsonObject.has("name")) {
            name = jsonObject.getString("name")
            if (jsonObject.has("city")) {
                city = jsonObject.getString("city")
            }
            if (jsonObject.has("country")) {
                country = jsonObject.getString("country")
            }
        }

        return Stop(0, name, lat, lon, city, country, null, Timestamp(Date().time))
    }

}