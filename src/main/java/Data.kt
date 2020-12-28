import com.google.gson.Gson
import spark.Request
import spark.Response

class Data(val mysql: Mysql) {

    fun getData(request: Request, response: Response): String {
        val startTime = request.queryParams("startTime").toLong()
        val endTime = request.queryParams("endTime").toLong()
        return Gson().toJson(mysql.getData(startTime, endTime))
    }
}