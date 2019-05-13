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
        if (r.getTimestamp() > _wts) {
            _wts = r.getTimestamp();
        }

        JSONObject writeBack = new JSONObject();
        writeBack.put("Action","WriteBack");
        writeBack.put("Timestamp", new java.util.Date().getTime());
        writeBack.put("t", r.getTimestamp());
        writeBack.put("v",r.getValue());
        writeBack.put("signer", client._name);
        highestValue = r;
        return client.buildFinalMessage(writeBack.toString(), new JSONObject());
    }



    JSONObject executeWriteBack(JSONObject request) throws HdsClientException {
        return writeBack(request, true);
    }

    public String sendWrittingBackToReplicas(JSONObject request) throws HdsClientException {
        String answerS = "";
        String auxS;


        System.out.println("-----------------------");
        System.out.println(request.toString());
        System.out.println("-----------------------");
        for (int i=0;i< client.NREPLICAS;i++) {
            auxS = client.connectToClient("localhost",
                    client._serverPort + i,
                    request);

            if (auxS != null) {
                answerS = auxS;
                RegisterValue r = new RegisterValue(new JSONObject(answerS));

                /*System.out.println("##################");
                System.out.println(getWts());
                System.out.println(r.getTimestamp());
                System.out.println(r.getMessage());
                System.out.println("###################");*/
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
        String answerS = sendWrittingBackToReplicas(request);
        return deliver(answerS, doCheckSignature);
    }


        @Override
    public JSONObject write(JSONObject request, boolean doCheckSignature) throws HdsClientException {
        _acks.clear();
        String answerS = sendWrittingToReplicas(request);
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
