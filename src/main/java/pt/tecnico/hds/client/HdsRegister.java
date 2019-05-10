package pt.tecnico.hds.client;

import org.json.JSONObject;
import org.json.JSONString;
import pt.tecnico.hds.plibrary.RequestDto;
import pt.tecnico.hds.plibrary.RequestWriteDto;
import sun.misc.Request;

import java.util.ArrayList;
import java.util.List;

public class HdsRegister {

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


    public HdsRegister(){
        _readList = new ArrayList<>();
        _acks = new ArrayList<>();
        _rid = 0;
        _wts = 0;
        //v = new RegisterValue();
    }

    public JSONObject getHighestValueReadList() {
        long max = Integer.MIN_VALUE;
        JSONObject maxO = null;

        for(int i=0; i<_readList.size(); i++){
            if(_readList.get(i).getTimestamp() > max){
                max = _readList.get(i).getTimestamp();
                maxO = _readList.get(i).getMessage();
            }
        }
        return maxO;
    }


}
