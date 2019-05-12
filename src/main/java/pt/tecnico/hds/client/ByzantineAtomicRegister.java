package pt.tecnico.hds.client;

import org.json.JSONObject;
import pt.tecnico.hds.client.exception.HdsClientException;

public class ByzantineAtomicRegister extends ByzantineRegularRegister {

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

    void executeWriteBack(JSONObject request) throws HdsClientException {
        write(request, true);
    }

    @Override
    public JSONObject read(JSONObject request) throws HdsClientException {
        _readList.clear();
        sendToNReplicas(request);
        if (_readList.size() > (client.NREPLICAS + Main.f)/2) {
            RegisterValue highest = getHighestValueTsPair();
            executeWriteBack(buildWriteBackRequest(highest));
            return highest.getValue();
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
