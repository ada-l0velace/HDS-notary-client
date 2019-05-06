package pt.tecnico.hds.client;

import org.json.JSONObject;

import java.util.Date;

public class RegisterValue {
    JSONObject _value;
    long _timestamp;

    public RegisterValue(JSONObject value){
        _value = value;
        //System.out.println("--------------------------------------------------------");
        //System.out.println();
        //System.out.println("--------------------------------------------------------");
        _timestamp = new JSONObject(value.getString("Message")).getLong("Timestamp");
    }

    public JSONObject getValue(){
        return _value;
    }

    public long getTimestamp(){
        return _timestamp;
    }

}
