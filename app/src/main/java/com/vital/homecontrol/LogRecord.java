package com.vital.homecontrol;

public class LogRecord {

    private final boolean misSend;
    private final byte[] mdata;

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
