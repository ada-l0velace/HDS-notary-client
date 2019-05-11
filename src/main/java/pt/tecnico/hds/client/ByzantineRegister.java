package pt.tecnico.hds.client;

import org.json.JSONObject;
import pt.tecnico.hds.client.exception.HdsClientException;

import java.util.ArrayList;
import java.util.List;

public abstract class ByzantineRegister {
    HdsClient client;
    public long getRid() {
        return _rid;
    }
    long _rid;

    public long getWts() {
        return _wts;
    }
    long _wts;
    List<RegisterValue> _acks;
    List<RegisterValue> _readList;

    public ByzantineRegister(HdsClient _client) {
        client = _client;
        _readList = new ArrayList<>();
        _acks = new ArrayList<>();
        _rid = 0;
        _wts = 0;
    }

    abstract JSONObject write(JSONObject request, boolean doCheckSignature) throws HdsClientException;
    abstract JSONObject read(JSONObject request) throws HdsClientException;
}
