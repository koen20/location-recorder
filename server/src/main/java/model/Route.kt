package model
//todo most variables need to removed to match the database,
// this will be done when the fetching of routes from the database works properly
data class Route(
    var routeId: Int?,
    var startDate: Long?,
    var endDate: Long?,
    var startLocationId: Int,
    var endLocationId: Int,
    var startLocation: String?,
    var stopLocation: String?,
    var route: String?,
    var distance: Double,
    var time: Double?,
    var pointCount: Int?,
    var movementType: String?,
    var speed: Double?
)
