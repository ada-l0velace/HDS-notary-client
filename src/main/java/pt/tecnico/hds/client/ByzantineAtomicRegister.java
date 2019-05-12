package pt.tecnico.hds.client;

import org.json.JSONObject;
import pt.tecnico.hds.client.exception.HdsClientException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ByzantineAtomicRegister extends ByzantineRegularRegister {
    HashMap<Long, RegisterValue[]> _answers;
    //List<RegisterValue> _answers;

    public ByzantineAtomicRegister(HdsClient _client) {
        super(_client);
        _answers = new HashMap<>();
    }

    @Override
    public JSONObject read(JSONObject request) throws HdsClientException {
        String answerS;
        String auxS;
        JSONObject finalAnswer = null;
        _rid++;
        for (int i=0;i< client.NREPLICAS;i++) {
            auxS = client.connectToClient("localhost", client._serverPort+i, request);
            if(auxS != null) {
                answerS = auxS;
                client.checkSignature(answerS);
                RegisterValue r = new RegisterValue(new JSONObject(answerS));
                if(!_answers.containsKey(r.getTimestamp())) {
                    _answers.put(r.getTimestamp(),new RegisterValue[client.NREPLICAS]);
                    _answers.get(r.getTimestamp())[i] = r;
                }
                else {
                    _answers.get(r.getTimestamp())[i] = r;
                    if(_answers.get(r.getTimestamp()).length > (client.NREPLICAS + Main.f)/2) {
                        if (r.getTimestamp() > 0)
                            _answers.put(r.getTimestamp(),new RegisterValue[client.NREPLICAS]);
                        for (i=0;i< client.NREPLICAS;i++) { /*TODO: ReadComplete*/}
                        finalAnswer = r.getMessage();
                    }
                }

            }
        }

        return finalAnswer;
    }
}
