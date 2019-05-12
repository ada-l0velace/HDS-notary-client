import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import pt.tecnico.hds.client.HdsClient;
import pt.tecnico.hds.client.exception.HdsClientException;
import pt.tecnico.hds.client.exception.ManInTheMiddleException;
import pt.tecnico.hds.client.exception.ReplayAttackException;

import static org.junit.Assume.assumeTrue;

@RunWith(MockitoJUnitRunner.class)
public class SecurityTestCase extends BaseTest {

    public SecurityTestCase () {
        super();
    }

    @Test
    public void testIfClientIsSigningTheMessage() {
        HdsClient h = ClientServiceTest.getClient("client1");//new HdsClient("user1", port);
        JSONObject response = h.sendJson("getStateOfGood good1");
        isSigned(response, h.getPublicKeyPath());
    }

    @Test
    public void testManInTheMiddleAttackIntentionToSell() throws Exception {
        assumeTrue("Server is not Up",serverIsUp());
        HdsClient cSeller = ClientServiceTest.getClient("client1");//new HdsClient("user1", 3999+1);
        JSONObject requestIST = cSeller.sendJson("intentionToSell good7");
        JSONObject mitmIST = new JSONObject(requestIST.getString("Message"));
        mitmIST.put("FUCK", "YOU");
        isSigned(requestIST, cSeller.getPublicKeyPath());
        requestIST.put("Message", mitmIST.toString());
        checkAnswer(cSeller.intentionToSell(requestIST), "NO");
    }

    @Test
    public void testReplayAttackIntentionToSell() throws Exception {
        assumeTrue("Server is not Up",serverIsUp());
        HdsClient cSeller = ClientServiceTest.getClient("client1");//new HdsClient("user1", 3999+1);

        JSONObject replayIST = cSeller.sendJson("intentionToSell good7");
        isSigned(replayIST, cSeller.getPublicKeyPath());

        checkAnswer(cSeller.intentionToSell(replayIST), "YES");
        checkAnswer(cSeller.intentionToSell(replayIST), "NO");
    }

    @Test
    public void testManInTheMiddleAttackBuyGood() throws Exception {
        assumeTrue("Server is not Up",serverIsUp());
        HdsClient cBuyer = ClientServiceTest.getClient("client1");//new HdsClient(buyer, portBuyer);
        HdsClient cSeller = ClientServiceTest.getClient("client2");//new HdsClient("user1", 3999+1);

        JSONObject requestIST = cSeller.sendJson("intentionToSell good20");
        isSigned(requestIST, cSeller.getPublicKeyPath());
        checkAnswer(cSeller.intentionToSell(requestIST), "YES");

        JSONObject mitmBuyGood = cBuyer.sendJson("buyGood good11 "+ cSeller._name);
        isSigned(mitmBuyGood, cBuyer.getPublicKeyPath());

        String a = mitmBuyGood.getString("Message");
        JSONObject mitm = new JSONObject(a);
        mitm.put("wtf", "yeah");
        mitmBuyGood.put("Message", mitm.toString());
        checkAnswer(cBuyer.buyGood(mitmBuyGood), "NO");
    }

    @Test
    public void testReplayAttackBuyGood() throws Exception {
        assumeTrue("Server is not Up",serverIsUp());
        HdsClient cBuyer = ClientServiceTest.getClient("client1");//new HdsClient(buyer, portBuyer);
        HdsClient cSeller = ClientServiceTest.getClient("client2");//new HdsClient(seller, portSeller);

        // Seller sells the item
        JSONObject ITSRequest = cSeller.sendJson("intentionToSell good20");
        isSigned(ITSRequest, "assymetricKeys/user2.pub");

        JSONObject answerITS = cSeller.intentionToSell(ITSRequest);
        checkAnswer(answerITS, "YES");

        // Buyer buys the item
        JSONObject replayAttackJson = cBuyer.sendJson("buyGood good20 "+ cSeller._name);
        isSigned(replayAttackJson, "assymetricKeys/user1.pub");

        JSONObject answerBuyGood = cBuyer.buyGood(replayAttackJson);
        checkAnswer(answerBuyGood, "YES");

        // Buyer puts the item for sale
        JSONObject ITSRequest2 = cBuyer.sendJson("intentionToSell good20");
        isSigned(ITSRequest2, "assymetricKeys/user1.pub");

        JSONObject answerITS2 = cBuyer.intentionToSell(ITSRequest2);
        checkAnswer(answerITS2, "YES");

        // Seller buys back the item
        JSONObject buyGoodRequest = cSeller.sendJson("buyGood good20 "+ cBuyer._name);
        isSigned(buyGoodRequest, "assymetricKeys/user2.pub");

        JSONObject answerBuyGood2 = cSeller.buyGood(buyGoodRequest);
        checkAnswer(answerBuyGood2, "YES");

        // Buyer tries to replay an old request
        JSONObject answerBuyGoodReplay = cBuyer.buyGood(replayAttackJson);
        checkAnswer(answerBuyGoodReplay, "NO");
    }

    @Test
    public void testManInTheMiddleAttackGetStateOfGood() throws Exception {
        assumeTrue("Server is not Up",serverIsUp());
        HdsClient cSeller = ClientServiceTest.getClient("client1");//new HdsClient("user1", 3999+1);
        JSONObject GSOGRequest = cSeller.sendJson("getStateOfGood good7");
        isSigned(GSOGRequest, "assymetricKeys/user1.pub");
        JSONObject answerGSOG = cSeller.getStateOfGood(GSOGRequest);
        checkGood(answerGSOG, "user1","good7", "false");
        GSOGRequest.put("Message", cSeller.sendJson("getStateOfGood good1").getString("Message"));
        JSONObject MITMAttack = cSeller.getStateOfGood(GSOGRequest);
        checkAnswer(MITMAttack, "NO");
    }

    @Test
    public void testReplayAttackGetStateOfGood() throws Exception {
        assumeTrue("Server is not Up",serverIsUp());
        HdsClient cSeller = ClientServiceTest.getClient("client1");//new HdsClient("user1", 3999+1);

        JSONObject GSOGRequest = cSeller.sendJson("getStateOfGood good7");
        isSigned(GSOGRequest, "assymetricKeys/user1.pub");
        JSONObject answerGSOG = cSeller.getStateOfGood(GSOGRequest);
        checkGood(answerGSOG, "user1","good7", "false");
        JSONObject replayAttack = cSeller.getStateOfGood(GSOGRequest);
        checkAnswer(replayAttack, "NO");
    }

    @Test
    public void testManInTheMiddleTransferGood() throws Exception {
        assumeTrue("Server is not Up",serverIsUp());

        HdsClient cBuyer = ClientServiceTest.getClient("client1");//new HdsClient(buyer, portBuyer);
        HdsClient cSeller = ClientServiceTest.getClient("client2");//new HdsClient(seller, portSeller);

        checkAnswer(cSeller.intentionToSell(cSeller.sendJson("intentionToSell good20")),"YES");
        checkAnswer(cSeller.intentionToSell(cSeller.sendJson("intentionToSell good11")),"YES");

        JSONObject j0 = cBuyer.sendJson("buyGood good20 "+ cSeller._name);
        JSONObject mitmJson =cSeller.sendJson("transferGood good20 "+ cBuyer._name, j0);

        //doing the man in the middle
        JSONObject manInTheMiddleJson = new JSONObject(mitmJson.getString("Message"));
        manInTheMiddleJson.put("Good","good11");
        mitmJson.put("Message", manInTheMiddleJson.toString());

        // checking the servers answer
        JSONObject serverAnswer = cSeller.transferGood(mitmJson);
        checkAnswer(serverAnswer, "NO");
    }
    /*@Test
    public void testReplayAttackTransferGood() throws Exception {
        assumeTrue("Server is not Up",serverIsUp());

        HdsClient cBuyer = ClientServiceTest.getClient("client1");
        HdsClient cSeller = ClientServiceTest.getClient("client2");
        checkAnswer(cSeller.intentionToSell(cSeller.sendJson("intentionToSell good20")), "YES");

        JSONObject j0 = cBuyer.sendJson("buyGood good20 "+ cSeller._name);
        JSONObject replayJson =cSeller.sendJson("transferGood good20 "+ cBuyer._name, j0);

        // Checking the servers answer
        checkAnswer(cSeller.transferGood(replayJson), "YES");

        // Buyer puts the item for sale
        checkAnswer(cBuyer.intentionToSell(cBuyer.sendJson("intentionToSell good20")),"YES");

        // Buying item back
        j0 = cSeller.sendJson("buyGood good20 "+ cBuyer._name);
        //System.out.println(j0.toString());
        JSONObject newJson =cBuyer.sendJson("transferGood good20 "+ cSeller._name, j0);

        checkAnswer(cSeller.transferGood(newJson), "YES");

        // Seller sets item to sell again
        checkAnswer(cSeller.intentionToSell(cSeller.sendJson("intentionToSell good20")), "YES");

        // replay attack
        checkAnswer(cSeller.transferGood(replayJson), "NO");
    }*/
    
    @Test(expected=ManInTheMiddleException.class)
    public void testServerResponseManInTheMiddle() throws HdsClientException {
        assumeTrue("Server is not Up",serverIsUp());
        HdsClient cSeller = ClientServiceTest.getClient("client2");
        JSONObject serverAnswer = cSeller.intentionToSell(cSeller.sendJson("intentionToSell good20"));
        JSONObject message = new JSONObject(serverAnswer.getString("Message"));
	    message.put("Good", "w");
        serverAnswer.put("Message", message.toString());

	    cSeller.validateServerRequest(serverAnswer);
    }

    @Test(expected = ReplayAttackException.class)
    public void testServerReplayAttack() throws HdsClientException {
        assumeTrue("Server is not Up",serverIsUp());
        HdsClient cSeller = ClientServiceTest.getClient("client2");
        JSONObject serverAnswer = cSeller.intentionToSell(cSeller.sendJson("intentionToSell good20"));
        Assert.assertTrue("Something is wrong the server is not signing well", cSeller.validateServerRequest(serverAnswer));
        cSeller.validateServerRequest(serverAnswer);
    }

}
