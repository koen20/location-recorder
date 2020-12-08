import org.json.JSONArray;
import org.json.JSONObject;

public class Routes {
    public static JSONArray getRouteFromStop(JSONArray stops, JSONArray all){
        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < stops.length(); i++) { //loop through every stop
            try {
                JSONObject item = stops.getJSONObject(i);
                JSONObject itemS = stops.getJSONObject(i + 1);
                int distanceTot = 0;
                double lastLat = item.getDouble("lat");
                double lastLon = item.getDouble("lon");
                for (int k = 0; k < all.length(); k++) { //loop through all locations
                    JSONObject item2 = all.getJSONObject(k);

                    //get all locations between two stops
                    if (item2.getLong("date") >= item.getLong("end") && item2.getLong("date") <= itemS.getLong("start")) {
                        distanceTot += Timeline.distance(item2.getDouble("lat"), lastLat, item2.getDouble("lon"), lastLon, 0, 0);
                        lastLat = item2.getDouble("lat");
                        lastLon = item2.getDouble("lon");
                    }
                }
                double time = itemS.getLong("start") - item.getLong("end");
                double timeHours = ((time / 1000) / 60) / 60;
                double speed = (distanceTot / 1000.0) / timeHours;

                String movementType = "unknown";
                if (speed < 9){
                    movementType = "walking";
                } else if (speed >= 9 ){
                    movementType = "driving";
                }

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("start", item.getLong("end"));
                jsonObject.put("end", itemS.getLong("start"));
                jsonObject.put("route", item.getString("location") + " - " + itemS.getString("location"));
                jsonObject.put("distance", distanceTot);
                jsonObject.put("time", time);
                jsonObject.put("speed", speed);
                jsonObject.put("movementType", movementType);
                jsonArray.put(jsonObject);

            } catch (Exception e) {
                //e.printStackTrace();
            }
        }

        return jsonArray;
    }
}
