import java.sql.*;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class Mysql {
    static Connection conn;
    static ArrayList<Stop> stops;

    public Mysql(String server, String username, String password) {
        try {
            conn = DriverManager.getConnection(server, username, password);
            Timer updateTimer = new Timer();
            updateTimer.scheduleAtFixedRate(new checkMysqlConnection(), 2000, 60000);
            stops = getStops();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<Stop> getStops(){
        ArrayList<Stop> stops = new ArrayList<>();
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM stops");
            while (rs.next()){
                Stop stop = new Stop(rs.getString("name"), rs.getDouble("lat"), rs.getDouble("lon"), rs.getInt("radius"));
                stops.add(stop);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return stops;
    }

    public static boolean AddStop(Stop stop){
        boolean added = false;
        try {
            String insert = "INSERT INTO stops VALUES(NULL, ?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(insert);
            ps.setString(1, stop.getName());
            ps.setDouble(2, stop.getLat());
            ps.setDouble(3, stop.getLon());
            ps.setInt(4, stop.getRadius());
            ps.execute();
            ps.close();
            added = true;
        } catch (SQLException exception){
            exception.printStackTrace();
        }

        return added;
    }

    private class checkMysqlConnection extends TimerTask {
        @Override
        public void run() {
            try {
                if (!conn.isValid(3000)) {
                    conn.close();
                    conn = DriverManager.getConnection(main.confItem.getMysqlServer(),
                            main.confItem.getMysqlUsername(), main.confItem.getMysqlPassword());
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
