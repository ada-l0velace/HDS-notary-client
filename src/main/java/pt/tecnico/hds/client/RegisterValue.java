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
    long _rid;

    public RegisterValue(JSONObject value){
        if (value.has("Value" ) && value.has("SignatureValue")) {
            _value = new JSONObject(value.getString("Value"));
            _user = _value.getString("signer");
            _valueSignature = value.getString("SignatureValue");
            _timestamp = _value.getLong("wts");

            //_pid = _value.getInt("pid");
        }
        else if(new JSONObject(value.getString("Message")).has("wts")) {
            _timestamp = new JSONObject(value.getString("Message")).getLong("wts");
        }
        if (new JSONObject(value.getString("Message")).has("rid"))
            _rid = new JSONObject(value.getString("Message")).getLong("rid");
        _message = value;
    }

    public Boolean verifySignature() {
        //System.out.println(getPublicKey(_user, _value));
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

    public long getRid(){
        return _rid;
    }

    @Override
    public String toString() {
        return _timestamp+"";
    }

}
