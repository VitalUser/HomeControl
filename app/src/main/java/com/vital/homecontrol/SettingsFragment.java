package com.vital.homecontrol;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.util.Log;

import java.io.File;
import java.util.Objects;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = "MyclassSettingsFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        EditTextPreference pass = (EditTextPreference) findPreference("key_udppass");
        pass.setSummary(pass.getText() + " (" + Integer.toHexString(Integer.parseInt(pass.getText())).toUpperCase() + ")");

        IPpickerPreference iPpicker = (IPpickerPreference) findPreference("key_remIP");
        iPpicker.setSummary(iPpicker.getIp());

        IPpickerPreference signaliPpicker = (IPpickerPreference) findPreference("key_signalIP");
        signaliPpicker.setSummary(signaliPpicker.getIp());

        EditTextPreference port = (EditTextPreference) findPreference("key_port");
        port.setSummary(port.getText());

        EditTextPreference serial = (EditTextPreference) findPreference("key_serial");
        serial.setSummary(serial.getText());

        SwitchPreference cb_static = (SwitchPreference)findPreference("id_cb_StaticIP");
        signaliPpicker.setEnabled(!cb_static.isChecked());
        serial.setEnabled(!cb_static.isChecked());

        EditTextPreference timeout = (EditTextPreference) findPreference("key_timeout");
        timeout.setSummary(timeout.getText());

        ListPreference theme = (ListPreference) findPreference("key_theme");
        theme.setSummary(theme.getEntry());

        EditTextPreference colsP = (EditTextPreference) findPreference("key_colsCount_P");
        colsP.setSummary(colsP.getText());

        EditTextPreference rowsP = (EditTextPreference) findPreference("key_rowsCount_P");
        rowsP.setSummary(rowsP.getText());

        EditTextPreference colsL = (EditTextPreference) findPreference("key_colsCount_L");
        colsL.setSummary(colsL.getText());

        EditTextPreference rowsL = (EditTextPreference) findPreference("key_rowsCount_L");
        rowsL.setSummary(rowsL.getText());

        EditTextPreference btnPadding = (EditTextPreference) findPreference("key_button_padding");
        btnPadding.setSummary(btnPadding.getText());

        ListPreference names = (ListPreference) findPreference("key_names");
        String defName = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("key_names", "");
        String namesPath = Environment.getExternalStorageDirectory().toString() + "/HomeControl/Names";
        File file = new File(namesPath);
        if (file.exists()){
            File[] fAr = file.listFiles();
            if (fAr.length>0){
                CharSequence[] entries = new CharSequence[fAr.length];
                CharSequence[] entryValues = new CharSequence[fAr.length];
                for (int i = 0; i <fAr.length ; i++) {
                    entries[i] = fAr[i].getName();
                    entryValues[i] = fAr[i].toString();
                    if (Objects.equals(defName, fAr[i].toString())){
                        names.setDefaultValue(fAr[i].toString());
                    }
                }
                names.setEntries(entries);
                names.setEntryValues(entryValues);
                names.setSummary(defName);
                names.setEnabled(true);
            }else{
                names.setEnabled(false);
            }
        }else{
            names.setEnabled(false);
        }

    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        Log.i(TAG, " onSharedPreferenceChanged (fragment): s = " + s);
        EditTextPreference editText;
        EditTextPreference remport = (EditTextPreference) findPreference("key_port");
        IPpickerPreference iPpicker = (IPpickerPreference) findPreference("key_remIP");
        IPpickerPreference signaliPpicker = (IPpickerPreference) findPreference("key_signalIP");
        EditTextPreference serial = (EditTextPreference) findPreference("key_serial");
        switch (s){
            case "key_udppass":
                EditTextPreference pass = (EditTextPreference) findPreference(s);
                pass.setSummary(pass.getText() + " (" + Integer.toHexString(Integer.parseInt(pass.getText())).toUpperCase() + ")");
                break;
            case "key_remIP":
                iPpicker.setSummary(iPpicker.getIp());
                break;
            case "key_port":
                remport.setSummary(remport.getText());
                break;
            case "key_signalIP":
                signaliPpicker.setSummary(signaliPpicker.getIp());
                break;
            case "id_cb_StaticIP":
                SwitchPreference cb_static = (SwitchPreference)findPreference(s);
                signaliPpicker.setEnabled(!cb_static.isChecked());
                serial.setEnabled(!cb_static.isChecked());
                break;
            case "key_serial":
                serial.setSummary(serial.getText());
                break;

            case "key_theme":
            case "key_names":
                ListPreference theme = (ListPreference) findPreference(s);
                theme.setSummary(theme.getEntry());
                break;
            case "key_timeout":
            case "key_colsCount_P":
            case "key_rowsCount_P":
            case "key_colsCount_L":
            case "key_rowsCount_L":
            case "key_button_padding":
                editText = (EditTextPreference) findPreference(s);
                editText.setSummary(editText.getText());
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }
}