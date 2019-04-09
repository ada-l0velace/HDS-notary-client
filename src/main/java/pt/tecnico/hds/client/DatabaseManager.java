package pt.tecnico.hds.client;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;

public class DatabaseManager {
    private static DatabaseManager databaseManager;
    private static Connection conn = null;
    private final String url = "jdbc:sqlite:db/client.db";
    // static method to create instance of Singleton class
    public static DatabaseManager getInstance() {
        if (databaseManager == null)
            databaseManager = new DatabaseManager();

        return databaseManager;
    }

    public void createDatabase() {
        String requests = "CREATE TABLE IF NOT EXISTS requests(requestId text PRIMARY KEY);";
        try {
            if (!Files.exists(Paths.get("db/hds.db"))) {
                conn = DriverManager.getConnection(url);
                Statement stmt4 = conn.createStatement();
                stmt4.execute(requests);

                //populate();
            }
            else{
                conn = DriverManager.getConnection(url);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void addToRequests(String hash){
        String sql = "INSERT INTO requests(requestId) Values(?)";

        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, hash);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public boolean verifyReplay(String hash) {
        String sql = "SELECT requestId FROM requests WHERE requestId=?";
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            System.out.println(hash);
            pstmt.setString(1, hash);
            ResultSet rs = pstmt.executeQuery();
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
