import org.eclipse.paho.client.mqttv3.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;

import static org.eclipse.paho.client.mqttv3.MqttClient.generateClientId;

public class Mqtt implements MqttCallbackExtended {
    MqttClient client;

    public Mqtt() {
        try {
            client = new MqttClient(main.confItem.getMqttServer(), generateClientId());
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setUserName(main.confItem.getMqttUsername());
            connOpts.setPassword(main.confItem.getMqttPassword().toCharArray());
            connOpts.setAutomaticReconnect(true);
            connOpts.setCleanSession(false);
            client.connect(connOpts);
            client.setCallback(this);
            client.subscribe(main.confItem.getOwntrackTopic());
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        Calendar cal = Calendar.getInstance();
        try {
            JSONObject jsonObject = new JSONObject(message.toString());
            if (jsonObject.has("lat")) {
                String tst = getMysqlDateString(cal.getTimeInMillis() / 1000);
                try {
                    tst = getMysqlDateString(jsonObject.getLong("tst"));
                } catch (JSONException ignored) {

                }
                Statement stmt = Mysql.conn.createStatement();
                stmt.executeUpdate("INSERT INTO data VALUES (NULL, '" + tst + "', '"
                        + jsonObject.getDouble("lat") + "', '" + jsonObject.getDouble("lon") + "'" +
                        ", '" + jsonObject.getDouble("alt") + "', '" + jsonObject.getDouble("acc")
                        + "',  '" + jsonObject.getInt("batt") + "', '" + jsonObject.getString("tid") + "')");
                stmt.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                System.out.println(Mysql.conn.isValid(3000));
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            System.out.println(message.toString());
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }

    @Override
    public void connectComplete(boolean reconnect, java.lang.String serverURI) {
        if (reconnect) {
            try {
                client.subscribe(main.confItem.getOwntrackTopic());
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void connectionLost(Throwable cause) {

    }

    static String getMysqlDateString(long milliseconds) {
        java.util.Date dt = new java.util.Date(milliseconds * 1000);

        java.text.SimpleDateFormat sdf =
                new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        String currentTime = sdf.format(dt);
        return currentTime;
    }
}
