package pt.tecnico.hds.client.integration;

import static org.junit.Assume.*;
import org.dbunit.*;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;

import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.json.JSONObject;
import org.junit.*;
import pt.tecnico.hds.client.HdsClient;
import org.junit.runner.RunWith;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.*;
import java.net.ConnectException;

import static com.sun.org.apache.xerces.internal.util.PropertyState.is;
import static org.mockito.Matchers.notNull;

public class ClientServiceTest extends DatabaseTestCase {
    public static final String TABLE_LOGIN = "salarydetails";
    private FlatXmlDataSet loadedDataSet;
    //private SalaryCalcutation salaryCalicutation;
    private Connection jdbcConnection;

    public ClientServiceTest (String name) {
        super( name );
        System.setProperty( PropertiesBasedJdbcDatabaseTester.DBUNIT_DRIVER_CLASS, "org.sqlite.JDBC" );
        System.setProperty( PropertiesBasedJdbcDatabaseTester.DBUNIT_CONNECTION_URL, "jdbc:sqlite:../HDS-notary-server/db/hds.db" );
    }

    /** * Provide a connection to the database * @return IDatabaseConnection */
    protected IDatabaseConnection getConnection() throws Exception {
        Class.forName("org.sqlite.JDBC");
        jdbcConnection = DriverManager.getConnection("jdbc:sqlite:../HDS-notary-server/db/hds.db");
        return new DatabaseConnection(jdbcConnection);
    }

    public String sendTo(String hostname, int port, String payload) {
        boolean sent = false;

        try {
            // getting localhost ip
            InetAddress ip = InetAddress.getByName(hostname);

            // establish the connection with server port 5056
            Socket s = new Socket(ip, port);

            DataInputStream dis = new DataInputStream(s.getInputStream());
            DataOutputStream dos = new DataOutputStream(s.getOutputStream());
            dos.writeUTF(payload);

            String value = dis.readUTF();
            s.close();
            dis.close();
            dos.close();
            return value;
        } catch (UnknownHostException e) {
            // TODO
        } catch (IOException e) {
            // TODO
        }
        return "";
    }

    /** * Load the data which will be inserted for the test * @return IDataSet */
    protected IDataSet getDataSet() {
        try {

            loadedDataSet = new FlatXmlDataSet(new FileInputStream("dbunitData.xml"));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return loadedDataSet;
    }

    public void insert(String goodsId, String userId) throws Exception {
        String sql = "INSERT INTO notary(goodsId, userId, onSale) Values(?,?, FALSE )";

        Connection conn = getConnection().getConnection();
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, goodsId);
            pstmt.setString(2, userId);
            pstmt.executeUpdate();
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

    }

    public Boolean serverIsUp() {
        try {
            Socket s = new Socket("localhost", 19999);
            s.close();
            return true;
        }
        catch (ConnectException e) {
            return false;
        }
        catch (Exception e) {
            return false;
        }
    }

    public String update(String goodsId) throws Exception {
        String sql = "UPDATE notary SET onSale = ? WHERE goodsId = ?";

        try (Connection conn = getConnection().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setBoolean(1, true);
            pstmt.setString(2, goodsId);
            pstmt.executeUpdate();
            return "YES";
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return "NO";
    }

    @Test
    public void testIsNotForSale() throws Exception {
        assumeTrue("Server is not Up",serverIsUp());
        insert("good30", "user30");
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("Action","getStateOfGood");
        jsonObj.put("Good","good30");
        jsonObj.put("Buyer","user30");
        Assert.assertEquals("{'Owner':'user30', 'Good':'good30', 'OnSale': 'False'}", sendTo("localhost", 19999, jsonObj.toString()));
    }

    @Test
    public void testIsForSale() throws Exception {
        assumeTrue("Server is not Up",serverIsUp());
        insert("good30", "user30");
        update("good30");
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("Action","getStateOfGood");
        jsonObj.put("Good","good30");
        jsonObj.put("Buyer","user30");
        Assert.assertEquals("{'Owner':'user30','Good':'good30' ,'OnSale': 'True'}", sendTo("localhost", 19999, jsonObj.toString()));
    }

    @Test
    public void testBuyGoodSuccess() throws Exception {
        assumeTrue("Server is not Up",serverIsUp());
        HdsClient h = new HdsClient("user1", 3999+ 1);
        insert("good30", "user30");
        update("good30");
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("Action","buyGood");
        jsonObj.put("Good","good30");
        jsonObj.put("Buyer","user30");
        Assert.assertEquals("YES", sendTo("localhost", 4000, jsonObj.toString()));
    }

}



