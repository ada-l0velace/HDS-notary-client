import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import pt.tecnico.hds.client.HdsClient;
import pt.tecnico.hds.client.Utils;

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
        Assert.assertTrue("This message has not been signed by user1 and is subject to attacks.",Utils.verifySignWithPubKey(message, signedMessage, "assymetricKeys/user1.pub"));
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




}
