package com.vital.homecontrol;

public class LogRecord {

    private boolean misSend;
    private byte[] mdata;

    public LogRecord(byte[] data, boolean isSend){
        mdata = data;
        misSend = isSend;
    }

    public boolean isSend(){
        return misSend;
    }

    public byte[] getData() {
        return mdata;
    }
}
