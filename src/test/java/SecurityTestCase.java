import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import pt.tecnico.hds.client.HdsClient;
import pt.tecnico.hds.client.Utils;

import java.util.concurrent.TimeUnit;

import static org.junit.Assume.assumeTrue;


public class SecurityTestCase extends BaseTest {

    public SecurityTestCase (String name) {
        super( name );
    }

    @Test
    public void testIfClientIsSigningTheMessage() {
        int port = 3999+1;
        HdsClient h = new HdsClient("user1", port);
        JSONObject response = h.sendJson("getStateOfGood good1");
        String message = response.getString("Message");
        String signedMessage = response.getString("Hash");
        //String signedMessage = Utils.signWithPrivateKey(message, "assymetricKeys/user1");
        //System.out.println(signedMessage);
        Assert.assertTrue("This message has not been signed by user1 and is subject to attacks.",Utils.verifySignWithPubKeyFile(message, signedMessage, "assymetricKeys/user1.pub"));
    }

    @Test
    public void testManInTheMiddleAttackIntentionToSell() throws Exception {
        assumeTrue("Server is not Up",serverIsUp());
        HdsClient cSeller = new HdsClient("user1", 3999+1);
        JSONObject jsonObj = cSeller.sendJson("intentionToSell good7");
        JSONObject j0 = new JSONObject(jsonObj.getString("Message"));
        j0.put("Good","good21");
        jsonObj.put("Message", j0.toString());
        //System.out.println(j0.toString());
        String serverAnswer = sendTo("localhost", serverPort, jsonObj.toString());
        jsonObj = new JSONObject(serverAnswer);
        jsonObj = new JSONObject(jsonObj.getString("Message"));
        Assert.assertEquals("Message got changed in the middle of the connection, the server isn't validating the requests.","NO", jsonObj.getString("Action"));
    }

    @Test
    public void testReplayAttackIntentionToSell() throws Exception {
        assumeTrue("Server is not Up",serverIsUp());
        HdsClient cSeller = new HdsClient("user1", 3999+1);
        JSONObject jsonObj = cSeller.sendJson("intentionToSell good7");

        String serverAnswer = sendTo("localhost", serverPort, jsonObj.toString());
        JSONObject j0 = new JSONObject(serverAnswer);
        j0 = new JSONObject(j0.getString("Message"));
        Assert.assertEquals("If poodle didn't fuck up something then this should always pass","YES", j0.getString("Action"));
        serverAnswer = sendTo("localhost", serverPort, jsonObj.toString());
        System.out.println(serverAnswer);

        j0 = new JSONObject(serverAnswer);
        j0 = new JSONObject(j0.getString("Message"));
        Assert.assertEquals("Replay attack detected.","NO", j0.getString("Action"));
    }

    @Test
    public void testManInTheMiddleAttackBuyGood() throws Exception {
        assumeTrue("Server is not Up",serverIsUp());
        String buyer = "user1";
        String seller = "user2";
        int portBuyer = 3999+1;
        int portSeller = 3999+2;
        HdsClient cBuyer = new HdsClient(buyer, portBuyer);
        HdsClient cSeller = new HdsClient(seller, portSeller);

        sendTo("localhost", serverPort, cSeller.sendJson("intentionToSell good20").toString());
        JSONObject j0 = cBuyer.sendJson("buyGood good11 "+ cSeller._name);
        JSONObject j1 = new JSONObject(j0.getString("Message"));
        j1.put("Good", "good20");
        //System.out.println(j1.toString()+"||||||||||");
        j0.put("Message",j1.toString());

        String serverAnswer = sendTo("localhost", portSeller, j0.toString());

        JSONObject jsonObj = new JSONObject(serverAnswer);
        jsonObj = new JSONObject(jsonObj.getString("Message"));
        Assert.assertEquals("NO", jsonObj.getString("Action"));
    }

    @Test
    public void testReplayAttackBuyGood() throws Exception {
        assumeTrue("Server is not Up",serverIsUp());
        String buyer = "user1";
        String seller = "user2";
        int portBuyer = 3999+1;
        int portSeller = 3999+2;
        HdsClient cBuyer = new HdsClient(buyer, portBuyer);
        HdsClient cSeller = new HdsClient(seller, portSeller);

        // Seller sells the item
        sendTo("localhost", serverPort, cSeller.sendJson("intentionToSell good20").toString());
        JSONObject replayAttackJson = cBuyer.sendJson("buyGood good20 "+ cSeller._name);
        String serverAnswer = sendTo("localhost", portSeller, replayAttackJson.toString());

        JSONObject jsonObj = new JSONObject(serverAnswer);
        jsonObj = new JSONObject(jsonObj.getString("Message"));
        Assert.assertEquals("YES", jsonObj.getString("Action"));

        // Buyer sells the item back to the seller
        sendTo("localhost", serverPort, cBuyer.sendJson("intentionToSell good20").toString());
        JSONObject j0 = cSeller.sendJson("buyGood good20 "+ cBuyer._name);
        serverAnswer = sendTo("localhost", portBuyer, j0.toString());

        // checks if the transaction is validated
        JSONObject _jsonObj = new JSONObject(serverAnswer);
        _jsonObj = new JSONObject(_jsonObj.getString("Message"));
        Assert.assertEquals("YES", _jsonObj.getString("Action"));

        TimeUnit.SECONDS.sleep(1);
        // Recovering the item
        serverAnswer = sendTo("localhost", serverPort, cSeller.sendJson("intentionToSell good20").toString());
        jsonObj = new JSONObject(serverAnswer);
        jsonObj = new JSONObject(jsonObj.getString("Message"));
        Assert.assertEquals("YES", jsonObj.getString("Action"));

        serverAnswer = sendTo("localhost", portSeller, replayAttackJson.toString());

        // checks if the replay attack was denied
        _jsonObj = new JSONObject(serverAnswer);
        _jsonObj = new JSONObject(_jsonObj.getString("Message"));
        Assert.assertEquals("NO", _jsonObj.getString("Action"));


    }

    @Test
    public void testManInTheMiddleAttackGetStateOfGood() throws Exception {
        assumeTrue("Server is not Up",serverIsUp());
        HdsClient cSeller = new HdsClient("user1", 3999+1);
        JSONObject jsonObj = cSeller.sendJson("getStateOfGood good7");
        JSONObject j0 = new JSONObject(jsonObj.getString("Message"));
        j0.put("Good","good21");
        jsonObj.put("Message", j0.toString());
        //System.out.println(j0.toString());
        String serverAnswer = sendTo("localhost", serverPort, jsonObj.toString());
        jsonObj = new JSONObject(serverAnswer);
        jsonObj = new JSONObject(jsonObj.getString("Message"));
        Assert.assertTrue("The server answer didn't send a refusing action after MITM",jsonObj.has("Action"));
        Assert.assertEquals("Message got changed in the middle of the connection, the server isn't validating the requests.","NO", jsonObj.getString("Action"));
    }

    @Test
    public void testReplayAttackGetStateOfGood() throws Exception {
        assumeTrue("Server is not Up",serverIsUp());
        HdsClient cSeller = new HdsClient("user1", 3999+1);
        JSONObject replayAttackJson = cSeller.sendJson("getStateOfGood good7");
        //System.out.println(j0.toString());
        String serverAnswer = sendTo("localhost", serverPort, replayAttackJson.toString());
        JSONObject jsonObj = new JSONObject(serverAnswer);
        jsonObj = new JSONObject(jsonObj.getString("Message"));
        System.out.println(jsonObj.toString() + "------------");
        Assert.assertFalse(jsonObj.has("Action"));

        // Applying the replay attack
        serverAnswer = sendTo("localhost", serverPort, replayAttackJson.toString());
        jsonObj = new JSONObject(serverAnswer);
        jsonObj = new JSONObject(jsonObj.getString("Message"));
        Assert.assertTrue("The server answer didn't send a refusing action after replay attack", jsonObj.has("Action"));
        Assert.assertEquals("NO", jsonObj.getString("Action"));

    }

    @Test
    public void testManInTheMiddleTransferGood() throws Exception {
        assumeTrue("Server is not Up",serverIsUp());
        String buyer = "user1";
        String seller = "user2";
        int portBuyer = 3999+1;
        int portSeller = 3999+2;
        HdsClient cBuyer = new HdsClient(buyer, portBuyer);
        HdsClient cSeller = new HdsClient(seller, portSeller);

        sendTo("localhost", serverPort, cSeller.sendJson("intentionToSell good20").toString());
        sendTo("localhost", serverPort, cSeller.sendJson("intentionToSell good11").toString());
        JSONObject j0 = cBuyer.sendJson("buyGood good20 "+ cSeller._name);

        JSONObject mitmJson =cSeller.sendJson("transferGood good20 "+ cBuyer._name, j0);


        //System.out.println(a + " -----------");

        //doing the man in the middle
        JSONObject manInTheMiddleJson = new JSONObject(mitmJson.getString("Message"));
        manInTheMiddleJson.put("Good","good11");
        mitmJson.put("Message", manInTheMiddleJson.toString());

        // checking the servers answer
        String serverAnswer = sendTo("localhost", serverPort, mitmJson.toString());
        JSONObject jsonObj = new JSONObject(serverAnswer);
        jsonObj = new JSONObject(jsonObj.getString("Message"));
        Assert.assertEquals("NO", jsonObj.getString("Action"));
    }

    @Test
    public void testReplayAttackTransferGood() throws Exception {
        assumeTrue("Server is not Up",serverIsUp());
        String buyer = "user1";
        String seller = "user2";
        int portBuyer = 3999+1;
        int portSeller = 3999+2;
        HdsClient cBuyer = new HdsClient(buyer, portBuyer);
        HdsClient cSeller = new HdsClient(seller, portSeller);

        sendTo("localhost", serverPort, cSeller.sendJson("intentionToSell good20").toString());
        JSONObject j0 = cBuyer.sendJson("buyGood good20 "+ cSeller._name);

        JSONObject replayJson =cSeller.sendJson("transferGood good20 "+ cBuyer._name, j0);

        // checking the servers answer
        String serverAnswer = sendTo("localhost", serverPort, replayJson.toString());
        JSONObject jsonObj = new JSONObject(serverAnswer);
        jsonObj = new JSONObject(jsonObj.getString("Message"));
        Assert.assertEquals("YES", jsonObj.getString("Action"));

        sendTo("localhost", serverPort, cBuyer.sendJson("intentionToSell good20").toString());

        // Buying item back
        j0 = cSeller.sendJson("buyGood good20 "+ cBuyer._name);

        JSONObject newJson =cBuyer.sendJson("transferGood good20 "+ cSeller._name, j0);

        // checking the servers answer
        serverAnswer = sendTo("localhost", serverPort, newJson.toString());
        jsonObj = new JSONObject(serverAnswer);
        jsonObj = new JSONObject(jsonObj.getString("Message"));
        Assert.assertEquals("YES", jsonObj.getString("Action"));

        // replay attack
        TimeUnit.SECONDS.sleep(3);
        sendTo("localhost", serverPort, cSeller.sendJson("intentionToSell good20").toString());

        // checking the servers answer
        serverAnswer = sendTo("localhost", serverPort, replayJson.toString());

        jsonObj = new JSONObject(serverAnswer);
        jsonObj = new JSONObject(jsonObj.getString("Message"));
        //System.out.println();
        Assert.assertEquals("NO", jsonObj.getString("Action"));

    }

    @Test
    public void testServerMessageIsSigned() throws Exception {
        assumeTrue("Server is not Up",serverIsUp());
        String seller = "user2";
        int portSeller = 3999+2;
        HdsClient cSeller = new HdsClient(seller, portSeller);
        String serverAnswer = sendTo("localhost", serverPort, cSeller.sendJson("intentionToSell good20").toString());
        JSONObject jsonObj = new JSONObject(serverAnswer);
        jsonObj = new JSONObject(jsonObj.getString("Message"));
        Assert.assertEquals("YES", jsonObj.getString("Action"));
        

    }



    }