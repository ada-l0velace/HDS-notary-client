import static org.junit.Assume.*;

import org.json.JSONObject;
import org.junit.*;
import org.junit.runner.Request;
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

    public static HdsClient client1;
    public static HdsClient client2;
    public static HdsClient client3;
    public static HdsClient client4;
    public static HdsClient client5;
    public static HdsClient client6;
    public static HdsClient client7;
    public static HdsClient client8;
    public static HdsClient client9;
    public static HdsClient client10;


    public static HdsClient getClient(String name) {
        switch (name) {
            case "client1":
                ClientServiceTest.client1 = ((ClientServiceTest.client1 == null) ? new HdsClient("user1",3999+1) : ClientServiceTest.client1);
                return ClientServiceTest.client1;
            case "client2":
                ClientServiceTest.client2 = ((ClientServiceTest.client2 == null) ? new HdsClient("user2",3999+2) : ClientServiceTest.client2);
                return ClientServiceTest.client2;
            case "client3":
                ClientServiceTest.client3 = ((ClientServiceTest.client3 == null) ? new HdsClient("user3",3999+3) : ClientServiceTest.client3);
                return ClientServiceTest.client3;
            case "client4":
                ClientServiceTest.client4 = ((ClientServiceTest.client4 == null) ? new HdsClient("user4",3999+4) : ClientServiceTest.client4);
                return ClientServiceTest.client4;
            case "client5":
                ClientServiceTest.client5 = ((ClientServiceTest.client5 == null) ? new HdsClient("user5",3999+5) : ClientServiceTest.client5);
                return ClientServiceTest.client5;
            case "client6":
                ClientServiceTest.client6 = ((ClientServiceTest.client6 == null) ? new HdsClient("user6",3999+6) : ClientServiceTest.client6);
                return ClientServiceTest.client6;
            case "client7":
                ClientServiceTest.client7 = ((ClientServiceTest.client7 == null) ? new HdsClient("user7",3999+7) : ClientServiceTest.client7);
                return ClientServiceTest.client7;
            case "client8":
                ClientServiceTest.client8 = ((ClientServiceTest.client8 == null) ? new HdsClient("user8",3999+8) : ClientServiceTest.client8);
                return ClientServiceTest.client8;
            case "client9":
                ClientServiceTest.client9 = ((ClientServiceTest.client9 == null) ? new HdsClient("user9",3999+9) : ClientServiceTest.client9);
                return ClientServiceTest.client9;
            case "client10":
                ClientServiceTest.client10 = ((ClientServiceTest.client10 == null) ? new HdsClient("user10",3999+10) : ClientServiceTest.client10);
                return ClientServiceTest.client10;
            default:
                return null;
        }
    }

    public ClientServiceTest () {
        super();
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
    public void testIntentionToSellOutputSuccess() throws Exception {
        assumeTrue("Server is not Up",serverIsUp());
        HdsClient hSeller = ClientServiceTest.getClient("client5");

        JSONObject intentionToSellRequest = hSeller.sendJson("intentionToSell good1");
        isSigned(intentionToSellRequest, "assymetricKeys/user5.pub");

        JSONObject answerITS = hSeller.intentionToSell(intentionToSellRequest);
        checkAnswer(answerITS, "YES");

    }

    @Test
    public void testIntentionToSellOutputFailure() throws Exception {
        assumeTrue("Server is not Up",serverIsUp());
        HdsClient hSeller = ClientServiceTest.getClient("client1");

        JSONObject intentionToSellRequest = hSeller.sendJson("intentionToSell good1");
        isSigned(intentionToSellRequest, "assymetricKeys/user1.pub");

        JSONObject answerITS = hSeller.intentionToSell(intentionToSellRequest);
        checkAnswer(answerITS, "NO");
    }

    @Test
    public void testIntentionToSellDatabaseSuccess() throws Exception {
        assumeTrue("Server is not Up",serverIsUp());
        HdsClient hSeller = ClientServiceTest.getClient("client1");//new HdsClient("user1", 3999+1);
        HdsClient hBuyer = ClientServiceTest.getClient("client3");//new HdsClient("user3", 3999+3);

        JSONObject intentionToSellRequest = hSeller.sendJson("intentionToSell good7");
        JSONObject getStateOfGoodRequest = hBuyer.sendJson("getStateOfGood good7");

        isSigned(intentionToSellRequest, "assymetricKeys/user1.pub");
        isSigned(getStateOfGoodRequest, "assymetricKeys/user3.pub");

        JSONObject answerITS = hSeller.intentionToSell(intentionToSellRequest);
        isSigned(answerITS, hSeller.serverPublicKey);
        checkAnswer(answerITS, "YES");

        JSONObject answerGSOG = hBuyer.getStateOfGood(getStateOfGoodRequest);
        isSigned(answerGSOG, hSeller.serverPublicKey);
        checkGood(answerGSOG, "user1", "good7", "true");
    }

    @Test
    public void testIntentionToSellDatabaseFailure() throws Exception {
        assumeTrue("Server is not Up",serverIsUp());

        HdsClient hSeller = ClientServiceTest.getClient("client1");//new HdsClient("user1", 3999+1);
        HdsClient hBuyer = ClientServiceTest.getClient("client3");//new HdsClient("user3", 3999+3);

        JSONObject intentionToSellRequest = hSeller.sendJson("intentionToSell good1");
        JSONObject getStateOfGoodRequest = hBuyer.sendJson("getStateOfGood good1");

        isSigned(intentionToSellRequest, "assymetricKeys/user1.pub");
        isSigned(getStateOfGoodRequest, "assymetricKeys/user3.pub");
        JSONObject answerITS = hSeller.intentionToSell(intentionToSellRequest);
        isSigned(answerITS, hSeller.serverPublicKey);
        checkAnswer(answerITS, "NO");

        JSONObject answerGSOG = hBuyer.getStateOfGood(getStateOfGoodRequest);
        Assert.assertNotNull(answerGSOG);
        isSigned(answerGSOG, hSeller.serverPublicKey);
        checkGood(answerGSOG, "user5", "good1", "false");

    }


    @Test
    public void testBuyGoodThatAClientDoesntOwn() throws Exception {
        //new ClientCmdExpectations(Arrays.asList("intentionToSell good19", "Exit", "buyGood good19 user8", "Exit"), scn);
        assumeTrue("Server is not Up",serverIsUp());
        HdsClient bBuyer = ClientServiceTest.getClient("client1");//new HdsClient(buyer, portBuyer);
        HdsClient bSeller = ClientServiceTest.getClient("client8");//new HdsClient(seller, portSeller);
        HdsClient bOffSeller = ClientServiceTest.getClient("client3");//new HdsClient(offseller, portOffSeller);

        JSONObject intentionToSellRequest = bOffSeller.sendJson("intentionToSell good19");
        JSONObject buyRequest = bBuyer.sendJson("buyGood good19 user8");

        isSigned(intentionToSellRequest, "assymetricKeys/user3.pub");
        isSigned(buyRequest, "assymetricKeys/user1.pub");

        JSONObject answerITSRequest = bOffSeller.intentionToSell(intentionToSellRequest);
        isSigned(answerITSRequest, bSeller.serverPublicKey);
        checkAnswer(answerITSRequest, "YES");

        JSONObject answerBuyGoodRequest = bBuyer.buyGood(buyRequest);
        isSigned(answerBuyGoodRequest, bBuyer.serverPublicKey);
        checkAnswer(answerBuyGoodRequest, "NO");

        /*bOffSeller.runCommands();
        bBuyer.runCommands();


        // intentionToSell chain
        Assert.assertEquals(4,bOffSeller.requests.length());
        commandSignatureChecker(bOffSeller.requests, bOffSeller.serverPublicKey, "user3.pub", 0);
        intentionToSellChecker(bOffSeller.requests, "YES", 3);

        // TransferGood chain
        Assert.assertEquals(4, bSeller.requests.length());
        commandSignatureChecker(bSeller.requests, bOffSeller.serverPublicKey, "user8.pub","user1.pub", 0);
        intentionToSellChecker(bSeller.requests, "NO", 3);

        // BuyGood chain
        Assert.assertEquals(2, bBuyer.requests.length());
        isSigned(bBuyer.requests.getJSONObject(0), "assymetricKeys/user1.pub");
        intentionToSellChecker(bBuyer.requests, "NO", 1);
        */
        //cBuyer.serverThread.interrupt();
        //cSeller.serverThread.interrupt();
        //cOffSeller.serverThread.interrupt();

    }

    @Test
    public void testBuyGoodOwnGoodFailure() throws Exception {
        //new ClientCmdExpectations(Arrays.asList("intentionToSell good3", "buyGood good3 user9", "Exit"), scn);
        assumeTrue("Server is not Up",serverIsUp());
        HdsClient cBuyer = ClientServiceTest.getClient("client9");//new HdsClient(buyer, portBuyer);

        JSONObject intentionToSellRequest = cBuyer.sendJson("intentionToSell good3");
        JSONObject buyRequest = cBuyer.sendJson("buyGood good3 user9");

        isSigned(intentionToSellRequest, "assymetricKeys/user9.pub");
        isSigned(buyRequest, "assymetricKeys/user9.pub");

        JSONObject answerITSRequest = cBuyer.intentionToSell(intentionToSellRequest);
        isSigned(answerITSRequest, cBuyer.serverPublicKey);
        checkAnswer(answerITSRequest, "YES");

        JSONObject answerBuyGoodRequest = cBuyer.buyGood(buyRequest);
        isSigned(answerBuyGoodRequest, cBuyer.serverPublicKey);
        checkAnswer(answerBuyGoodRequest, "NO");

        /*cBuyer.runCommands();


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
        //cBuyer.serverThread.interrupt();*/

    }

    @Test
    public void testBuyGoodSuccess() throws Exception {
        //new ClientCmdExpectations(Arrays.asList("intentionToSell good12", "Exit", "buyGood good12 user10", "Exit"), scn);
        assumeTrue("Server is not Up",serverIsUp());

        HdsClient dBuyer = ClientServiceTest.getClient("client1");//new HdsClient(buyer, portBuyer);
        HdsClient dSeller = ClientServiceTest.getClient("client10");//new HdsClient(seller, portSeller);

        JSONObject intentionToSellRequest = dSeller.sendJson("intentionToSell good12");
        JSONObject buyRequest = dBuyer.sendJson("buyGood good12 user10");

        isSigned(intentionToSellRequest, "assymetricKeys/user10.pub");
        isSigned(buyRequest, "assymetricKeys/user1.pub");

        JSONObject answerITSRequest = dSeller.intentionToSell(intentionToSellRequest);
        isSigned(answerITSRequest, dSeller.serverPublicKey);
        checkAnswer(answerITSRequest, "YES");

        JSONObject answerBuyGoodRequest = dBuyer.buyGood(buyRequest);
        isSigned(answerBuyGoodRequest, dSeller.serverPublicKey);
        checkAnswer(answerBuyGoodRequest, "YES");

        JSONObject gsRequest = dBuyer.sendJson("getStateOfGood good12");
        isSigned(gsRequest, "assymetricKeys/user1.pub");

        JSONObject answerGSRequest = dBuyer.getStateOfGood(gsRequest);
        isSigned(answerGSRequest, dSeller.serverPublicKey);
        checkGood(answerGSRequest, "user1", "good12", "false");


        //checkGood
        // intentionToSell chain
        //Assert.assertEquals(8,dSeller.requests.length());
        //commandSignatureChecker(dSeller.requests, dSeller.serverPublicKey, "user10.pub", 0);
        //intentionToSellChecker(dSeller.requests, "YES", 3);

        // TransferGood chain
        //commandSignatureChecker(dSeller.requests, dSeller.serverPublicKey, "user10.pub","user1.pub", 4);
        //intentionToSellChecker(dSeller.requests, "YES", 7);

        // BuyGood chain
        //Assert.assertEquals(2,dBuyer.requests.length());
        //isSigned(dBuyer.requests.getJSONObject(0), "assymetricKeys/user1.pub");
        //intentionToSellChecker(dBuyer.requests, "YES", 1);
        //cBuyer.serverThread.interrupt();
        //cSeller.serverThread.interrupt();
    }

    @Test
    public void testBuyGoodFailure() throws Exception {
        //new ClientCmdExpectations(Arrays.asList("buyGood good15 user7", "Exit"), scn);
        assumeTrue("Server is not Up",serverIsUp());
        /*String buyer = "user1";
        String seller = "user7";
        int portBuyer = 3999+1;
        int portSeller = 3999+7;*/

        HdsClient fBuyer = ClientServiceTest.getClient("client1");//new HdsClient(buyer, portBuyer);
        HdsClient fSeller = ClientServiceTest.getClient("client7");//new HdsClient(seller, portSeller);
        JSONObject buyRequest = fBuyer.sendJson("buyGood good15 user7");
        JSONObject answerRequest = fBuyer.buyGood(buyRequest);
        isSigned(buyRequest, "assymetricKeys/user1.pub");
        isSigned(answerRequest, fSeller.serverPublicKey);
        checkAnswer(answerRequest, "NO");
        //fBuyer.runCommands();

        // TransferGood chain
        //Assert.assertEquals(4,fSeller.requests.length());
        //commandSignatureChecker(fSeller.requests, fSeller.serverPublicKey, "user7.pub","user1.pub", 0);
        //intentionToSellChecker(fSeller.requests, "NO", 3);

        // BuyGood chain
        //Assert.assertEquals(2,fBuyer.requests.length());
        //isSigned(fBuyer.requests.getJSONObject(0), "assymetricKeys/user1.pub");
        //intentionToSellChecker(fBuyer.requests, "NO", 1);
        //The good was not for sale.
        //cBuyer.serverThread.interrupt();
        //cSeller.serverThread.interrupt();
    }

    @Test
    public void testBuyGoodDoesNotExist(@Mocked final Scanner scn) throws Exception {
        //new ClientCmdExpectations(Arrays.asList("buyGood good50 user6", "Exit"), scn);
        assumeTrue("Server is not Up",serverIsUp());

        HdsClient gBuyer = ClientServiceTest.getClient("client1");//new HdsClient(buyer, portBuyer);
        HdsClient gSeller = ClientServiceTest.getClient("client6");//new HdsClient(seller, portSeller);

        JSONObject buyRequest = gBuyer.sendJson("buyGood good50 user6");
        JSONObject answerRequest = gBuyer.buyGood(buyRequest);
        isSigned(buyRequest, "assymetricKeys/user1.pub");
        isSigned(answerRequest, gSeller.serverPublicKey);
        checkAnswer(answerRequest, "NO");

        //gBuyer.runCommands();

        // TransferGood chain
        //Assert.assertEquals(4,gSeller.requests.length());
        //commandSignatureChecker(gSeller.requests, gSeller.serverPublicKey, "user6.pub","user1.pub", 0);
        //intentionToSellChecker(gSeller.requests, "NO", 3);

        // BuyGood chain
        //Assert.assertEquals(2,gBuyer.requests.length());
        //isSigned(gBuyer.requests.getJSONObject(0), "assymetricKeys/user1.pub");
        //intentionToSellChecker(gBuyer.requests, "NO", 1);
        //cBuyer.serverThread.interrupt();
        //cSeller.serverThread.interrupt();
    }

}


