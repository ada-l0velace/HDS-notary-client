package pt.tecnico.hds.client;

import org.json.JSONObject;
import pt.tecnico.hds.plibrary.RequestDto;
import pt.tecnico.hds.plibrary.RequestWriteDto;
import sun.misc.Request;

import java.util.ArrayList;
import java.util.List;

public class HdsRegister {

    RegisterValue _v;
    long _rid;
    long _wts;
    List<RegisterValue> _acks;
    List<RegisterValue> _readList;


    public HdsRegister(){
        _readList = new ArrayList<>();
        _acks = new ArrayList<>();
        _rid = 0;
        _wts = 0;
        //v = new RegisterValue();
    }

    public JSONObject verifySignature(RequestDto dto, HdsClient h){
        if (Utils.verifySignWithPubKeyFile(dto.getValueToSign(), dto.getSignature(),"assymetricKeys/"+h._name+".pub")) {
            return new JSONObject(new JSONObject(dto.getValueToSign()).getString("value"));
        }
        return null;
    }

    public RequestDto sign(HdsClient h, int pid, JSONObject value) {
        _wts += 1;
        JSONObject b = new JSONObject();
        b.put("pId", pid);
        b.put("wts",_wts);
        b.put("value", value.toString());
        RequestDto dto = new RequestWriteDto(b.toString(), Utils.signWithPrivateKey(b.toString(), "assymetricKeys/"+h._name));

        return dto;
    }

    public void deliveryWrite(RegisterValue v) {
        if (_v.getTimestamp() >  v.getTimestamp())
            _v = v;

        //send ACK to client
    }

    public JSONObject getHighestValueReadList() {
        long max = Integer.MIN_VALUE;
        JSONObject maxO = null;
        for(int i=0; i<_readList.size(); i++){
            if(_readList.get(i).getTimestamp() > max){
                max = _readList.get(i).getTimestamp();
                maxO = _readList.get(i).getValue();
            }
        }
        return maxO;
    }


}
