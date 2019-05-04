package pt.tecnico.hds.client;

import org.json.JSONObject;
import pt.tecnico.hds.client.exception.HdsClientException;

public interface ILibrary {
    JSONObject getStateOfGood(JSONObject request) throws HdsClientException;
    JSONObject buyGood(JSONObject request) throws HdsClientException;
    JSONObject intentionToSell(JSONObject request) throws HdsClientException;
    JSONObject transferGood(JSONObject request) throws HdsClientException;
}
