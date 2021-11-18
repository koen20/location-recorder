package data

import model.Route
import java.sql.Connection
import java.sql.SQLException
import java.sql.Statement
import java.sql.Types

interface RouteDao {
    fun addRoute(route: Route): Route?
    //fun updateRoute(route: Route): Boolean
    //fun getRoutes(): ArrayList<Route>
    //fun getRoutesView(startTime: Long, endTime: Long): ArrayList<RouteView>
}

class RouteDaoImpl (private val conn: Connection) : RouteDao {
    //add route to db, returns added route with the generated id
    override fun addRoute(route: Route): Route? {
        var routeAdded: Route? = null
        try {
            val insert = "INSERT INTO route VALUES(NULL, ?, ?, ?, ?, ?)"
            val pst = conn.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)
            pst.use { ps ->
                ps.setInt(1, route.startLocationId)
                ps.setInt(2, route.endLocationId)
                if (route.speed !== null) {
                    ps.setDouble(3, route.speed!!)
                } else {
                    ps.setNull(3, Types.DOUBLE)
                }
                ps.setDouble(4, route.distance)
                ps.setNull(5, Types.INTEGER)

                ps.execute()
            }
            routeAdded = route

            val rs = pst.generatedKeys
            if (rs.next()) {
                routeAdded.routeId = rs.getInt(1)
            }
        } catch (exception: SQLException) {
            exception.printStackTrace()
        }
        return routeAdded
    }
}