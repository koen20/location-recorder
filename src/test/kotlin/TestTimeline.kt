import com.google.gson.Gson
import org.junit.Assert
import org.junit.Test
import java.io.FileReader
import java.sql.Timestamp

class TestTimeline {
    @Test
    fun testTimeline() {
        val configItem = Gson().fromJson(FileReader("configExample.json"), ConfigItem::class.java)
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
        val data = Timeline(configItem, mysql).getData(items)
        mysql.disconnect()
        Assert.assertEquals(
            "{\"routes\":[{\"route\":\" - \",\"distance\":111195.38050108914,\"movementType\":\"driving\",\"start\":1609170013000,\"end\":1609170015000,\"time\":2000,\"speed\":200151.68490196043}],\"stops\":[{\"locationUserAdded\":false,\"start\":1609168993000,\"end\":1609170013000,\"location\":\"\",\"lon\":-29.62734,\"lat\":35.68655},{\"locationUserAdded\":false,\"start\":1609170015000,\"end\":1609191015000,\"location\":\"\",\"lon\":-29.62734,\"lat\":36.68655}]}",
            data.toString()
        )
    }
}