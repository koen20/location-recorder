package model

data class Route(
    var routeId: Int?,
    var startDate: Long,
    var endDate: Long,
    var startLocationId: Int,
    var endLocationId: Int,
    var distance: Double,
    var time: Double,
    var pointCount: Int?,
    var movementType: String?,
    var speed: Double?
)
