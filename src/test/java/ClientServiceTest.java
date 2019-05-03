import static org.junit.Assume.*;

import org.junit.*;
import org.junit.runner.RunWith;
import pt.tecnico.hds.client.HdsClient;
import mockit.Mocked;
import mockit.integration.junit4.JMockit;
import java.util.Arrays;
import java.util.Scanner;
/*
QueryDataSet queryDataSet = new QueryDataSet(new DatabaseConnection(DriverManager.getConnection("jdbc:sqlite:../HDS-notary-server/db/hds.db")));
queryDataSet.addTable("users", "SELECT userId FROM users");
queryDataSet.addTable("goods", "SELECT goodsId FROM goods");
queryDataSet.addTable("notary", "SELECT userId,goodsId,CASE WHEN LOWER(onSale) = 'true' THEN 1 ELSE 0 END AS onSale FROM notary");
FlatXmlDataSet.write(queryDataSet, new FileOutputStream("dbunitData.xml"));
 */


@RunWith(JMockit.class)
public class ClientServiceTest extends BaseTest {

    public ClientServiceTest () {
        super("");
    }

    /** * Load the data which will be inserted for the test * @return IDataSet */
    /*protected IDataSet getDataSet() {

        try {
            QueryDataSet queryDataSet = new QueryDataSet(getConnection());
            queryDataSet.addTable("users", "SELECT userId FROM users");
            queryDataSet.addTable("goods", "SELECT goodsId FROM goods");
            queryDataSet.addTable("notary", "SELECT userId,goodsId,CASE WHEN LOWER(onSale) = 'true' THEN 1 ELSE 0 END AS onSale FROM notary");
            FlatXmlDataSet.write(queryDataSet, new FileOutputStream("dbunitData.xml"));
            //System.exit(0);
            //flatXmlDataSet = new FlatXmlDataSet(new FileInputStream("dbunitData.xml"));
            //dataSet.addTable("BAR");
            //loadedDataSet = new FlatXmlDataSet(new FileInputStream("dbunitData.xml"));
            loadedDataSet = new FlatXmlDataSetBuilder().build(new FileInputStream("dbunitData.xml"));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            return loadedDataSet;
        }

    }*/

    @Test
        public void testIsNotForSale(@Mocked final Scanner scn) throws Exception {
        new ClientCmdExpectations(Arrays.asList("getStateOfGood good1", "Exit"), scn);
        assumeTrue("Server is not Up",serverIsUp());
        HdsClient cSeller = new HdsClient("user1", 3999+1);
        cSeller.connectToServer("localhost", 19999);

        // getStateOfGood chain
        commandSignatureChecker(cSeller.requests, cSeller.serverPublicKey, "user1.pub", 0);

        getStateOfGoodChecker(cSeller.requests, "user5", "good1", "false", 3);

        cSeller.serverThread.interrupt();
    }

    @Test
    public void testIsForSale(@Mocked final Scanner scn) throws Exception {
        new ClientCmdExpectations(Arrays.asList("intentionToSell good1", "getStateOfGood good1", "Exit"), scn);
        assumeTrue("Server is not Up",serverIsUp());

        HdsClient cSeller = new HdsClient("user5", 3999+5);
        cSeller.connectToServer("localhost", 19999);

        // intentionToSell chain
        commandSignatureChecker(cSeller.requests, cSeller.serverPublicKey, "user5.pub", 0);
        intentionToSellChecker(cSeller.requests, "YES", 3);

        // getStateOfGood chain
        commandSignatureChecker(cSeller.requests, cSeller.serverPublicKey, "user5.pub", 4);
        getStateOfGoodChecker(cSeller.requests, "user5", "good1", "true", 7);

        cSeller.serverThread.interrupt();
    }


    @Test
    public void testIntentionToSellOutputSuccess(@Mocked final Scanner scn) throws Exception {
        new ClientCmdExpectations(Arrays.asList("intentionToSell good1", "Exit"), scn);
        assumeTrue("Server is not Up",serverIsUp());

        HdsClient cSeller = new HdsClient("user5", 3999+5);
        cSeller.connectToServer("localhost", 19999);

        // intentionToSell chain
        commandSignatureChecker(cSeller.requests, cSeller.serverPublicKey, "user5.pub", 0);
        intentionToSellChecker(cSeller.requests, "YES", 3);


        cSeller.serverThread.interrupt();
    }

    @Test
    public void testIntentionToSellOutputFailure(@Mocked final Scanner scn) throws Exception {
        new ClientCmdExpectations(Arrays.asList("intentionToSell good1", "Exit"), scn);
        assumeTrue("Server is not Up",serverIsUp());

        HdsClient cSeller = new HdsClient("user1", 3999+1);
        cSeller.connectToServer("localhost", 19999);

        // intentionToSell chain
        commandSignatureChecker(cSeller.requests, cSeller.serverPublicKey, "user1.pub", 0);
        intentionToSellChecker(cSeller.requests, "NO", 3);

        cSeller.serverThread.interrupt();
    }

    @Test
    public void testIntentionToSellDatabaseSuccess(@Mocked final Scanner scn) throws Exception {
        new ClientCmdExpectations(Arrays.asList("intentionToSell good7", "Exit", "getStateOfGood good7", "Exit"), scn);
        assumeTrue("Server is not Up",serverIsUp());

        HdsClient _cSeller = new HdsClient("user1", 3999+1);
        HdsClient _cBuyer = new HdsClient("user3", 3999+3);

        _cSeller.connectToServer("localhost", 19999);
        _cBuyer.connectToServer("localhost", 19999);

        // intentionToSell chain
        commandSignatureChecker(_cSeller.requests, _cSeller.serverPublicKey, "user1.pub", 0);
        intentionToSellChecker(_cSeller.requests, "YES", 3);

        // getStateOfGood chain
        commandSignatureChecker(_cBuyer.requests, _cSeller.serverPublicKey, "user3.pub", 0);
        getStateOfGoodChecker(_cBuyer.requests, "user1", "good7", "true", 3);
        _cBuyer.serverThread.interrupt();
        _cSeller.serverThread.interrupt();
    }

    @Test
    public void testIntentionToSellDatabaseFailure(@Mocked final Scanner scn) throws Exception {
        new ClientCmdExpectations(Arrays.asList("intentionToSell good1", "Exit", "getStateOfGood good1", "Exit"), scn);
        assumeTrue("Server is not Up",serverIsUp());

        HdsClient _cSeller = new HdsClient("user1", 3999+1);
        HdsClient _cBuyer = new HdsClient("user3", 3999+3);

        _cSeller.connectToServer("localhost", 19999);
        _cBuyer.connectToServer("localhost", 19999);

        // intentionToSell chain
        commandSignatureChecker(_cSeller.requests, _cSeller.serverPublicKey, "user1.pub", 0);
        intentionToSellChecker(_cSeller.requests, "NO", 3);

        // getStateOfGood chain
        commandSignatureChecker(_cBuyer.requests, _cSeller.serverPublicKey, "user3.pub", 0);
        getStateOfGoodChecker(_cBuyer.requests, "user5", "good1", "false", 3);

        _cBuyer.serverThread.interrupt();
        _cSeller.serverThread.interrupt();
    }


    @Test
    public void testBuyGoodThatAClientDoesntOwn(@Mocked final Scanner scn) throws Exception {
        new ClientCmdExpectations(Arrays.asList("intentionToSell good19", "Exit", "buyGood good19 user8", "Exit"), scn);
        assumeTrue("Server is not Up",serverIsUp());
        String buyer = "user1";
        String seller = "user8";
        String offseller = "user3";
        int portBuyer = 3999+1;
        int portSeller = 3999+8;
        int portOffSeller = 3999+3;
        HdsClient cBuyer = new HdsClient(buyer, portBuyer);
        HdsClient cSeller = new HdsClient(seller, portSeller);
        HdsClient cOffSeller = new HdsClient(offseller, portOffSeller);

        cOffSeller.connectToServer("localhost", 19999);
        cBuyer.connectToServer("localhost", 19999);


        // intentionToSell chain
        Assert.assertEquals(4,cOffSeller.requests.length());
        commandSignatureChecker(cOffSeller.requests, cOffSeller.serverPublicKey, "user3.pub", 0);
        intentionToSellChecker(cOffSeller.requests, "YES", 3);

        // TransferGood chain
        Assert.assertEquals(4, cSeller.requests.length());
        commandSignatureChecker(cSeller.requests, cOffSeller.serverPublicKey, "user8.pub","user1.pub", 0);
        intentionToSellChecker(cSeller.requests, "NO", 3);

        // BuyGood chain
        Assert.assertEquals(2, cBuyer.requests.length());
        isSigned(cBuyer.requests.getJSONObject(0), "assymetricKeys/user1.pub");
        intentionToSellChecker(cBuyer.requests, "NO", 1);

        cBuyer.serverThread.interrupt();
        cSeller.serverThread.interrupt();
        cOffSeller.serverThread.interrupt();

    }

    @Test
    public void testBuyGoodOwnGoodFailure(@Mocked final Scanner scn) throws Exception {
        new ClientCmdExpectations(Arrays.asList("intentionToSell good3", "buyGood good3 user9", "Exit"), scn);
        assumeTrue("Server is not Up",serverIsUp());
        String buyer = "user9";
        int portBuyer = 3999+9;
        HdsClient cBuyer = new HdsClient(buyer, portBuyer);

        cBuyer.connectToServer("localhost", 19999);


        // intentionToSell chain
        Assert.assertEquals(10,cBuyer.requests.length());
        commandSignatureChecker(cBuyer.requests, cBuyer.serverPublicKey, "user9.pub", 0);
        intentionToSellChecker(cBuyer.requests, "YES", 3);

        // TransferGood chain
        commandSignatureChecker(cBuyer.requests, cBuyer.serverPublicKey, "user9.pub","user9.pub", 5);
        intentionToSellChecker(cBuyer.requests, "NO", 8);

        // BuyGood chain
        isSigned(cBuyer.requests.getJSONObject(4), "assymetricKeys/user9.pub");
        intentionToSellChecker(cBuyer.requests, "NO", 9);
        cBuyer.serverThread.interrupt();

    }

    @Test
    public void testBuyGoodSuccess(@Mocked final Scanner scn) throws Exception {
        new ClientCmdExpectations(Arrays.asList("intentionToSell good12", "Exit", "buyGood good12 user10", "Exit"), scn);
        assumeTrue("Server is not Up",serverIsUp());
        String buyer = "user1";
        String seller = "user10";
        int portBuyer = 3999+1;
        int portSeller = 3999+10;

        HdsClient cBuyer = new HdsClient(buyer, portBuyer);
        HdsClient cSeller = new HdsClient(seller, portSeller);


        cSeller.connectToServer("localhost", 19999);
        cBuyer.connectToServer("localhost", 19999);

        // intentionToSell chain
        Assert.assertEquals(8,cSeller.requests.length());
        commandSignatureChecker(cSeller.requests, cSeller.serverPublicKey, "user10.pub", 0);
        intentionToSellChecker(cSeller.requests, "YES", 3);

        // TransferGood chain
        commandSignatureChecker(cSeller.requests, cSeller.serverPublicKey, "user10.pub","user1.pub", 4);
        intentionToSellChecker(cSeller.requests, "YES", 7);

        // BuyGood chain
        Assert.assertEquals(2,cBuyer.requests.length());
        isSigned(cBuyer.requests.getJSONObject(0), "assymetricKeys/user1.pub");
        intentionToSellChecker(cBuyer.requests, "YES", 1);
        cBuyer.serverThread.interrupt();
        cSeller.serverThread.interrupt();
    }

    @Test
    public void testBuyGoodFailure(@Mocked final Scanner scn) throws Exception {
        new ClientCmdExpectations(Arrays.asList("buyGood good15 user7", "Exit"), scn);
        assumeTrue("Server is not Up",serverIsUp());
        String buyer = "user1";
        String seller = "user7";
        int portBuyer = 3999+1;
        int portSeller = 3999+7;

        HdsClient cBuyer = new HdsClient(buyer, portBuyer);
        HdsClient cSeller = new HdsClient(seller, portSeller);


        cBuyer.connectToServer("localhost", 19999);

        // TransferGood chain
        Assert.assertEquals(4,cSeller.requests.length());
        commandSignatureChecker(cSeller.requests, cSeller.serverPublicKey, "user7.pub","user1.pub", 0);
        intentionToSellChecker(cSeller.requests, "NO", 3);

        // BuyGood chain
        Assert.assertEquals(2,cBuyer.requests.length());
        isSigned(cBuyer.requests.getJSONObject(0), "assymetricKeys/user1.pub");
        intentionToSellChecker(cBuyer.requests, "NO", 1);
        //The good was not for sale.
        cBuyer.serverThread.interrupt();
        cSeller.serverThread.interrupt();
    }

    @Test
    public void testBuyGoodDoesNotExist(@Mocked final Scanner scn) throws Exception {
        new ClientCmdExpectations(Arrays.asList("buyGood good50 user6", "Exit"), scn);
        assumeTrue("Server is not Up",serverIsUp());
        String buyer = "user1";
        String seller = "user6";
        int portBuyer = 3999+1;
        int portSeller = 3999+6;

        HdsClient cBuyer = new HdsClient(buyer, portBuyer);
        HdsClient cSeller = new HdsClient(seller, portSeller);


        cBuyer.connectToServer("localhost", 19999);

        // TransferGood chain
        Assert.assertEquals(4,cSeller.requests.length());
        commandSignatureChecker(cSeller.requests, cSeller.serverPublicKey, "user6.pub","user1.pub", 0);
        intentionToSellChecker(cSeller.requests, "NO", 3);

        // BuyGood chain
        Assert.assertEquals(2,cBuyer.requests.length());
        isSigned(cBuyer.requests.getJSONObject(0), "assymetricKeys/user1.pub");
        intentionToSellChecker(cBuyer.requests, "NO", 1);
        cBuyer.serverThread.interrupt();
        cSeller.serverThread.interrupt();
    }

    @Test
    public void testBuyGoodBuyerDoesNotExist(@Mocked final Scanner scn) throws Exception {
        new ClientCmdExpectations(Arrays.asList("intentionToSell good8", "Exit", "buyGood good8 user4", "Exit"), scn);
        assumeTrue("Server is not Up",serverIsUp());
        String buyer = "user30";
        String seller = "user4";
        int portBuyer = 3999+30;
        int portSeller = 3999+4;

        HdsClient cBuyer = new HdsClient(buyer, portBuyer);
        HdsClient cSeller = new HdsClient(seller, portSeller);
        cBuyer._myMap.put("user30", 3999+30);
        cSeller._myMap.put("user30", 3999+30);

        cSeller.connectToServer("localhost", 19999);
        cBuyer.connectToServer("localhost", 19999);

        // intentionToSell chain
        Assert.assertEquals(8,cSeller.requests.length());
        commandSignatureChecker(cSeller.requests, cSeller.serverPublicKey, "user4.pub", 0);
        intentionToSellChecker(cSeller.requests, "YES", 3);

        // TransferGood chain
        commandSignatureChecker(cSeller.requests, cSeller.serverPublicKey, "user4.pub","user30.pub", 4);
        intentionToSellChecker(cSeller.requests, "NO", 7);

        // BuyGood chain
        Assert.assertEquals(2,cBuyer.requests.length());
        isSigned(cBuyer.requests.getJSONObject(0), "assymetricKeys/user30.pub");
        intentionToSellChecker(cBuyer.requests, "NO", 1);
        cBuyer.serverThread.interrupt();
        cSeller.serverThread.interrupt();
    }

    @Test
    public void testBuyGoodSellerDoesNotExist(@Mocked final Scanner scn) throws Exception {
        new ClientCmdExpectations(Arrays.asList("intentionToSell good7", "Exit", "buyGood good11 user30", "Exit"), scn);
        assumeTrue("Server is not Up",serverIsUp());
        String buyer = "user1";
        String seller = "user30";
        int portBuyer = 3999+1;
        int portSeller = 3999+30;

        HdsClient cBuyer = new HdsClient(buyer, portBuyer);
        HdsClient cSeller = new HdsClient(seller, portSeller);

        cBuyer._myMap.put("user30", 3999+30);
        cSeller._myMap.put("user30", 3999+30);
        cSeller.connectToServer("localhost", 19999);
        cBuyer.connectToServer("localhost", 19999);

        // intentionToSell chain
        Assert.assertEquals(8,cSeller.requests.length());
        commandSignatureChecker(cSeller.requests, cSeller.serverPublicKey, "user30.pub", 0);
        intentionToSellChecker(cSeller.requests, "NO", 3);

        // TransferGood chain
        commandSignatureChecker(cSeller.requests, cSeller.serverPublicKey, "user30.pub","user1.pub", 4);
        intentionToSellChecker(cSeller.requests, "NO", 7);

        // BuyGood chain
        Assert.assertEquals(2,cBuyer.requests.length());
        isSigned(cBuyer.requests.getJSONObject(0), "assymetricKeys/user1.pub");
        intentionToSellChecker(cBuyer.requests, "NO", 1);
        cBuyer.serverThread.interrupt();
        cSeller.serverThread.interrupt();

    }

}


