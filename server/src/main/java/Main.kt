import com.google.gson.Gson
import io.ktor.application.*
import io.ktor.http.content.*
import io.ktor.routing.*
import model.ConfigItem
import java.io.FileReader
import java.util.*

class Main

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
    val configItem = Gson().fromJson(FileReader("config.json"), ConfigItem::class.java)
    val mysql = Mysql(configItem)

    Timer().scheduleAtFixedRate(object : TimerTask() {
        override fun run() {
            Timeline(configItem, mysql).addItemsToDb()
        }
    }, 2000, 1200000) //20 minutes
    routing {
        data(mysql, configItem)

        static("/") {
            resources("static")
            defaultResource("static/index.html")
        }
    }
}