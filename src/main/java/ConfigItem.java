public class ConfigItem {
    private String mqttServer;
    private String mqttUsername;
    private String mqttPassword;
    private String mysqlServer;
    private String[] owntracksTopic;
    private String mysqlUsername;
    private String mysqlPassword;
    private int radiusLocation;

    public ConfigItem(String mqttServer, String mqttUsername, String mqttPassword, String mysqlServer, String[] owntrackTopic, String mysqlUsername, String mysqlPassword, int radiusLocation) {
        this.mqttServer = mqttServer;
        this.mqttUsername = mqttUsername;
        this.mqttPassword = mqttPassword;
        this.mysqlServer = mysqlServer;
        this.owntracksTopic = owntrackTopic;
        this.mysqlUsername = mysqlUsername;
        this.mysqlPassword = mysqlPassword;
        this.radiusLocation = radiusLocation;
    }

    public String getMqttServer() {
        return mqttServer;
    }

    public String getMysqlServer() {
        return mysqlServer;
    }

    public String[] getOwntrackTopic() {
        return owntracksTopic;
    }

    public String getMysqlUsername() {
        return mysqlUsername;
    }

    public String getMysqlPassword() {
        return mysqlPassword;
    }

    public String getMqttUsername() {
        return mqttUsername;
    }

    public String getMqttPassword() {
        return mqttPassword;
    }

    public int getRadiusLocation() {
        return radiusLocation;
    }
}
