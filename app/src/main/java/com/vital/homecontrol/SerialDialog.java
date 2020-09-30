package com.vital.homecontrol;

import android.content.Context;
import android.content.pm.PackageManager;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.Objects;

public class SerialDialog extends DialogPreference {

    private static final String TAG = "MyclassSerialDialog";

    private int serial;
    private EditText editSerial;
    private int curserial;

    public SerialDialog(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogLayoutResource(R.layout.serial_dlg);
        serial = 0;
        curserial = PreferenceManager.getDefaultSharedPreferences(context).getInt("curSerial", 0);
//        Log.i(TAG, " AttributeCount = " + attrs.getAttributeCount());
        for (int i=0;i<attrs.getAttributeCount();i++) {
            String attr = attrs.getAttributeName(i);
            String val  = attrs.getAttributeValue(i);
//            Log.i(TAG, " IPpickerPreference attr = " + attr + ", val = " + val);
            if (attr.equalsIgnoreCase("key")) {
                serial = Integer.parseInt(Objects.requireNonNull(PreferenceManager.getDefaultSharedPreferences(context).getString(val, "0")));
            }
            if ((attr.equalsIgnoreCase("defaultvalue"))&&(Objects.equals(serial, 0))){
                serial = Integer.parseInt(val);
            }
        }
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        editSerial = view.findViewById(R.id.edit_txt_serial);
        editSerial.setText(String.valueOf(serial));
        editSerial.setInputType(InputType.TYPE_CLASS_NUMBER);
        editSerial.setSelectAllOnFocus(true);

        Button btnGetSerial = view.findViewById(R.id.btn_get_serial);
        if (curserial!=0){
            btnGetSerial.setText(String.valueOf(curserial));
            btnGetSerial.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    editSerial.setText(String.valueOf(curserial));
                }
            });

        }else {
            btnGetSerial.setVisibility(View.INVISIBLE);
        }

    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (positiveResult){
            serial = Integer.parseInt(editSerial.getText().toString());
            setSummary(String.valueOf(serial));
            persistString(String.valueOf(serial));
        }
    }

    public String getText(){
        return String.valueOf(serial);
    }
}
