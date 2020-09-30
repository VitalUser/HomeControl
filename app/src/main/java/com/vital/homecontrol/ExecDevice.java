package com.vital.homecontrol;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public class ExecDevice implements Parcelable {
    private static final String TAG = "MyclassExecDevice";
    private int mDevNum;
    private byte mOutState;
    private int mOutCount;
    private String[] mLampText;
    private ArrayList<Byte> mem;

    public ExecDevice(int devNum, byte outState, int outCount){
        this.mDevNum = devNum;
        this.mOutState = outState;
        this.mOutCount = outCount;
        this.mLampText = new String[outCount];
        Arrays.fill(this.mLampText, "");
        mem = new ArrayList<>();
    }

    public ExecDevice(Parcel in) {
        int[] data = new int[2];
        in.readIntArray(data);
        in.readStringArray(mLampText);
        mDevNum = data[0];
        mOutState = (byte) data[1];
    }

//    public void setDevNum(int devNum){
//        mDevNum = devNum;
//    }

    public void setOutState(byte outState){
        Log.i(TAG, " setOutState: "+outState );
        mOutState = outState;
    }

    public int getDevNum(){
        return mDevNum;
    }

    public byte getOutState(){
        return mOutState;
    }

    public int getOutCount(){
        return this.mOutCount;
    }

    public String getLampText(int index){
        if (index<this.mLampText.length){
            return this.mLampText[index];
        }else{
            return "";
        }
    }

    public void setLampText(int index, String lampText){
        if (index<this.mLampText.length){
            this.mLampText[index]=lampText;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeIntArray(new int[]{mDevNum, mOutState});
        parcel.writeArray(mLampText);
    }

    public static final Creator<ExecDevice> CREATOR = new Creator<ExecDevice>() {
        @Override
        public ExecDevice createFromParcel(Parcel in) {
            return new ExecDevice(in);
        }

        @Override
        public ExecDevice[] newArray(int size) {
            return new ExecDevice[size];
        }
    };

    public void addMem(byte[] inbuf){
        for (byte b : inbuf) {
            this.mem.add(this.mem.size(), b);
        }
    }

    public int getNumCmd(int index){
        int offs = 1 + index*5;
        if ((offs+1)<this.mem.size()){
            return (this.mem.get(offs) & 0xFF) * 0x100 + (this.mem.get(offs+1) & 0xFF);
        }else {
            return 0;
        }
    }

    public String getCmdParamStr(int index){
        int offs = 1 + index*5;
        if ((offs+4)<this.mem.size()){
            return String.format(Locale.getDefault(), "%02x%02x%02x", this.mem.get(offs+2), this.mem.get(offs+3), this.mem.get(offs+4));
        }else {
            return "000000";
        }
    }

    public void clearMem(){
        this.mem.clear();
    }

    public boolean memEmpty(){
        return this.mem.size()==0;
    }

    public int getCmdCount(){
        if (this.mem.size()>0){
            return mem.get(0);
        }else{
            return 0;
        }
    }

}
