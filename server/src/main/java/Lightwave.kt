import model.ConfigItem
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class Lightwave(private val configItem: ConfigItem) {

    fun sendLocation(
        timestamp: Long,
        lat: Double,
        lon: Double,
        alt: Double?,
        acc: String,
        batt: String,
        tid: String
    ) {
        val url = configItem.lightwaveUrl ?: return
        val bucketId = configItem.lightwaveBucketId ?: return
        val userId = configItem.lightwaveUserId ?: return
        val apiKey = configItem.lightwaveApiKey ?: return

        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")

            val json = JSONObject()
            json.put("bucketId", bucketId)
            json.put("timestamp", sdf.format(Date(timestamp * 1000)))
            json.put("latitude", lat)
            json.put("longitude", lon)
            if (alt != null) {
                json.put("altitude", alt)
            }
            json.put("accuracy", acc.toDoubleOrNull() ?: 0.0)
            batt.toDoubleOrNull()?.let { json.put("battery", it) }
            json.put("userAgent", tid)

            val endpoint = URL("${url.trimEnd('/')}/api/bucket/$bucketId/location")
            val con = endpoint.openConnection() as HttpURLConnection
            con.requestMethod = "POST"
            con.setRequestProperty("Content-Type", "application/json")
            val basicAuth = Base64.getEncoder().encodeToString("$userId:$apiKey".toByteArray(Charsets.UTF_8))
            con.setRequestProperty("Authorization", "Basic $basicAuth")
            con.doOutput = true

            val os: OutputStream = con.outputStream
            os.write(json.toString().toByteArray(Charsets.UTF_8))
            os.flush()
            os.close()

            val responseCode = con.responseCode
            if (responseCode in 200..299) {
                println("Lightwave location sent: $responseCode")
            } else {
                var errorBody = ""
                try {
                    val reader = BufferedReader(InputStreamReader(con.errorStream))
                    var line: String?
                    val sb = StringBuffer()
                    while (reader.readLine().also { line = it } != null) {
                        sb.append(line)
                    }
                    reader.close()
                    errorBody = sb.toString()
                } catch (ignored: Exception) {
                }
                println("Lightwave location failed: $responseCode $errorBody")
            }
            con.disconnect()
        } catch (e: Exception) {
            println("Failed to send location to Lightwave")
            e.printStackTrace()
        }
    }
}
