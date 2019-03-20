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




}
