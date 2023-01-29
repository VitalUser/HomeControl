package com.vital.homecontrol;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class IniFile {

    private static final String TAG = "MyclassIniFile";
    private final Properties iniFile;
    private final String filePath;

    IniFile(String fPath){
        iniFile = new Properties();
        filePath = fPath;

    }

    private void load(){
        File file = new File(filePath);
        if (!file.exists()){
            try {
                if (!file.createNewFile())
                    return;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            iniFile.load(new FileInputStream(file));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void save(){
        File file = new File(filePath);
        try {
            FileOutputStream fOut = new FileOutputStream(file);
            iniFile.store(fOut,null);
            fOut.close();
        } catch (IOException e) {
            Log.i(TAG, " Config store: Configuration error: " + e.getMessage());
        }
    }

    public void setStr(String key, String value) {
        load();
        iniFile.setProperty(key, value);
        save();
    }

    public String getStr(String key) {
        load();
        return iniFile.getProperty(key, "");
    }


}
