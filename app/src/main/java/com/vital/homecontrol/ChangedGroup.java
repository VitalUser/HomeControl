package com.vital.homecontrol;

public class ChangedGroup {

    private int nDev;
    private int nOut;
    private boolean isOn;
    private String room;
    private String name;

    public ChangedGroup(int nDev, int nOut, boolean isOn){
        this.nDev=nDev;
        this.nOut=nOut;
        this.isOn=isOn;
        this.room="";
        this.name="";

    }

    public int getNDev(){
        return this.nDev;
    }

    public int getnOut(){
        return this.nOut;
    }

    public boolean isOnState(){
        return this.isOn;
    }

    public String getRoom(){
        return this.room;
    }

    public String getName(){
        return this.name;
    }

    public void setRoom(String room){
        this.room=room;
    }

    public void setName(String name){
        this.name=name;
    }
}
