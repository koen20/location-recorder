import com.google.gson.Gson
import spark.Request
import spark.Response

class StopHandler(val mysql: Mysql) {
    fun addStop(request: Request, response: Response): String {
        val stop = Stop(request.queryParams("name"), request.queryParams("lat").toDouble(),
            request.queryParams("lon").toDouble(),
            Integer.parseInt(request.queryParamOrDefault("radius", "100")), true)

        if(!mysql.AddStop(stop)){
            response.status(500)
        }
        mysql.updateStops()

        return ""
    }

    fun getStops(request: Request, response: Response): String{
        return Gson().toJson(mysql.getStops())
    }
}