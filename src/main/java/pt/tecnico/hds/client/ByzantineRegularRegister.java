package pt.tecnico.hds.client;

import org.json.JSONObject;
import pt.tecnico.hds.client.exception.HdsClientException;
import java.util.ArrayList;
import java.util.List;

public class ByzantineRegularRegister extends ByzantineRegister {
    List<RegisterValue> _readList;

    public ByzantineRegularRegister(HdsClient _client){
        super(_client);
        _readList = new ArrayList<>();
    }

    public void checkSignature(boolean bool, String answerS) throws HdsClientException {
        if (bool)
            client.checkSignature(answerS);
    }

    public JSONObject write(JSONObject request, boolean doCheckSignature) throws HdsClientException {
        String answerS ="";
        String auxS;

        answerS = sendWritingToReplicas(request);


        if (client._register._acks.size() > (client.NREPLICAS + Main.f)/2) {
            client._register._acks.clear();
            checkSignature(doCheckSignature,answerS);
            return new JSONObject(answerS);
        }
        return null;
    }

    /**
     * This method is only for tests, it's a simulator for a binzantine client.
     * @param responses
     * @param request
     */
    /*public void sendEvilMessage(AnswerThread responses[], JSONObject request){
        for (int i=0;i< client.NREPLICAS;i++) {
            if (i == 0) {
                responses[i] = new AnswerThread(i, client, client.sendJson("IntentionToSell good17"));
            }
            else {
                responses[i] = new AnswerThread(i, client, request);
            }
            responses[i].start();
        }
        for (int i=0;i< client.NREPLICAS;i++) {
            try {
                responses[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }*/

    public void sendMessages(AnswerThread responses[], JSONObject request){
        for (int i=0;i< client.NREPLICAS;i++) {
            responses[i] = new AnswerThread(i, client, request);
            responses[i].start();
        }
        for (int i=0;i< client.NREPLICAS;i++) {
            try {
                responses[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public String sendWritingToReplicas(JSONObject request) {
        String answerS = "";
        AnswerThread responses[] = new AnswerThread[Main.replicas];

        sendMessages(responses, request);

        for (int i=0;i< client.NREPLICAS;i++) {
            if (responses[i].auxS != null) {
                answerS = responses[i].auxS;
                RegisterValue r = new RegisterValue(new JSONObject(answerS));
                if(getWts() == r.getTimestamp())
                    _acks.add(r);
            }
        }
        return answerS;
    }




    public void sendReadingToReplicas(JSONObject request) throws HdsClientException {
        String answerS = "";
        AnswerThread responses[] = new AnswerThread[Main.replicas];
        sendMessages(responses, request);

        for (int i=0;i< client.NREPLICAS;i++) {
            if (responses[i].auxS != null) {
                answerS = responses[i].auxS;
                client.checkSignature(answerS);

                RegisterValue r = new RegisterValue(new JSONObject(answerS));
                if(r.verifySignature() && getRid() == r.getRid())
                    _readList.add(r);
            }
        }
    }


    public JSONObject read(JSONObject request) throws HdsClientException {
        _readList.clear();
        sendReadingToReplicas(request);
        if (_readList.size() > (client.NREPLICAS + Main.f)/2) {
            return getHighestValueReadList();
        }
        return null;

    }

    public JSONObject getHighestValueReadList() {
        long max = Integer.MIN_VALUE;
        JSONObject maxO = null;
        List<RegisterValue> readList = _readList;
        for(int i=0; i< readList.size(); i++){
            if(readList.get(i).getTimestamp() > max){
                max = readList.get(i).getTimestamp();
                maxO = readList.get(i).getMessage();
            }
        }
        return maxO;
    }
}
