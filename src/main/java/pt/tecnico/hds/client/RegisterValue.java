package pt.tecnico.hds.client;

import org.json.JSONObject;

import java.util.Date;

public class RegisterValue {
    JSONObject _value;
    JSONObject _message;
    String _user;
    String _valueSignature;
    long _timestamp;
    int _pid;

    public RegisterValue(JSONObject value){
        if (value.has("Value" ) && value.has("SignatureValue")) {
            _value = new JSONObject(value.getString("Value"));
            _user = _value.getString("signer");
            _valueSignature = value.getString("SignatureValue");
            _timestamp = _value.getLong("wts");
            _pid = _value.getInt("pid");
        }
        _message = value;
    }

    public Boolean verifySignature() {
        if (_user != null && _value != null && _valueSignature != null)
            return Utils.verifySignWithPubKeyFile(_value.toString(), _valueSignature, "assymetricKeys/"+_user+".pub");
        return false;
    }

    public JSONObject getValue(){
        return _value;
    }

    public JSONObject getMessage(){
        return _message;
    }

    public long getTimestamp(){
        return _timestamp;
    }

    @Override
    public String toString() {
        return _timestamp+" "+_pid+ " " + _value.toString()+ " " +_message.getString("Message");
    }

}
