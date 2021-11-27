package data

import model.Route
import java.sql.*

interface RouteDao {
    fun addRoute(route: Route): Route?

    //fun updateRoute(route: Route): Boolean
    //fun getRoutes(): ArrayList<Route>
    fun getRoutes(startTime: Long, endTime: Long): ArrayList<Route>
}

class RouteDaoImpl(private val conn: Connection) : RouteDao {
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

    override fun getRoutes(startTime: Long, endTime: Long): ArrayList<Route> {
        val items: ArrayList<Route> = ArrayList()
        try {
            conn.prepareStatement(
                "Select * FROM route, location WHERE route.startLocationId = location.locationId AND (endDate between ? AND ?)"
            ).use { stmt ->
                stmt.setTimestamp(1, Timestamp(startTime))
                stmt.setTimestamp(2, Timestamp(endTime))
                stmt.executeQuery().use { rs ->
                    while (rs.next()) {
                        val routeItem = Route(
                            rs.getInt("routeId"),
                            null,
                            null,
                            rs.getInt("startLocationId"),
                            rs.getInt("endLocationId"),
                            null,
                            null,
                            null,
                            rs.getDouble("distance"),
                            null,
                            null,
                            "",
                            rs.getDouble("speed")
                        )
                        items.add(routeItem)
                    }
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }

        return items
    }
}