import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import pt.tecnico.hds.client.HdsClient;

import static org.mockito.Mockito.*;

import static org.junit.Assume.assumeTrue;

@RunWith(MockitoJUnitRunner.class)
public class FaultToleranceTest extends BaseTest {

    public FaultToleranceTest() {
        super();
    }

    @Test
    public void testOneServerDownFirstFails() throws Exception {
        assumeTrue("Server is not Up", serverIsUp());
        HdsClient c = ClientServiceTest.getClient("client5");
        HdsClient t = spy(c);
        when(t.connectToClient(anyString(), anyInt(), any())).thenAnswer(
                new Answer() {
                    int i = 0;
                    public Object answer(InvocationOnMock invocation)throws Throwable {
                        if (i++ == 0)
                            return null;
                        return invocation.callRealMethod();
                    }
                });
        JSONObject intentionToSellRequest = t.sendJson("intentionToSell good1");
        JSONObject answerITS = t.intentionToSell(intentionToSellRequest);
        checkAnswer(answerITS, "YES");
    }

    @Test
    public void testOneServerDownMiddleFails() throws Exception {
        assumeTrue("Server is not Up", serverIsUp());
        HdsClient c = ClientServiceTest.getClient("client5");
        HdsClient t = spy(c);
        when(t.connectToClient(anyString(), anyInt(), any())).thenAnswer(
                new Answer() {
                    int i = 0;
                    public Object answer(InvocationOnMock invocation)throws Throwable {
                        if (i++ == 1)
                            return null;
                        return invocation.callRealMethod();
                    }
                });
        JSONObject intentionToSellRequest = t.sendJson("intentionToSell good1");
        JSONObject answerITS = t.intentionToSell(intentionToSellRequest);
        checkAnswer(answerITS, "YES");
    }

    @Test
    public void testOneServerDownLastFails() throws Exception {
        assumeTrue("Server is not Up", serverIsUp());
        HdsClient c = ClientServiceTest.getClient("client5");
        HdsClient t = spy(c);
        when(t.connectToClient(anyString(), anyInt(), any())).thenAnswer(
                new Answer() {
                    int i = 0;
                    public Object answer(InvocationOnMock invocation) throws Throwable {
                        if (i++ == t.NREPLICAS-1)
                            return null;
                        return invocation.callRealMethod();
                    }
                });
        JSONObject intentionToSellRequest = t.sendJson("intentionToSell good1");
        JSONObject answerITS = t.intentionToSell(intentionToSellRequest);
        checkAnswer(answerITS, "YES");
    }

    @Test
    public void oneFaultGetStateOfGoodAfterwardsFails() throws Exception {
        assumeTrue("Server is not Up", serverIsUp());
        HdsClient c = ClientServiceTest.getClient("client5");
        HdsClient t = spy(c);
        when(t.connectToClient(anyString(), anyInt(), any())).thenAnswer(
                new Answer() {
                    int i = 0;
                    public Object answer(InvocationOnMock invocation) throws Throwable {
                        if (i++ == 0)
                            return null;
                        return invocation.callRealMethod();
                    }
                });
        JSONObject intentionToSellRequest = t.sendJson("intentionToSell good1");
        JSONObject answerITS = t.intentionToSell(intentionToSellRequest);
        checkAnswer(answerITS, "YES");
        JSONObject getStateOfGoodRequest = t.sendJson("getStateOfGood good1");
        JSONObject answerGSOGR = t.getStateOfGood(getStateOfGoodRequest);
        checkGood(answerGSOGR, t._name, "good1", "true");
        InOrder inOrder = inOrder(t);
        for (int i = 0; i < t.NREPLICAS; i++) {
            inOrder.verify(t).connectToClient("localhost", t._baseServerPort+i, intentionToSellRequest);
        }

        for (int i = 0; i < t.NREPLICAS; i++) {
            inOrder.verify(t).connectToClient("localhost", t._baseServerPort+i, getStateOfGoodRequest);
        }
    }

}
