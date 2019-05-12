package pt.tecnico.hds.client;

import org.json.JSONObject;
import pt.tecnico.hds.client.exception.HdsClientException;

public class ByzantineAtomicRegister extends ByzantineRegularRegister {

    Boolean reading;

    public ByzantineAtomicRegister(HdsClient _client) {
        super(_client);

    }

    JSONObject buildWriteBackRequest(RegisterValue r) {
        JSONObject writeBack = new JSONObject();
        writeBack.put("Action","WriteBack");
        writeBack.put("Timestamp", new java.util.Date().getTime());
        writeBack.put("t", r.getTimestamp());
        writeBack.put("v",r.getValue());
        writeBack.put("signer", client._name);
        return client.buildFinalMessage(writeBack.toString(), new JSONObject());
    }



    JSONObject executeWriteBack(JSONObject request) throws HdsClientException {
        return write(request, true);
    }

    @Override
    public JSONObject write(JSONObject request, boolean doCheckSignature) throws HdsClientException {

        String answerS = sendWrittingToReplicas(request);

        if (_acks.size() > (client.NREPLICAS + Main.f)/2) {
            if (reading) {
                reading = false;
            }
            _acks.clear();
            checkSignature(doCheckSignature, answerS);
            return new JSONObject(answerS);
        }
        return null;

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
        RegisterValue highest = null;
        for(int i=0; i< _readList.size(); i++){
            if(_readList.get(i).getTimestamp() > highest.getTimestamp()){
                highest = _readList.get(i);
            }
        }
        return highest;
    }
}
