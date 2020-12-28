import com.google.gson.Gson
import io.ktor.application.*
import io.ktor.routing.*
import java.io.FileReader

class Main

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
    val configItem = Gson().fromJson(FileReader("config.json"), ConfigItem::class.java)
    val mysql = Mysql(configItem)
    routing {
        data(mysql, configItem)
    }
}
