package com.vital.homecontrol;

import android.content.res.Configuration;

import java.util.Locale;

public class ControlElement {

    private int num;
    private String type;
    private int l_Addr;
    private int p_Addr;
    private int numDev;
    private int cmdNum;
    private int outMask;
    private int sensType;
    private int sensModel;
    private String text;
    private String upText;

    ControlElement(int num, String type){
        this.num=num;
        this.type=type;
        this.l_Addr=0;
        this.p_Addr=0;
        this.numDev=0;
        this.cmdNum=0;
        this.outMask=0;
        this.sensType=0;
        this.sensModel=0;
        this.text="";
        this.upText="";
    }

    public void setNum(int num){
        this.num=num;
    }

    public void setText(String text){
        this.text=text;
    }

    public void setCmdNum(int cmdNum){
        this.cmdNum=cmdNum;
    }

    public void setOutMask(int outMask){
        this.outMask=outMask;
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

    public int getOutMask(){
        return this.outMask;
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
