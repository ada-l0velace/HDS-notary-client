package pt.tecnico.hds.client;

import java.util.ArrayList;
import java.util.List;

public class HdsRegister {

    RegisterValue _v;
    long _rid;
    List<RegisterValue> _acks;
    List<RegisterValue> _readList;


    public HdsRegister(){
        _readList = new ArrayList<RegisterValue>();
        _acks = new ArrayList<RegisterValue>();
        _rid = 0;
        //v = new RegisterValue();
    }

    public void deliveryWrite(RegisterValue v) {
        if (_v.getTimestamp() >  v.getTimestamp())
            _v = v;

        //send ACK to client
    }


}
