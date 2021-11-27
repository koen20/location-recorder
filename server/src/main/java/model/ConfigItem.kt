package model

class ConfigItem(
    val mqttServer: String,
    val mqttUsername: String,
    val mqttPassword: String,
    val mysqlServer: String,
    val owntrackTopic: Array<String>,
    val mysqlUsername: String,
    val mysqlPassword: String,
    val radiusLocation: Int,
    val reverseGeocodeAddress: String
)