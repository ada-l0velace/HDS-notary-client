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


        for (int i=0;i< client.NREPLICAS;i++) {
            auxS = client.connectToClient("localhost",
                    client._serverPort + i,
                    request);

            if (auxS != null) {
                answerS = auxS;
                RegisterValue r = new RegisterValue(new JSONObject(answerS));
                System.out.println("---------------");
                System.out.println(getWts());
                System.out.println("---------------");
                System.out.println(r.getTimestamp());
                System.out.println("---------------");
                System.out.println(answerS);
                if(getWts() == r.getTimestamp())
                    client._register._acks.add(r);
            }
        }

        if (client._register._acks.size() > (client.NREPLICAS + Main.f)/2) {
            client._register._acks.clear();
            checkSignature(doCheckSignature,answerS);
            return new JSONObject(answerS);
        }
        return null;
    }

    public JSONObject read(JSONObject request) throws HdsClientException {
        String answerS = "";
        String auxS;
        client._register._rid++;
        List<RegisterValue> readList = _readList;
        readList.clear();

        for (int i=0;i< client.NREPLICAS;i++) {
            auxS = client.connectToClient("localhost", client._serverPort+i, request);
            if (auxS != null) {
                answerS = auxS;
                client.checkSignature(answerS);
                RegisterValue r = new RegisterValue(new JSONObject(answerS));
                if (r.verifySignature())
                    readList.add(r);
            }

        }
        if (readList.size() > (client.NREPLICAS + Main.f)/2) {
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
