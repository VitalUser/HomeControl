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
    private int mType;
    private byte mOutState;
    private byte mLastOutState;
    private int mOutCount;
    private boolean mReadMem;
    private String[] mLampText;

    private String[] mRoomText;
    private int mMemSize;
    private int mMemblock;
    private Byte[] mem;
    private final Byte zero = 0;

    public ExecDevice(int devNum, int devType, byte outState, int outCount){
        this.mDevNum = devNum;
        this.mOutState = outState;
        this.mLastOutState = 0;
        this.mOutCount = outCount;
        this.mReadMem = false;
        this.mLampText = new String[outCount];
        Arrays.fill(this.mLampText, "");
        this.mRoomText = new String[outCount];
        this.mType = devType;
        switch (mType & 0x70){
            case 0x10:
                mMemSize = 64;
                break;
            case 0x20:
                mMemSize = 128;
                break;
            case 0x30:
                mMemSize = 256;
                break;
            case 0x40:
                mMemSize = 512;
                break;
            case 0x50:
                mMemSize = 1024;
                break;
            case 0x60:
                mMemSize = 2048;
                break;
            default:
                mMemSize = 0;
        }
        this.mem = new Byte[mMemSize];
        Arrays.fill(this.mem, zero);

        switch (mType & 0x0C){
            case 0x00:
                mMemblock = mMemSize;
                break;
            case 0x04:
                mMemblock = 16;
                break;
            case 0x08:
                mMemblock = 64;
                break;
            case 0x0C:
                mMemblock = 128;
                break;
            default:
                mMemblock = 0;
        }
    }

    public ExecDevice(Parcel in) {
        int[] data = new int[2];
        in.readIntArray(data);
        in.readStringArray(mLampText);
        mDevNum = data[0];
        mOutState = (byte) data[1];
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

    public String getRoomText(int index){
        if (index<this.mRoomText.length){
            return this.mRoomText[index];
        }else{
            return "";
        }
    }

    public String getFullText(int index){
        if (index<this.mRoomText.length){
            String name = "";
            if (!this.mRoomText[index].equals("")){
                name += this.mRoomText[index] + ", ";
            }
            name += this.mLampText[index];
            return name;
        }else{
            return "";
        }
    }



    public void setLampText(int index, String lampText){
        if (index<this.mLampText.length){
            this.mLampText[index]=lampText;
        }
    }

    public void setRoomText(int index, String roomText){
        if (index<this.mRoomText.length){
            this.mRoomText[index]=roomText;
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

    public void addMem(byte[] inbuf, int addr, int count){
        for (int i = 0; i < count; i++) {
            this.mem[addr+i] = inbuf[i];
        }
    }

    public void writeMem(byte data, int addr){
        if (addr<this.mem.length){
            this.mem[addr]=data;
        }
    }

    public int getNumCmd(int index){
        int offs = 0xB1 + index*5;
        if ((offs+1)<this.mem.length){
            return (this.mem[offs] & 0xFF) * 0x100 + (this.mem[offs+1] & 0xFF);
        }else {
            return 0;
        }
    }

    public String getCmdParamStr(int index){
        int offs = 0xB1 + index*5;
        if ((offs+4)<this.mem.length){
            return String.format(Locale.getDefault(), "%02x%02x%02x", this.mem[offs+2], this.mem[offs+3], this.mem[offs+4]);
        }else {
            return "000000";
        }
    }

    public void setReadMem(boolean state){
        this.mReadMem=state;
    }

    public boolean getReadMem(){
        return this.mReadMem;
    }

    public void clearMem(){
        Arrays.fill(this.mem, zero);
        this.mReadMem = false;
    }

    public boolean memEmpty(){
        return this.mem.length==0;
    }

    public int getCmdCount(){
        if (this.mem.length>0){
            return mem[0xB0];
        }else{
            return 0;
        }
    }

    public byte getLastOutState() {
        return mLastOutState;
    }

    public void setLastOutState(byte LastOutState) {
        this.mLastOutState = LastOutState;
    }

    public int getMemSize(){
        return this.mMemSize;
    }

    public int getMemblock(){
        return this.mMemblock;
    }
}
