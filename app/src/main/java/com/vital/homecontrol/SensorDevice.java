package com.vital.homecontrol;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;


public class SensorDevice implements Parcelable {

    public static final int IS_DS18B20 = 3;
    public static final int IS_SHT21 = 4;
    public static final int IS_BMP180 = 5;

    public static final int IS_TEMP = 1;
    public static final int IS_HUM = 2;
    public static final int IS_PRESS = 3;

    private int mDevNum;
    private int mModel;
    private int mTemp;
    private int mHum;
    private int mPress;
    public List<Byte> mStat = new ArrayList<>();

    public SensorDevice(int devNum, int model){
        mDevNum = devNum;
        mModel = model;
        mTemp = 0xFFFF;
        mHum = 0xFFFF;
        mPress = 0xFFFF;
        mStat.clear();
    }

    public SensorDevice(Parcel in) {
        int[] data = new int[5];
        in.readIntArray(data);
        mDevNum = data[0];
        mModel = data[1];
        mTemp = data[2];
        mHum = data[3];
        mPress = data[4];
    }

    public int getNum(){
        return mDevNum;
    }

    public int getModel(){
        return mModel;
    }

    public void setNum(int num){
        mHum = num;
    }

    public void setModel(int model){
        mModel = model;
    }

    public int getTemp(){
        return mTemp;
    }

    public int getHum(){
        return mHum;
    }

    public int getPress(){
        return mPress;
    }

    public void setTemp(int temp){
        mTemp=temp;
    }

    public void setHum(int hum){
        mHum=hum;
    }

    public void setPress(int press){
        mPress=press;
    }

    public void setData(byte[] indData){
//        int count = (indData[2] & 0xFF)-3;
//        byte[] data = Arrays.copyOfRange(indData, 5, count+5);
        switch (mModel){
            case IS_DS18B20:
                this.mTemp = ((indData[0]&0xFF)<<8)|(indData[1]&0xFF);
                break;
            case IS_SHT21:
                this.mTemp = ((indData[0]&0xFF)<<8)|(indData[1]&0xFF);
                this.mHum = ((indData[2]&0xFF)<<8)|(indData[3]&0xFF);
                break;

            case IS_BMP180:
                this.mTemp = ((indData[0]&0xFF)<<8)|(indData[1]&0xFF);
                this.mPress = ((indData[2]&0xFF)<<16)|((indData[3]&0xFF)<<8)|(indData[4]&0xFF);
                break;
        }
    }


    public String modelToString(int model){
        switch (model){
            case IS_DS18B20:
                return "DS18B20";
            case IS_SHT21:
                return "SHT21";
            case IS_BMP180:
                return "BMP180";
            default:
                return "???";
        }
    }


    public String getModelString(){
        return modelToString(mModel);
    }

    public String getValue(int sensType){
        switch (sensType){
            case IS_TEMP:
                if (mTemp!=0xFFFF){
                    switch (mModel){
                        case IS_DS18B20:
                            return String.format(Locale.getDefault(), "%2.2f%s", ((float) mTemp)/16, "\u00B0C");

                        case IS_SHT21:
                            float rtemp = (float) (((mTemp&0xFFFC)*175.72)/0x10000 - 46.85);
                            return String.format(Locale.getDefault(), "%2.2f%s", rtemp, "\u00B0C");
                        case IS_BMP180:
                            return String.format(Locale.getDefault(), "%2.1f%s", ((float) mTemp)/10, "\u00B0C");

                        default:
                            return "No Temp";
                    }
                }else{
                    return "--\u00B0C";
                }
            case IS_HUM:
                if (mHum!=0xFFFF){
                    switch (mModel){
                        case IS_SHT21:
                            float rhum = (float) (((mHum&0xFFFC)*125)/0x10000 - 6);
                            return String.format(Locale.getDefault(), "%2.2f%s", rhum, "%");

                        default:
                            return "No Hum";
                    }
                }else{
                    return "--%";
                }
            case IS_PRESS:
                if (mPress!=0xFFFF){
                    switch (mModel){
                        case IS_BMP180:
                            return String.format(Locale.getDefault(), "%4.2f%s", ((float) mPress)/100, "hPa");

                        default:
                            return "No Press";
                    }
                }else{
                    return "--hPa";
                }
            default:
                return "XXX";
        }
    }


    public Float getFloatSensorValue(int inData, int model, int sensType){
        switch (sensType){
            case IS_TEMP:
                switch (model){
                    case IS_DS18B20:
                        return (float) (inData / 16);
                    case IS_SHT21:
                        return (float) (((inData&0xFFFC)*175.72)/0x10000 - 46.85);
                    case IS_BMP180:
                        return (float) (inData/10);
                }
            case IS_HUM:
                switch (model){
                    case IS_SHT21:
                        return (float) (((inData&0xFFFC)*125)/0x10000 - 6);
                }
            case IS_PRESS:
                switch (model){
                    case IS_BMP180:
                        return (float) (inData/100);
                }
        }
        return (float) 0;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeIntArray(new int[]{mDevNum, mModel, mTemp, mHum, mPress});
    }

    public static final Creator<SensorDevice> CREATOR = new Creator<SensorDevice>() {
        @Override
        public SensorDevice createFromParcel(Parcel in) {
            return new SensorDevice(in);
        }

        @Override
        public SensorDevice[] newArray(int size) {
            return new SensorDevice[size];
        }
    };

}
