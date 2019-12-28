package com.vital.homecontrol;

import android.content.Context;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.NumberPicker;

import java.util.Objects;

public class IPpickerPreference extends DialogPreference {

    private static final String TAG = "MyclassIPpicker";

    private String ip;
    private NumberPicker p1;
    private NumberPicker p2;
    private NumberPicker p3;
    private NumberPicker p4;

// http://qaru.site/questions/79278/concise-way-of-writing-new-dialogpreference-classes
// https://gist.github.com/thom-nic/959884


    public IPpickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogLayoutResource(R.layout.numberpicker_dialog);
        ip = "0.0.0.0";
        Log.i(TAG, " AttributeCount = " + attrs.getAttributeCount());
        for (int i=0;i<attrs.getAttributeCount();i++) {
            String attr = attrs.getAttributeName(i);
            String val  = attrs.getAttributeValue(i);
            Log.i(TAG, " IPpickerPreference attr = " + attr + ", val = " + val);
            if (attr.equalsIgnoreCase("key")) {
                ip = PreferenceManager.getDefaultSharedPreferences(context).getString(val, "0.0.0.0");
            }
            if ((attr.equalsIgnoreCase("defaultvalue"))||(Objects.equals(ip, "0.0.0.0"))){
                ip = val;
            }
        }

    }
    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        p1 = view.findViewById(R.id.np_dialog1);
        p2 = view.findViewById(R.id.np_dialog2);
        p3 = view.findViewById(R.id.np_dialog3);
        p4 = view.findViewById(R.id.np_dialog4);
        String[] res = ip.split("\\.");     // "\\." - просто точка не работает

        p1.setMinValue(0);
        p1.setMaxValue(255);
        if (res[0]!=null) p1.setValue(Integer.parseInt(res[0]));
        p2.setMinValue(0);
        p2.setMaxValue(255);
        if (res[1]!=null) p2.setValue(Integer.parseInt(res[1]));
        p3.setMinValue(0);
        p3.setMaxValue(255);
        if (res[2]!=null) p3.setValue(Integer.parseInt(res[2]));
        p4.setMinValue(0);
        p4.setMaxValue(255);
        if (res[3]!=null) p4.setValue(Integer.parseInt(res[3]));
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (positiveResult){
            ip = p1.getValue()+"."+p2.getValue()+"."+p3.getValue()+"."+p4.getValue();
            setSummary(ip);
            persistString(ip);
        }
    }


    String getIp() {
        return ip;
    }
}
