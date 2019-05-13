package pt.tecnico.hds.client;

import org.json.JSONObject;
import pt.tecnico.hds.client.exception.HdsClientException;

public class ByzantineAtomicRegister extends ByzantineRegularRegister {

    Boolean reading;
    RegisterValue highestValue;
    public ByzantineAtomicRegister(HdsClient _client) {
        super(_client);
        reading = false;

    }

    JSONObject buildWriteBackRequest(RegisterValue r) {
        /*if (r.getTimestamp() > _wts) {
            _wts = r.getTimestamp();
        }*/

        JSONObject writeBack = new JSONObject();
        writeBack.put("Action","WriteBack");
        writeBack.put("Timestamp", new java.util.Date().getTime());
        writeBack.put("t", r.getTimestamp());
        writeBack.put("v",r.getValue());
        writeBack.put("v",r.getValue());
        writeBack.put("rid",r.getRid());
        writeBack.put("signer", client._name);
        highestValue = r;
        return client.buildFinalMessage(writeBack.toString(), new JSONObject());
    }



    JSONObject executeWriteBack(JSONObject request) throws HdsClientException {
        return writeBack(request, true);
    }

    public String sendWritingBackToReplicas(JSONObject request) {
        String answerS = "";
        AnswerThread responses[] = new AnswerThread[Main.replicas];

        sendMessages(responses, request);

        for (int i=0;i< client.NREPLICAS;i++) {
            if (responses[i].auxS != null) {
                answerS = responses[i].auxS;
                RegisterValue r = new RegisterValue(new JSONObject(answerS));
                if (r.getRid() == getRid())
                    _acks.add(r);
            }
        }
        return answerS;
    }

    public JSONObject deliver(String answerS, Boolean doCheckSignature) throws HdsClientException {
        if (_acks.size() > (client.NREPLICAS + Main.f)/2) {
            if (reading) {
                reading = false;
                //System.out.println("WTFFFFFFFFFFFFFFFFF");
                //checkSignature(doCheckSignature, highestValue.getMessage().toString());
                return highestValue.getMessage();
            }
            else {
                checkSignature(doCheckSignature, answerS);
                return new JSONObject(answerS);
            }

        }
        return null;
    }

    public JSONObject writeBack(JSONObject request, boolean doCheckSignature) throws HdsClientException {
        _acks.clear();
        String answerS = sendWritingBackToReplicas(request);
        return deliver(answerS, doCheckSignature);
    }


        @Override
    public JSONObject write(JSONObject request, boolean doCheckSignature) throws HdsClientException {
        _acks.clear();
        String answerS = sendWritingToReplicas(request);
        return deliver(answerS, doCheckSignature);
    }

    @Override
    public JSONObject read(JSONObject request) throws HdsClientException {

        _readList.clear();
        reading = true;

        sendReadingToReplicas(request);

        if (_readList.size() > (client.NREPLICAS + Main.f)/2) {
            RegisterValue highest = getHighestValueTsPair();
            return executeWriteBack(buildWriteBackRequest(highest));
        }
        return null;
    }

    RegisterValue getHighestValueTsPair() {
        long max = Integer.MIN_VALUE;
        RegisterValue highest = null;
        for(int i=0; i< _readList.size(); i++){
            if(_readList.get(i).getTimestamp() > max){
                highest = _readList.get(i);
                max =  highest.getTimestamp();
            }
        }
        return highest;
    }
}
