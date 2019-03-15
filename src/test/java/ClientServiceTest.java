import static org.junit.Assume.*;
import org.dbunit.*;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;

import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.json.JSONArray;
import org.json.JSONException;
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
    private int serverPort = 19999;
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
            Socket s = new Socket("localhost", serverPort);
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

    public boolean isJSONValid(String test) {
        try {
            new JSONObject(test);
        } catch (JSONException ex) {
            // edited, to include @Arthur's comment
            // e.g. in case JSONArray is valid as well...
            try {
                new JSONArray(test);
            } catch (JSONException ex1) {
                return false;
            }
        }
        return true;
    }

    @Test
    public void testIsNotForSale() throws Exception {
        assumeTrue("Server is not Up",serverIsUp());
        insert("good30", "user30");
        HdsClient cSeller = new HdsClient("user30", 3999+30);
        JSONObject jsonObj = cSeller.sendJson("getStateOfGood good30");

        String serverAnswer = sendTo("localhost", serverPort, jsonObj.toString());
        Assert.assertTrue("The server answer is not valid json.",isJSONValid(serverAnswer));
        jsonObj = new JSONObject(serverAnswer);
        jsonObj = new JSONObject(jsonObj.getString("Message"));

        Assert.assertEquals("The Owner value is wrong.","user30", jsonObj.getString("Owner"));
        Assert.assertEquals("The Good value is wrong.","good30", jsonObj.getString("Good"));
        Assert.assertEquals("The OnSale value is wrong.","false", jsonObj.getString("OnSale"));

        //Assert.assertEquals("{\"Owner\":\"user30\", \"Good\":\"good30\", \"OnSale\": \"False\"}", sendTo("localhost", serverPort, jsonObj.toString()));
    }

    @Test
    public void testIsForSale() throws Exception {
        assumeTrue("Server is not Up",serverIsUp());
        insert("good30", "user30");
        update("good30");
        HdsClient cSeller = new HdsClient("user30", 3999+30);
        JSONObject jsonObj = cSeller.sendJson("getStateOfGood good30");

        String serverAnswer = sendTo("localhost", serverPort, jsonObj.toString());
        Assert.assertTrue("The server answer is not valid json.",isJSONValid(serverAnswer));
        jsonObj = new JSONObject(serverAnswer);
        jsonObj = new JSONObject(jsonObj.getString("Message"));

        Assert.assertEquals("The Owner value is wrong.","user30", jsonObj.getString("Owner"));
        Assert.assertEquals("The Good value is wrong.","good30", jsonObj.getString("Good"));
        Assert.assertEquals("The OnSale value is wrong.","true", jsonObj.getString("OnSale"));

    }

    @Test
    public void testIntentionToSellOutputSuccess() throws Exception {
        assumeTrue("Server is not Up",serverIsUp());
        insert("good30", "user30");
        HdsClient cSeller = new HdsClient("user30", 3999+30);

        JSONObject jsonObj = cSeller.sendJson("intentionToSell good30");
        Assert.assertEquals("YES", sendTo("localhost", serverPort, jsonObj.toString()));
    }

    @Test
    public void testIntentionToSellOutputFailure() throws Exception {
        assumeTrue("Server is not Up",serverIsUp());
        insert("good30", "user30");
        HdsClient cSeller = new HdsClient("user1", 3999+1);

        JSONObject jsonObj = cSeller.sendJson("intentionToSell good30");
        //System.out.println(jsonObj.toString());
        Assert.assertEquals("NO", sendTo("localhost", serverPort, jsonObj.toString()));
    }

    @Test
    public void testIntentionToSellDatabaseSuccess() throws Exception {
        assumeTrue("Server is not Up",serverIsUp());
        insert("good30", "user30");
        HdsClient cBuyer = new HdsClient("user30", 3999+30);

        JSONObject jsonObj = cBuyer.sendJson("intentionToSell good30"); //new JSONObject();
        sendTo("localhost", serverPort, jsonObj.toString());

        JSONObject jsonObj2 = cBuyer.sendJson("getStateOfGood good30"); //new JSONObject();
        String serverAnswer = sendTo("localhost", serverPort, jsonObj2.toString());
        Assert.assertTrue("The server answer is not valid json.",isJSONValid(serverAnswer));

        jsonObj = new JSONObject(serverAnswer);
        jsonObj = new JSONObject(jsonObj.getString("Message"));

        Assert.assertEquals("The Owner value is wrong.","user30", jsonObj.getString("Owner"));
        Assert.assertEquals("The Good value is wrong.","good30", jsonObj.getString("Good"));
        Assert.assertEquals("The OnSale value is wrong.","true", jsonObj.getString("OnSale"));

    }

    @Test
    public void testIntentionToSellDatabaseFailure() throws Exception {
        assumeTrue("Server is not Up",serverIsUp());
        insert("good30", "user30");

        HdsClient cBuyer = new HdsClient("user30", 3999+30);
        //HdsClient cSeller = new HdsClient("user30", 3999+30);

        JSONObject jsonObj = cBuyer.sendJson("intentionToSell good30");

        sendTo("localhost", serverPort, jsonObj.toString());
        JSONObject jsonObj2 = cBuyer.sendJson("getStateOfGood good30");


        String serverAnswer = sendTo("localhost", serverPort, jsonObj2.toString());

        Assert.assertTrue("The server answer is not valid json.",isJSONValid(serverAnswer));
        jsonObj = new JSONObject(serverAnswer);
        jsonObj = new JSONObject(jsonObj.getString("Message"));

        Assert.assertEquals("The Owner value is wrong.","user1", jsonObj.getString("Owner"));
        Assert.assertEquals("The Good value is wrong.","good30", jsonObj.getString("Good"));
        Assert.assertEquals("The OnSale value is wrong.","false", jsonObj.getString("OnSale"));
    }

    @Test
    public void testBuyGoodOwnGoodFailure() throws Exception {
        assumeTrue("Server is not Up",serverIsUp());
        String buyer = "user1";
        String seller = "user30";
        int port = 3999+1;
        HdsClient h = new HdsClient("user30", port);
        insert("good30", "user30");
        update("good30");
        Assert.assertEquals("Users can't buy their own goods.","YES", sendTo("localhost", port, h.sendJson("buyGood good30").toString()));
    }

    @Test
    public void testBuyGoodSuccess() throws Exception {
        assumeTrue("Server is not Up",serverIsUp());
        String buyer = "user1";
        String seller = "user30";
        int portBuyer = 3999+1;
        int portSeller = 3999+30;
        HdsClient cBuyer = new HdsClient(buyer, portBuyer);
        HdsClient cSeller = new HdsClient(seller, portSeller);

        insert("good30", seller);
        update("good30");
        Assert.assertEquals("YES", sendTo("localhost", portSeller, cBuyer.sendJson("buyGood good30").toString()));
    }

    @Test
    public void testBuyGoodFailure() throws Exception {
        assumeTrue("Server is not Up",serverIsUp());
        String buyer = "user1";
        String seller = "user30";
        int portBuyer = 3999+1;
        int portSeller = 3999+30;
        HdsClient cBuyer = new HdsClient(buyer, portBuyer);
        HdsClient cSeller = new HdsClient(seller, portSeller);
        insert("good30", seller);
        Assert.assertEquals("The good was not for sale.","NO", sendTo("localhost", cSeller._port, cBuyer.sendJson("buyGood good30").toString()));
    }

    @Test
    public void testBuyGoodDoesNotExist() throws Exception {
        assumeTrue("Server is not Up",serverIsUp());
        String buyer = "user1";
        String seller = "user30";
        int portBuyer = 3999+1;
        int portSeller = 3999+30;
        HdsClient cBuyer = new HdsClient(buyer, portBuyer);
        HdsClient cSeller = new HdsClient(seller, portSeller);
        insert("good25", seller);
        Assert.assertEquals("This good does not exist.","NO", sendTo("localhost", cSeller._port, cBuyer.sendJson("buyGood good30").toString()));
    }

    @Test
    public void testBuyGoodBuyerDoesNotExist() throws Exception {
        assumeTrue("Server is not Up",serverIsUp());
        insert("good30", "user30");
        HdsClient cBuyer = new HdsClient("user1", 3999+1);
        HdsClient cSeller = new HdsClient("user30", 3999+30);
        JSONObject j = cBuyer.sendJson("buyGood good30");
        j.put("Buyer","user33");
        Assert.assertEquals("NO", sendTo("localhost", cSeller._port, j.toString()));
    }

}



