import model.OsAddressItem
import model.Stop
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL
import java.sql.Timestamp
import java.util.*

class Address {
    fun getAddressName(lat: Double, lon: Double, configItem: ConfigItem, mysql: Mysql): Stop {
        val stops: ArrayList<Stop> = mysql.getStops()
        for (i in stops.indices) {
            if (Timeline.distance(lat, stops[i].lat, lon, stops[i].lon, 0.0, 0.0) < stops[i].radius) {
                return stops[i]
            }
        }

        //check db for stored addresses, fetch address if it doesn't exist
        val checkItem = checkDbAddress(mysql, lat, lon)
        if (checkItem != null){
            return Stop(checkItem.name, checkItem.lat, checkItem.lon, 0, false)
        }

        var name = ""
        var fetched: OsAddressItem? = null
        try {
            fetched = getAddress(lat, lon, configItem)
        } catch (e: Exception) {
            println("Failed to get address from Openstreetmap")
        }

        //add fetched address to db
        if (fetched != null){
            if (fetched.name != "") {
                name = fetched.name
                mysql.addOsAddress(fetched)
            }
        }

        return Stop(name, lat, lon, 0, false)
    }

    //get addresses from Mysql.kt and return if items exists within 40 meter radius
    fun checkDbAddress(mysql: Mysql, lat: Double, lon: Double): OsAddressItem?{
        val addresses = mysql.getOsAddressItems()
        addresses.forEach {
            if (Timeline.distance(lat, it.lat, lon, it.lon, 0.0, 0.0) < 41){
                return it
            }
        }
        return null
    }

    @Throws(Exception::class)
    fun getAddress(lat: Double, lon: Double, configItem: ConfigItem): OsAddressItem {
        val obj = URL(
            configItem.reverseGeocodeAddress.replace("LON", lon.toString() + "").replace("LAT", lat.toString() + "")
        )
        val con = obj.openConnection() as HttpURLConnection
        con.requestMethod = "GET"
        con.setRequestProperty("User-Agent", "owntracks-mysql-recorder")
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
            if (jsonObject.has("city")){
                city = jsonObject.getString("city")
            }
            if (jsonObject.has("country")){
                country = jsonObject.getString("country")
            }
        } else if (jsonObject.has("name")) {
            name = jsonObject.getString("name")
            if (jsonObject.has("city")){
                city = jsonObject.getString("city")
            }
            if (jsonObject.has("country")){
                country = jsonObject.getString("country")
            }
        }

        return OsAddressItem(0, name, lat, lon, Timestamp(Date().time), city, country)
    }

}