import com.google.gson.Gson
import model.LocationItem
import org.junit.jupiter.api.Test
import java.sql.Timestamp

class TestTimeline {
    val config = "{\"mysqlServer\": \"jdbc:mariadb://127.0.0.1:3306/location\",\n" +
            "  \"mysqlUsername\":  \"\",\n" +
            "  \"mysqlPassword\":  \"\",\n" +
            "  \"owntracksTopic\":  [\"owntracks/koen/oneplus6\"],\n" +
            "  \"mqttServer\":  \"ssl://mqtt.example.com:1883\",\n" +
            "  \"mqttUsername\": \"\",\n" +
            "  \"mqttPassword\":  \"\",\n" +
            "  \"radiusLocation\":  140,\n" +
            "  \"reverseGeocodeAddress\":  \"https://vps3.koenhabets.nl/reverse?lon=LON&lat=LAT\"}"
    @Test
    fun testTimeline() {
        val configItem = Gson().fromJson(config, model.ConfigItem::class.java)
        val mysql = Mysql(configItem)
        val items = ArrayList<LocationItem>().apply {
            add(LocationItem(Timestamp(1609168993000), 35.686546, -29.627341))
            add(LocationItem(Timestamp(1609169000000), 35.686546, -29.627341))
            add(LocationItem(Timestamp(1609169500000), 35.686546, -29.627341))
            add(LocationItem(Timestamp(1609170013000), 35.686546, -29.627341))
            add(LocationItem(Timestamp(1609170015000), 36.686546, -29.627341))
            add(LocationItem(Timestamp(1609180015000), 36.686546, -29.627341))
            add(LocationItem(Timestamp(1609190015000), 36.686546, -29.627341))
            add(LocationItem(Timestamp(1609191015000), 36.686546, -29.627341))
        }
        val data = Timeline(configItem, mysql).getData(items, skipLast = true, skipStops = true)
        val arrayRoutes = Routes().getRouteFromStop(data, items)

        mysql.disconnect()

        assert(data.size == 2)
        assert(arrayRoutes.size == 1)
    }
}