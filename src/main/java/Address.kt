import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.ArrayList

class Address {

    fun getAddressName(lat: Double, lon: Double, configItem: ConfigItem, mysql: Mysql): Stop {
        val stops: ArrayList<Stop> = mysql.getStops()
        for (i in stops.indices) {
            if (Timeline.distance(lat, stops[i].lat, lon, stops[i].lon, 0.0, 0.0) < stops[i].radius) {
                return stops[i]
            }
        }
        var address = JSONObject()
        try {
            address = getAddress(lat, lon, configItem)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        var name = ""
        if (address.has("street")) {
            name = address.getString("street")
            if (address.has("housenumber")) {
                name = name + " " + address.getString("housenumber")
            }
        } else if (address.has("name")) {
            name = address.getString("name")
        }
        return Stop(name, lat, lon, 0, false)
    }

    @Throws(IOException::class)
    fun getAddress(lat: Double, lon: Double, configItem: ConfigItem): JSONObject {
        //URL obj = new URL("http://photon.komoot.de/reverse?lon=" + lon + "&lat=" + lat);
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
        println(JSONObject(response.toString()).getJSONArray("features").getJSONObject(0).toString())
        return jsonObject
    }

}