import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Timer;
import java.util.TimerTask;

public class Mysql {
    static Connection conn;

    public Mysql(String server, String username, String password) {
        try {
            conn = DriverManager.getConnection(server, username, password);
            Timer updateTimer = new Timer();
            updateTimer.scheduleAtFixedRate(new checkMysqlConnection(), 2000, 60000);
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
