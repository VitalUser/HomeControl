package com.vital.homecontrol;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class ExecDevice implements Parcelable {
    private static final String TAG = "MyclassExecDevice";
    private int mDevNum;
    private byte mOutState;
    private int mOutCount;
    private String[] mLampText;

    public ExecDevice(int devNum, byte outState, int outCount){
        this.mDevNum = devNum;
        this.mOutState = outState;
        this.mOutCount = outCount;
        this.mLampText = new String[outCount];
        for (int i = 0; i <this.mLampText.length ; i++) {
            this.mLampText[i]="";

        }
    }

    public ExecDevice(Parcel in) {
        int[] data = new int[2];
        in.readIntArray(data);
        in.readStringArray(mLampText);
        mDevNum = data[0];
        mOutState = (byte) data[1];
    }

    public void setDevNum(int devNum){
        mDevNum = devNum;
    }

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

}
