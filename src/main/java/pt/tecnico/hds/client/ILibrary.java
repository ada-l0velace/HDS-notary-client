package pt.tecnico.hds.client;

import org.json.JSONObject;

public interface ILibrary {
    public JSONObject getStateOfGood(JSONObject request) throws Exception;
    public JSONObject buyGood(JSONObject request) throws Exception;
    public JSONObject intentionToSell(JSONObject request) throws Exception;
    public JSONObject transferGood(JSONObject request) throws Exception;
}
