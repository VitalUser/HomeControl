package com.vital.homecontrol;

import android.content.res.Configuration;

import java.util.ArrayList;
import java.util.Locale;

public class ControlElement {

    private int num;
    private String type;
    private int l_Addr;
    private int p_Addr;
    private int numDev;
    private int cmdNum;
    private int sensType;
    private int sensModel;
    private String text;
    private String upText;
    private ArrayList<Integer> numAr;
    private ArrayList<Integer> maskAr;
    private ArrayList<Integer> tempNumAr;
    private ArrayList<Integer> tempMaskAr;

    ControlElement(int num, String type){
        this.num=num;
        this.type=type;
        this.l_Addr=0;
        this.p_Addr=0;
        this.numDev=0;
        this.cmdNum=0;
        this.sensType=0;
        this.sensModel=0;
        this.text="";
        this.upText="";
        this.numAr = new ArrayList<>();
        this.maskAr = new ArrayList<>();
        this.tempNumAr = new ArrayList<>();
        this.tempMaskAr = new ArrayList<>();
    }

    public void setText(String text){
        this.text=text;
    }

    public void setCmdNum(int cmdNum){
        this.cmdNum=cmdNum;
    }

    public void setNumDev(int numDev){
        this.numDev=numDev;
    }

    public void setL_Addr(int l_Addr){
        this.l_Addr=l_Addr;
    }

    public void setP_Addr(int p_Addr){
        this.p_Addr=p_Addr;
    }

    public String getLocation(int orientation){
        switch (orientation){
            case Configuration.ORIENTATION_PORTRAIT:
                return String.format(Locale.getDefault(), "%04d", this.p_Addr);
            case Configuration.ORIENTATION_LANDSCAPE:
                return String.format(Locale.getDefault(), "%04d", this.l_Addr);
        }
        return "0000";
    }

    public void setLocation(int orientation, int row_col){
        switch (orientation){
            case Configuration.ORIENTATION_PORTRAIT:
                this.p_Addr = row_col;
                break;
            case Configuration.ORIENTATION_LANDSCAPE:
                this.l_Addr = row_col;
                break;
        }
    }

    public boolean isOtherLocExist(int orientation){
        switch (orientation){
            case Configuration.ORIENTATION_PORTRAIT:
                return ((p_Addr==0)&&(l_Addr>0));
            case Configuration.ORIENTATION_LANDSCAPE:
                return ((p_Addr>0)&&(l_Addr==0));

        }
        return false;
    }

    public boolean isBothSide(){
        return ((this.l_Addr>0)&&(this.p_Addr>0));
    }

    public String getType(){
        return this.type;
    }

    public int getNum(){
        return this.num;
    }

    public int getCmdNum(){
        return this.cmdNum;
    }

    public String getText(){
        return this.text;
    }

    public int getNumDev(){
        return this.numDev;
    }

    public int getMask(int numDev){
        int ind = this.numAr.indexOf(numDev);
        if (ind>=0){
            return this.maskAr.get(ind);
        }else{
            return 0;
        }
    }

    public boolean isExistNumDev(int numDev){
        return this.numAr.contains(numDev);
    }

    public void setMask(int numDev, int mask){
        int ind = this.numAr.indexOf(numDev);
        if (ind>=0){
            this.maskAr.set(ind, mask);
        }else{
            this.numAr.add(numDev);
            this.maskAr.add(mask);
        }
    }

    public void setTempMask(int numDev, int mask){
        int ind = this.tempNumAr.indexOf(numDev);
        if (ind>=0){
            this.tempMaskAr.set(ind, mask);
        }else{
            this.tempNumAr.add(numDev);
            this.tempMaskAr.add(mask);
        }
    }

    public void saveMaskFromTemp(){
        this.numAr.clear();
        this.maskAr.clear();
        for (int i = 0; i < this.tempNumAr.size(); i++) {
            if (this.tempMaskAr.get(i)>0){
                this.numAr.add(this.tempNumAr.get(i));
                this.maskAr.add(this.tempMaskAr.get(i));
            }
        }
    }

    public void loadTempMask(){
        this.tempNumAr.clear();
        this.tempMaskAr.clear();
        for (int i = 0; i < this.numAr.size(); i++) {
            this.tempNumAr.add(this.numAr.get(i));
            this.tempMaskAr.add(this.maskAr.get(i));
        }
    }

    public int getTempMask(int numDev){
        int ind = this.tempNumAr.indexOf(numDev);
        if (ind>=0){
            return this.tempMaskAr.get(ind);
        }else{
            return 0;
        }
    }

    public void readMaskString(String source){
        if (!source.equals("")){
            if (source.contains(";")){
                String[] arSrc = source.split(";");
                for (String s : arSrc) {
                    setMask(Integer.parseInt(s.substring(0, 2), 16), Integer.parseInt(s.substring(2, 4), 16));
                }
            }else{
                setMask(this.numDev, Integer.parseInt(source));
            }
        }
    }

    public String getMaskString(){
        StringBuilder msk = new StringBuilder();
        for (int i = 0; i < this.numAr.size(); i++) {
            if (this.maskAr.get(i)>0){
                msk.append(String.format(Locale.getDefault(), "%02x%02x;", this.numAr.get(i), this.maskAr.get(i)));
            }
        }
        return msk.toString();
    }

    public int getFirstNum(){
        if (this.numAr.size()>0)
            return this.numAr.get(0);
        else
            return 0;
    }


    public int getSensType(){
        return this.sensType;
    }

    public void setSensType(int sensType){
        this.sensType=sensType;
    }

    public String getUpText(){
        return this.upText;
    }

    public void setUpText(String upText){
        this.upText=upText;
    }

    public int getSensModel(){
        return this.sensModel;
    }

    public void setSensModel(int sensModel){
        this.sensModel=sensModel;
    }



}
