package pt.tecnico.hds.client;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;

public class DatabaseManager {
    private static DatabaseManager databaseManager;
    private static final String url = "jdbc:sqlite:db/client.db";
    private static Connection c = null;
    public static Connection getConn() throws SQLException {
        if(c == null){
            c = DriverManager.getConnection(url);
        }
        return c;
    }
    // static method to create instance of Singleton class
    public static DatabaseManager getInstance() {
        if (databaseManager == null)
            databaseManager = new DatabaseManager();

        return databaseManager;
    }

    public void createDatabase() {
        String requests = "CREATE TABLE IF NOT EXISTS requests(requestId text PRIMARY KEY);";
        Connection conn;
        try {
            if (!Files.exists(Paths.get("db/client.db"))) {
                conn = getConn();
                Statement stmt4 = conn.createStatement();
                //Statement stmt5 = conn.createStatement();
                stmt4.execute(requests);
                //stmt5.execute("PRAGMA journal_mode = WAL;");
                //populate();
                //conn.close();
            }


        } catch (SQLException e) {
            //System.out.println(e.getMessage());
        }
    }

    public void addToRequests(String hash){
        String sql = "INSERT INTO requests(requestId) Values(?)";
        Connection conn;
        try {
            conn = getConn();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, hash);
            pstmt.executeUpdate();
            //conn.close();
        } catch (SQLException e) {
            //System.out.println(e.getMessage());
        }
    }

    public boolean verifyReplay(String hash) {
        String sql = "SELECT requestId FROM requests WHERE requestId=?";
        Connection conn;
        try {
            conn = getConn();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            //System.out.println(hash);
            pstmt.setString(1, hash);
            ResultSet rs = pstmt.executeQuery();
            //conn.close();
            if (rs.next()) {
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
