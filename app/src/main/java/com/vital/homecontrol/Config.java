package com.vital.homecontrol;

import android.preference.PreferenceManager;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Set;

public class Config {
    private static final String TAG = "MyclassConfig";
    private final Properties iniFile;
    private final String configurationFile = "Config.ini";
    private final String filePath;
//    private File file;

    Config(String fPath) {
        iniFile = new Properties();
        filePath = fPath;
//        Log.i(TAG, " Config create: " + filePath);
    }


    public void load(){
        File file = new File(filePath, configurationFile);
        if (file.exists()){
            try {
                iniFile.load(new FileInputStream(file));
//                Log.i(TAG, " Config load: Configuration File loaded from "+filePath);
            } catch (IOException e) {
                e.printStackTrace();
                Log.i(TAG, " Config load: Configuration error: " + e.getMessage());
            }
        }else{
            Log.i(TAG, " Config load: File not Exist");
            try {
                Log.i(TAG,"Creating " + file.toString());
                if (file.createNewFile()) {
                    Log.i(TAG, "File " + file.toString() + " created");
                }

            } catch (IOException e){
                Log.i(TAG, " " + e.getMessage());
            }

        }
    }

    private void save() {
        File file = new File(filePath, configurationFile);
        try {
            FileOutputStream fOut = new FileOutputStream(file);
            iniFile.store(fOut,null);
            fOut.close();
//            Log.i(TAG, " Config store: Configuration File stored to "+filePath);
        } catch (IOException e) {
            Log.i(TAG, " Config store: Configuration error: " + e.getMessage());
        }
    }

    public void saveTo(String dest){
        File file = new File(dest, configurationFile);
        try {
            FileOutputStream fOut = new FileOutputStream(file);
            iniFile.store(fOut,null);
            fOut.close();
            Log.i(TAG, " Config store: Configuration File stored to "+filePath);
        } catch (IOException e) {
            Log.i(TAG, " Config store: Configuration error: " + e.getMessage());
        }
    }

    public void delete(String key){
        load();
//        Log.i(TAG, " Config delete: " + key );
        if(iniFile.remove(key)==null){
            Log.i(TAG, " key " +key+" not found" );
        }
        save();
    }

    public void setStr(String key, String value) {
        load();
        iniFile.setProperty(key, value);
        save();
    }

    public void setInt(String key, int value) {
        String val = String.valueOf(value);
//        Log.i(TAG, " Config SetInt: " + key + " = " + val);
        load();
        iniFile.setProperty(key, val);
        save();
    }

    public String getStr(String key) {
//        String res = iniFile.getProperty(key,"");
        load();
        String res = iniFile.getProperty(key);
//        Log.i(TAG, " Config GetStr: " + key + " = " + res);
        if (res==null){
            res="";
        }

        return res;
    }
    public int getInt(String key) {
        load();
        String res = iniFile.getProperty(key);
//        Log.i(TAG, " Config GetInt: " + key + " = " + res);
        if (res==null){
            res="0";
        }
        return Integer.parseInt(res);
    }

    public List<String> getKeys(String key){  // get all keys, that contains "key"
        load();
        Set set = iniFile.keySet();
        List<String> resList = new ArrayList<>();
        String[] inList = (String[]) set.toArray(new String[0]);
        for (int i = 0; i <inList.length ; i++) {
//            Log.i(TAG, " Config getKeys: " + i + " = " + inList[i]);
            if (inList[i].contains(key)){
                resList.add(inList[i]);
//                Log.i(TAG, " Config getKeys: " + i + " = " + inList[i] + " is match");
            }
        }
        Collections.sort(resList);
        return resList;
    }

    public List<String> getKeysforValue(String value){  // get all keys, that represent "value"
        load();
        Set set = iniFile.keySet();
        List<String> resList = new ArrayList<>();
        String[] inList = (String[]) set.toArray(new String[set.size()]);
        for (int i = 0; i <inList.length ; i++) {
            if (iniFile.getProperty(inList[i]).equals(value)){
//                Log.i(TAG, " add key = " + inList[i]);
                resList.add(inList[i]);
            }
        }
        Collections.sort(resList);
        return resList;
    }

    public List<String> getValues(String key){  // get all values, that contains "key" in key
        List<String> keyList = getKeys(key);
        ArrayList<String> resList = new ArrayList<>();
        for (int i = 0; i < keyList.size(); i++) {
            resList.add(getStr(keyList.get(i)));
        }
        return  resList;
    }

}
