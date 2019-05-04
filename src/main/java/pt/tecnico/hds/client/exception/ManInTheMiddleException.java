package pt.tecnico.hds.client.exception;

import org.json.JSONObject;

public class ManInTheMiddleException extends HdsClientException {

    private static final long serialVersionUID = 1L;

    private JSONObject _request;

    public ManInTheMiddleException(JSONObject request) {
        _request = request;
    }

    public JSONObject getReplayedRequest() {
        return _request;
    }

    @Override
    public String getMessage() {
        return "The request " + getReplayedRequest().toString() + " was modified in the middle of the connection";
    }
}