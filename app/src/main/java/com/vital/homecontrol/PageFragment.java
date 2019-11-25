package com.vital.homecontrol;

// https://startandroid.ru/ru/uroki/vse-uroki-spiskom/228-urok-125-viewpager.html

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.text.InputType;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static com.vital.homecontrol.MainActivity.CMD_ASK_STATISTIC;
import static com.vital.homecontrol.MainActivity.CMD_MSG_ANALOG_DATA;
import static com.vital.homecontrol.MainActivity.CMD_MSG_STATISTIC;
import static com.vital.homecontrol.MainActivity.CMD_SEND_COMMAND;
import static com.vital.homecontrol.MainActivity.MSG_DEVICE_KIND;
import static com.vital.homecontrol.MainActivity.MSG_DEV_TYPE;
import static com.vital.homecontrol.MainActivity.MSG_OUT_STATE;
import static com.vital.homecontrol.MainActivity.MSG_RE_SENT_W;
import static com.vital.homecontrol.MainActivity.MSG_SENSOR_STATE;
import static com.vital.homecontrol.MainActivity.MSG_STATE;
import static com.vital.homecontrol.MainActivity.SET_W_COMMAND;
import static com.vital.homecontrol.MainActivity.byteArrayToHex;

public class PageFragment extends Fragment {
    private static final String TAG = "MyclassPageFragment";
    public static final String ARG_PAGE = "ARG_PAGE";
    public static final String ARG_FR_ID = "ARG_FR_ID";
    static final String UDP_RCV = "UDP_received";
//    static final String STATE_EXECDEVS = "ExecDevices";

    SharedPreferences prefs;

    private final String ROOM_NAME_KEY = "RoomName";
    private final String CELL_W = "CellWidth";
    private final String CELL_H = "CellHeight";
    private final String CELL_N = "CellNum";        // format: CELL_N + _P_(_L_) + cell_addr (0P0R0C),  value: ctrl_num (xPxxN)

    private final String BTN_TEXT = "BtnText";      // format: BTN_TEXT + "N" + ctrl_num
    private final String BTN_UP_TEXT = "BtnUpText";      // format: BTN_UP_TEXT + "N" + ctrl_num

    private final String BTN_NUMDEV = "BtnNumDev";      // format: BTN_NUMDEV + "N" + ctrl_num
    private final String BTN_OUTMASK = "ButtonOutMask";      // format: BTN_OUTMASK + "N" + ctrl_num
    private final String BTN_CMD = "BtnCommand";      // format: BTN_CMD + "N" + ctrl_num

    private final String CELL_TYPE = "CellType";      // format: CELL_TYPE + "N" + ctrl_num
    // values for CELL_TYPE
    private final String TYPE_BUTTON_UP = "ButtonUp";
    private final String TYPE_BUTTON_SQ = "ButtonSq";
    private final String TYPE_BUTTON_DN = "ButtonDn";
    private final String TYPE_SENSOR = "Sensor";

    private final String BTN_LA = "BtnL_Address";
    private final String BTN_PA = "BtnP_Address";


    // values for R.id.v_type
    public static final int IS_CELL = 1;
    public static final int IS_BUTTON = 2;
    public static final int IS_SENSOR = 3;

    private final String SNS_MODEL = "SensorModel";
    // values for sensors.get(i).getType() and SNS_MODEL
    public static final int IS_DS18B20 = 3;
    public static final int IS_SHT21 = 4;
    public static final int IS_BMP180 = 5;

    private final String SNS_TYPE = "SensorType";
    // values for SNS_TYPE ana R.id.sns_typ
    public static final int IS_TEMP = 1;
    public static final int IS_HUM = 2;
    public static final int IS_PRESS = 3;

    private final int M_CANCEL      = 1;
    private final int M_ADD_BTN     = 2;
    private final int M_ADD_SNS     = 3;
    private final int M_ADD_EXIST   = 4;
    private final int M_REN_BTN     = 5;
    private final int M_DEL_BTN     = 6;
    private final int M_SET_CMD     = 7;
    private final int M_SET_NDEV    = 8;
    private final int M_SET_MASK    = 9;
    private final int M_SET_CHNG    = 10;
    private final int M_REN_SNS     = 11;
    private final int M_DEL_SNS     = 12;
    private final int M_TEMP_SNS    = 13;
    private final int M_HUM_SNS     = 14;
    private final int M_PRES_SNS    = 15;
    private final int M_SHOW_STAT   = 16;

    private int mPage;
//    private static View selectedView;
    private View selectedView;
    private ViewGroup vGroup;
    private ViewGroup mTable;
    private MainActivity act;
//    private int lastCommand = 0;
//    private int changedState = 0;

//    private Config config;
    private Boolean udpRecieverRegistered;
    private int orientation;
    public List<View> btnArray = new ArrayList<>();
    public  List<View> snsArray = new ArrayList<>();
//    private List<ExecDevice> execDevs = new ArrayList<>();
//    private List<SensorDevice> sensors = new ArrayList<>();

    private List<ControlElement> controls = new ArrayList<>();

    public static PageFragment newInstance(int page) {
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        PageFragment fragment = new PageFragment();
        fragment.setArguments(args);
        Log.i(TAG, " PageFragment: new page =" + page);

        return fragment;
    }

    @Override
    public void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        if (getArguments()!=null){
            mPage = getArguments().getInt(ARG_PAGE);
        }
        act = (MainActivity)getActivity();
        prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        if (act!=null){
            LocalBroadcastManager.getInstance(act).registerReceiver(udpReciever, new IntentFilter(UDP_RCV));
            udpRecieverRegistered = true;
        }

        String stPage = String.format(Locale.getDefault(), "%02d", mPage);
        String keyPrefix = CELL_TYPE + "N"+stPage;
        act.updateConfig();
        List<String> keys = act.readKeys(keyPrefix);
        controls.clear();
        for (int i = 0; i <keys.size() ; i++) {
            String stNum = keys.get(i).replace(keyPrefix, "");
            String suff = "N"+stPage+stNum;
            String typ = act.readStr(keys.get(i));
            controls.add(new ControlElement(Integer.parseInt(stNum), typ));
            int cind = controls.size()-1;
            controls.get(cind).setCmdNum(act.readInt(BTN_CMD+suff));
            controls.get(cind).setNumDev(act.readInt(BTN_NUMDEV+suff));
            controls.get(cind).setL_Addr(act.readInt(BTN_LA+suff));
            controls.get(cind).setP_Addr(act.readInt(BTN_PA+suff));
            controls.get(cind).setOutMask(act.readInt(BTN_OUTMASK+suff));
            controls.get(cind).setText(act.readStr(BTN_TEXT+suff));
            controls.get(cind).setSensType(act.readInt(SNS_TYPE+suff));
            controls.get(cind).setSensModel(act.readInt(SNS_MODEL+suff));
            controls.get(cind).setUpText(act.readStr(BTN_UP_TEXT+suff));

        }
        Log.i(TAG, " onCreate, fragment " + mPage);
    }


    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG, " PageFragment("+mPage+"): onPause");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, " PageFragment: onResume  " + mPage +", controls: = " + controls.size());
    }

    @Override
    public void onDestroy() {
        if (udpRecieverRegistered){
            if (act!=null){
                LocalBroadcastManager.getInstance(act).unregisterReceiver(udpReciever);
                udpRecieverRegistered=false;
            }
        }
        super.onDestroy();
        Log.i(TAG, " PageFragment: onDestroy  " + mPage);
    }

    // http://apsoid.ru/talk/topic/7489-динамическое-добавление-компонентов/
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_page, container, false);
        vGroup = container;
        final String sufOr;

        int cols;
        int rows;
        int orientation = act.getResources().getConfiguration().orientation;
        switch (orientation){
            case Configuration.ORIENTATION_LANDSCAPE:
                sufOr="_L_";
                cols = Integer.parseInt(prefs.getString("key_colsCount_L", "6"));
                rows = Integer.parseInt(prefs.getString("key_rowsCount_L", "4"));
                break;
            case Configuration.ORIENTATION_PORTRAIT:
                sufOr="_P_";
                cols = Integer.parseInt(prefs.getString("key_colsCount_P", "4"));
                rows = Integer.parseInt(prefs.getString("key_rowsCount_P", "6"));
                break;
            default:
                sufOr="_N_";
                cols =3;
                rows =3;
        }

        TableLayout tl = (TableLayout)view.findViewById(R.id.tlb_Layout);
        tl.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT));
        tl.setStretchAllColumns(true);

        tl.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
                act.saveInt(CELL_W+sufOr, view.getWidth());
                act.saveInt(CELL_H+sufOr, view.getHeight());
            }
        });
        String cell_addr;

        int w = act.readInt(CELL_W+sufOr);
        int h = act.readInt(CELL_H+sufOr);

        for (int row = 1; row < rows+1; row++) {
            TableRow tableRow = new TableRow(getActivity());
            tableRow.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT, 1));

            for (int col = 1; col < cols+1; col++) {

                LinearLayout cell = new LinearLayout(getActivity());
                if ((w>0)&&(h>0)){
                    cell.setLayoutParams(new TableRow.LayoutParams(w/ cols, h/ rows));
                }else{
                    cell.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT, 1));
                }

                cell_addr = String.format(Locale.getDefault(),"%02d%02d", row, col);
                cell.setTag(R.id.v_type, IS_CELL);
                cell.setTag(R.id.tbl_col, col);
                cell.setTag(R.id.tbl_row, row);
                cell.setTag(R.id.ctr_num, 0);
//                cell.setId(mPage*10000 + row*100 + col);
//                cell.setBackgroundResource(R.drawable.cell_color);

                int cInd = getControlIndexByLocation(orientation, cell_addr);

                if (cInd<0){
                    registerForContextMenu(cell);
                }else{
                    switch (controls.get(cInd).getType()){
                        case TYPE_BUTTON_SQ:
                            cell.addView(makeButton(cInd, row, col));
                            break;
                        case TYPE_SENSOR:
                            cell.addView(makeSensor(cInd, row, col));
                            break;
                    }
                }
                tableRow.addView(cell, col-1);
            }
            tl.addView(tableRow, row-1);
        }
        mTable = (ViewGroup) tl;

        Log.i(TAG, " onCreateView PageFragment " + mPage);
        return view;
    }

    @Override
    public void onDestroyView() {
        Log.i(TAG, " onDestroyView: " + mPage);
        super.onDestroyView();
    }

    private View makeButton(int cInd, int row, int col){
        LinearLayout cellButton = (LinearLayout) LayoutInflater.from(getActivity()).inflate(R.layout.control_element, vGroup, false);
        cellButton.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        int pd = Integer.parseInt(prefs.getString("key_button_padding", "2"));
        cellButton.setPadding(pd,pd,pd,pd);
        Button btn = cellButton.findViewById(R.id.ctr_button);
        registerForContextMenu(btn);
        final int num = controls.get(cInd).getNum();
        btn.setTag(R.id.v_type, IS_BUTTON);
        btn.setTag(R.id.ctr_num, num);
        btn.setTag(R.id.tbl_col, col);
        btn.setTag(R.id.tbl_row, row);

//        ((GradientDrawable)btn.getBackground().getCurrent()).setCornerRadius(50);

//        btn.setText(String.format(Locale.getDefault(),"%02d%03d", mPage, num));
        btn.setText(controls.get(cInd).getText());
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int ind = getControlIndexByNum(num);
                int cmd = controls.get(ind).getCmdNum();
                Log.i(TAG, " button: "+num);
                act.sendCommand(cmd);
            }
        });

//        TextView text = cellButton.findViewById(R.id.ctr_text);
//        text.setText(controls.get(cInd).getText());

        int devInd = act.getDevIndex(controls.get(cInd).getNumDev());
        if (devInd>=0){
            int outState = act.execDevs.get(devInd).getOutState();
            setButtonVisualState(btn, cInd, outState);
        }

        return cellButton;
    }

    private View makeSensor(int cInd, int row, int col){
        LinearLayout cellSensor = (LinearLayout) LayoutInflater.from(getActivity()).inflate(R.layout.sns_element, vGroup, false);
        cellSensor.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        TextView sensText = cellSensor.findViewById(R.id.sns_text);
        TextView sensUpText = cellSensor.findViewById(R.id.sns_up_text);
        TextView sensValue = cellSensor.findViewById(R.id.sns_value);
        registerForContextMenu(sensValue);
        final int num = controls.get(cInd).getNum();
        sensValue.setTag(R.id.v_type, IS_SENSOR);
        sensValue.setTag(R.id.ctr_num, num);
        sensValue.setTag(R.id.tbl_col, col);
        sensValue.setTag(R.id.tbl_row, row);
        sensText.setText(controls.get(cInd).getText());
        sensValue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                int ind = getControlIndexByNum(num);
//                int cmd = controls.get(ind).getCmdNum();
//                Log.i(TAG, " button: "+num);
//                act.sendCommand(cmd);
            }
        });

        sensUpText.setText(controls.get(cInd).getUpText());
        int nDev = controls.get(cInd).getNumDev();
        int sensType = controls.get(cInd).getSensType();
        int sInd = act.getSnsIndex(nDev);
        if (sInd>=0){
            sensValue.setText(act.sensors.get(sInd).getValue(sensType));
        }else{
            sensValue.setText("No sns");
        }


//        TextView text = cellSensor.findViewById(R.id.ctr_text);
//        text.setText(controls.get(cInd).getText());


        return cellSensor;
    }



    // https://developer.android.com/guide/topics/ui/menus.html

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        selectedView = v;
//        orientation = act.getResources().getConfiguration().orientation;
//            MenuInflater menuInflater= getActivity().getMenuInflater();
        int typ = (int) v.getTag(R.id.v_type);
//            int num = (int) v.getTag(R.id.ctr_num);
//            int page = (int) v.getTag(R.id.tbl_page);
//            int row = (int) v.getTag(R.id.tbl_row);
//            int col = (int) v.getTag(R.id.tbl_col);
        switch (typ){
            case IS_CELL:
//                    menuInflater.inflate(R.menu.tbl_context_menu, menu);
                int row = (int) v.getTag(R.id.tbl_row);
                int col = (int) v.getTag(R.id.tbl_col);
                int loc = row*100+col;
                String[] newums = getExistNums();
                menu.add(0, M_ADD_BTN*10000+loc, 1, getString(R.string.tbl_add_button));
                if (act.sensors.size()>0){
                    menu.add(0, M_ADD_SNS*10000+loc, 2, getString(R.string.tbl_add_sensor));
                }
                if (newums.length>0){
                    menu.add(0, M_ADD_EXIST*10000+loc, 3, getString(R.string.tbl_add_exist_btn));
                }
                menu.add(0, M_CANCEL*10000+loc, 4, getString(R.string.cancel));
                String roomName = act.readStr(ROOM_NAME_KEY + String.format(Locale.getDefault(),"%02d", mPage));
                menu.setHeaderTitle(roomName);
                return;
            case IS_BUTTON:
                int num = (int) v.getTag(R.id.ctr_num);
                String txt = "";
//                    menuInflater.inflate(R.menu.btn_context_menu, menu);
                menu.add(0, M_REN_BTN*10000+num, 1, getString(R.string.msg_rename));
                menu.add(0, M_SET_CMD*10000+num, 2, getString(R.string.set_cmd_num));
                if (act.execDevs.size()>0){
                    menu.add(0, M_SET_NDEV*10000+num, 3, getString(R.string.set_dev_num));
                    menu.add(0, M_SET_MASK*10000+num, 4, getString(R.string.set_outmask));
                    menu.add(0, M_SET_CHNG*10000+num, 5, getString(R.string.set_last_change));
                }
                menu.add(0, M_DEL_BTN*10000+num, 9, getString(R.string.btn_item_Del));
                menu.add(0, M_CANCEL*10000+num, 10, getString(R.string.cancel));
                int cInd = getControlIndexByNum(num);
                if (cInd>=0){
                    txt = controls.get(cInd).getText();
                }
                if (txt.equals("")){
                    menu.setHeaderTitle(getString(R.string.button)+" " + num);
                }else{
                    menu.setHeaderTitle(txt);
                }
                return;
            case IS_SENSOR:
                int numSns = (int) v.getTag(R.id.ctr_num);
                cInd = getControlIndexByNum(numSns);
                String snsTitle = "";
                if (cInd>=0){
                    snsTitle += controls.get(cInd).getNumDev() +", "+ controls.get(cInd).getUpText();
                    switch (controls.get(cInd).getSensModel()){
                        case IS_DS18B20:
                            menu.add(0, M_TEMP_SNS*10000+numSns, 2, getString(R.string.sns_is_Temp));
                            break;
                        case IS_SHT21:
                            menu.add(0, M_TEMP_SNS*10000+numSns, 2, getString(R.string.sns_is_Temp));
                            menu.add(0, M_HUM_SNS*10000+numSns, 3, getString(R.string.sns_is_Hum));
                            break;
                        case IS_BMP180:
                            menu.add(0, M_TEMP_SNS*10000+numSns, 2, getString(R.string.sns_is_Temp));
                            menu.add(0, M_PRES_SNS*10000+numSns, 4, getString(R.string.sns_is_Press));
                            break;
                    }

                }
//                    menuInflater.inflate(R.menu.sensor_context_menu, menu);
                menu.add(0, M_SHOW_STAT*10000+numSns, 5, getString(R.string.msg_statistic));
                menu.add(0, M_REN_SNS*10000+numSns, 1, getString(R.string.msg_rename));
                menu.add(0, M_DEL_SNS*10000+numSns, 9, getString(R.string.btn_item_Del));
                menu.add(0, M_CANCEL*10000+numSns, 10, getString(R.string.cancel));
                menu.setHeaderTitle(getString(R.string.sensor)+" " + snsTitle);
                return;
            default:
        }
    }



    private int getFreeCtrlNum(){
        int num = 1;
        while (getControlIndexByNum(num)>=0){
            num++;
        }
        return num;
    }

    private String[] getExistNums(){
        int orientation = act.getResources().getConfiguration().orientation;
        List<String> exnums = new ArrayList<>();

        for (int i = 0; i <controls.size() ; i++) {
            if (controls.get(i).isOtherLocExist(orientation)){
                String existItem = String.valueOf(controls.get(i).getNum())+"; ";
                switch (controls.get(i).getType()){
                    case TYPE_BUTTON_SQ:
                        existItem += getString(R.string.button)+" "+controls.get(i).getText();
                        break;
                    case TYPE_SENSOR:
                        existItem +=  getString(R.string.sensor)+" "+ controls.get(i).getNumDev()+", " + controls.get(i).getUpText()+", " +controls.get(i).getText();
                        break;
                }
                exnums.add(existItem);
            }
        }
        String[] newums = new String[exnums.size()];
        for (int i = 0; i <exnums.size() ; i++) {
            newums[i]=exnums.get(i);
        }
        return newums;
    }


    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (!getUserVisibleHint()){
            return false;
        }
        act = (MainActivity)getActivity();
        orientation = act.getResources().getConfiguration().orientation;
        final String orientTag;
        if (orientation==Configuration.ORIENTATION_PORTRAIT){
            orientTag = BTN_PA;
        }else{
            orientTag = BTN_LA;
        }
        int freeNum;
        int gotItemID = item.getItemId();
        int mItem = gotItemID/10000;
        final int row = (gotItemID % 10000)/100 ;
        final int col = gotItemID % 100;
        final int btnNum = (int) selectedView.getTag(R.id.ctr_num);
        final int cInd = getControlIndexByNum(btnNum);
        final String suff = String.format(Locale.getDefault(), "N%02d%03d", mPage, btnNum);
//        final int btnNum = gotItemID % 1000;
        AlertDialog.Builder dlg;

        switch (mItem){

//----------------------------------------------------------------IS CELL---------------------------------------------------------------------
            case M_ADD_BTN:
//                View cell = findViewAiLocation(row, col);
                unregisterForContextMenu(selectedView);
                freeNum = getFreeCtrlNum();
                String textFreeNum = String.format(Locale.getDefault(), "N%02d%03d", mPage, freeNum);
                act.saveStr(CELL_TYPE+textFreeNum, TYPE_BUTTON_SQ);
                controls.add(new ControlElement(freeNum, TYPE_BUTTON_SQ));
                int newInd = controls.size()-1;
                act.saveInt(orientTag+textFreeNum, row*100+col);
                controls.get(newInd).setLocation(orientation, row*100+col);
                ((LinearLayout)selectedView).addView(makeButton(newInd, row, col));
                return true;

            case M_ADD_EXIST:
                dlg = new AlertDialog.Builder(getActivity());
                final String[] newums = getExistNums();
                if (newums.length==0){
                    dlg.setTitle(R.string.no_exist_elms);
                }else{
                    dlg.setTitle(R.string.msg_choose_exist_element);
                    dlg.setItems(newums, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            unregisterForContextMenu(selectedView);
                            int num = Integer.parseInt(newums[i].substring(0, newums[i].indexOf(";")));
                            String textNum = String.format(Locale.getDefault(), "N%02d%03d", mPage, num);
//                            act.saveStr(CELL_TYPE+textNum, TYPE_BUTTON_SQ);
                            int ind=getControlIndexByNum(num);
                            if (ind>=0){
                                act.saveInt(orientTag+textNum, row*100+col);
                                controls.get(ind).setLocation(orientation, row*100+col);
                                switch (controls.get(ind).getType()){
                                    case TYPE_BUTTON_SQ:
                                        ((LinearLayout)selectedView).addView(makeButton(ind, row, col));

                                        break;
                                    case TYPE_SENSOR:
                                        ((LinearLayout)selectedView).addView(makeSensor(ind, row, col));
                                        break;

                                }

                            }
                        }
                    });
                }
                dlg.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
                dlg.show();
                return true;

            case M_ADD_SNS:
                dlg = new AlertDialog.Builder(getActivity());
                final String[] sensList = new String[act.sensors.size()];
                for (int i = 0; i <sensList.length ; i++) {
                    sensList[i]= String.valueOf(act.sensors.get(i).getNum()) + "; "+act.sensors.get(i).getModelString();
                }

                dlg.setTitle(R.string.sensor_choice);
                dlg.setItems(sensList, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        unregisterForContextMenu(selectedView);
//                        int numDev = Integer.parseInt(sensList[i].substring(0, sensList[i].indexOf(";")));
                        int numDev = act.sensors.get(i).getNum();
                        String snsModel = act.sensors.get(i).getModelString();
                        int freeNum = getFreeCtrlNum();
                        String textFreeNum = String.format(Locale.getDefault(), "N%02d%03d", mPage, freeNum);
                        act.saveStr(CELL_TYPE+textFreeNum, TYPE_SENSOR);
                        act.saveStr(BTN_UP_TEXT+textFreeNum, snsModel);
                        act.saveInt(SNS_MODEL+textFreeNum, act.sensors.get(i).getModel());
                        act.saveInt(orientTag+textFreeNum, row*100+col);
                        act.saveInt(BTN_NUMDEV+textFreeNum, numDev);
                        int newInd = controls.size();
                        controls.add(new ControlElement(freeNum, TYPE_SENSOR));
                        controls.get(newInd).setNumDev(numDev);
                        controls.get(newInd).setSensModel(act.sensors.get(i).getModel());
                        controls.get(newInd).setUpText(snsModel);
                        controls.get(newInd).setLocation(orientation, row*100+col);

                        ((LinearLayout)selectedView).addView(makeSensor(newInd, row, col));
                    }
                });

                dlg.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
                dlg.show();
                return true;



//----------------------------------------------------------------IS BUTTON---------------------------------------------------------------------

            case M_REN_BTN:
//                cInd = getControlIndexByNum(btnNum);
                if (cInd>=0){
                    dlg = new AlertDialog.Builder(getActivity());
                    final EditText input = new EditText(getActivity());
                    input.setSelectAllOnFocus(true);
                    String srcText = controls.get(cInd).getText();
                    int dNum = controls.get(cInd).getNumDev();
                    int msk = controls.get(cInd).getOutMask();
                    if ((srcText.equals(""))&&(dNum>0)&&(msk>0)){
                        int mskind = -1;
                        while(msk>0){
                            msk = msk /2;
                            mskind++;
                        }
                        int di = act.getDevIndex(dNum);
                        if (di>=0){
                            srcText = act.execDevs.get(di).getLampText(mskind);
                        }
                    }
                    input.setText(srcText);
                    dlg.setView(input);
                    dlg.setTitle(R.string.msg_rename);
                    dlg.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
//                            TextView tx =  ((View) selectedView.getParent()).findViewById(R.id.ctr_text);
//                            tx.setText(input.getText().toString());
                            ((Button)selectedView).setText(input.getText().toString());
                            act.saveStr(BTN_TEXT + suff, input.getText().toString());
                            controls.get(cInd).setText(input.getText().toString());
                        }
                    });
                    dlg.setNegativeButton(getText(R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    });
                    dlg.show();
                }
                return true;

            case M_DEL_BTN:
//                cInd = getControlIndexByNum(btnNum);
                if (controls.get(cInd).isBothSide()){
                    controls.get(cInd).setLocation(orientation, 0);
                    act.delKey(orientTag  + suff);

                } else {
                    String suffN = "N"+String.format(Locale.getDefault(), "%02d%03d", mPage, controls.get(cInd).getNum());
                    List<String> keys = act.readKeys(suffN);
                    for (int i = 0; i <keys.size() ; i++) {
                        Log.i(TAG, " delete key: " + keys.get(i));
                        act.delKey(keys.get(i));
                    }

                    controls.remove(cInd);

                }
                LinearLayout ctrElm = (LinearLayout) selectedView.getParent();
                LinearLayout cell = (LinearLayout) ctrElm.getParent();
                cell.removeAllViews();
                registerForContextMenu(cell);

                return true;

            case M_SET_CMD:
//                cInd = getControlIndexByNum(btnNum);
                dlg = new AlertDialog.Builder(getActivity());
                dlg.setTitle(getString(R.string.enter_cmd));
                final EditText cmdNum = new EditText(getActivity());
                cmdNum.setSelectAllOnFocus(true);
                cmdNum.setInputType(InputType.TYPE_CLASS_NUMBER);
                String txt = String.valueOf(controls.get(cInd).getCmdNum());
                cmdNum.setText(txt);
                dlg.setView(cmdNum);

                dlg.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        controls.get(cInd).setCmdNum(Integer.parseInt(cmdNum.getText().toString()));
//                        String strBtnNum = String.format(Locale.getDefault(), "N%02d%03d", mPage, btnNum);
                        act.saveInt(BTN_CMD + suff, Integer.parseInt(cmdNum.getText().toString()));
                    }
                });


                dlg.setNegativeButton(getText(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
                dlg.show();

            return true;

            case M_SET_NDEV:
//                cInd = getControlIndexByNum(btnNum);
                dlg = new AlertDialog.Builder(getActivity());
                final String[] devNums = new String[act.execDevs.size()];
                for (int i = 0; i <devNums.length ; i++) {
                    devNums[i] = String.valueOf(act.execDevs.get(i).getDevNum());
                }
                txt = getString(R.string.set_dev_num)+ " ("+controls.get(cInd).getNumDev() + ")";
                dlg.setTitle(txt);
                dlg.setItems(devNums, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        int num = controls.get(cInd).getNum();
//                        String textNum = String.format(Locale.getDefault(), "N%02d%03d", mPage, num);
                        int dNum = Integer.parseInt(devNums[i]);
                        controls.get(cInd).setNumDev(dNum);
                        act.saveInt(BTN_NUMDEV+suff, dNum);

                        num = act.getDevIndex(dNum);
                        if (num>=0){
                            updateButtonsState(dNum, act.execDevs.get(num).getOutState());
                        }

                    }
                });
                dlg.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
                dlg.show();
                return true;

            case M_SET_MASK:
//                cInd = getControlIndexByNum(btnNum);
                dlg = new AlertDialog.Builder(getActivity());
                int dNum = controls.get(cInd).getNumDev();
                String title = getString(R.string.set_outmask);
                title = title + " " + (dNum>0?getString(R.string.for_device)+" "+dNum : getString(R.string.dev_not_assigned));
                dlg.setTitle(title);
//                int ms = controls.get(cInd).getOutMask();
                final String[] lamps;
                int di = act.getDevIndex(dNum);
                if (di>=0){
                    lamps = new String[act.execDevs.get(di).getOutCount()];
                    for (int i = 0; i <lamps.length ; i++) {
                        String lItem = act.execDevs.get(di).getLampText(i);
                        if (lItem.equals("")){
                            lamps[i] = getString(R.string.lightgroup)+" "+(i+1);
                        }else{
                            lamps[i] = lItem;
                        }
                    }
                }else{
                    lamps = new String[8];
                    for (int i = 0; i <lamps.length ; i++) {
                        lamps[i] = getString(R.string.lightgroup)+" "+(i+1);
                    }
                }

                final boolean[] mChecked = new boolean[lamps.length];
                for (int i = 0; i <lamps.length ; i++) {
                    mChecked[i] = ((1<<i)&controls.get(cInd).getOutMask())>0;
                }

                dlg.setMultiChoiceItems(lamps, mChecked, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                        mChecked[i] = b;
                    }
                });

                dlg.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        int num = controls.get(cInd).getNum();
                        String textNum = String.format(Locale.getDefault(), "N%02d%03d", mPage, num);

                        int ms = 0;
                        for (int j = 0; j <mChecked.length ; j++) {
                            if (mChecked[j]){
                                ms = ms + (1<<j);
                            }
                        }
                        controls.get(cInd).setOutMask(ms);
                        act.saveInt(BTN_OUTMASK+textNum, ms);

                        num = act.getDevIndex(controls.get(cInd).getNumDev());
                        if (num>=0){
                            updateButtonsState(controls.get(cInd).getNumDev(), act.execDevs.get(num).getOutState());
                        }
                    }
                });

                dlg.setNegativeButton(getText(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
                dlg.show();

                return true;


            case M_SET_CHNG:
                dlg = new AlertDialog.Builder(getActivity());
//                cInd = getControlIndexByNum(btnNum);
                int numDev = act.getDevNumChange();
                int outMask = act.getMaskChange();
                int lastCmd = act.getLastCommand();

                LayoutInflater inflater = getLayoutInflater();
                View v = inflater.inflate(R.layout.last_change_getter, vGroup, false);

                TextView cmdTitle = v.findViewById(R.id.cmdN_text);
                txt = getText(R.string.set_cmd_num) + " (" + Integer.toString(controls.get(cInd).getCmdNum()) + ")";
                cmdTitle.setText(txt);
                final EditText cmdN = v.findViewById(R.id.cmdN_edit);
                cmdN.setSelectAllOnFocus(true);
                txt = Integer.toString(lastCmd);
                cmdN.setText(txt);

                TextView ndevTitle = v.findViewById(R.id.devN_text);
                txt = getText(R.string.set_dev_num) + " (" + Integer.toString(controls.get(cInd).getNumDev()) + ")";
                ndevTitle.setText(txt);
                final EditText devN = v.findViewById(R.id.devN_edit);
                devN.setSelectAllOnFocus(true);
                txt = Integer.toString(numDev);
                devN.setText(txt);

                TextView maskTitle = v.findViewById(R.id.mask_text);
                txt = getText(R.string.set_outmask) + " (" + Integer.toString(controls.get(cInd).getOutMask()) + ")";
                maskTitle.setText(txt);
                final EditText msk = v.findViewById(R.id.mask_edit);
                msk.setSelectAllOnFocus(true);
                txt = Integer.toString(outMask);
                msk.setText(txt);

                dlg.setView(v);
                dlg.setTitle(controls.get(cInd).getText());
                dlg.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        int numDev = Integer.parseInt(devN.getText().toString()) ;
                        int lastCmd = Integer.parseInt(cmdN.getText().toString()) ;
                        int outMask = Integer.parseInt(msk.getText().toString()) ;
                        controls.get(cInd).setNumDev(numDev);
                        controls.get(cInd).setCmdNum(lastCmd);
                        controls.get(cInd).setOutMask(outMask);
//                        String strBtnNum = String.format(Locale.getDefault(), "N%02d%03d", mPage, btnNum);
                        act.saveInt(BTN_NUMDEV + suff, numDev);
                        act.saveInt(BTN_OUTMASK + suff, outMask);
                        act.saveInt(BTN_CMD + suff, lastCmd);

                        int dInd = act.getDevIndex(numDev);
                        if (dInd>=0){
                            updateButtonsState(numDev, act.execDevs.get(dInd).getOutState());
                        }
                    }
                });
                dlg.setNegativeButton(getText(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
                dlg.show();

            return true;

//----------------------------------------------------------------IS SENSOR---------------------------------------------------------------------

            case M_REN_SNS:
                dlg = new AlertDialog.Builder(getActivity());
                final EditText sensNameText = new EditText(getActivity());
                sensNameText.setSelectAllOnFocus(true);
                sensNameText.setText(controls.get(cInd).getText());
                dlg.setView(sensNameText);
                dlg.setTitle(R.string.msg_rename);
                dlg.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        controls.get(cInd).setText(sensNameText.getText().toString());
                        act.saveStr(BTN_TEXT + suff, sensNameText.getText().toString());
                        View snsCell = (View) selectedView.getParent();
                        TextView dnText = snsCell.findViewById(R.id.sns_text);
                        if (dnText!=null){
                            dnText.setText(controls.get(cInd).getText());
                        }
                    }
                });
                dlg.setNegativeButton(getText(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
                dlg.show();

                return true;

            case M_TEMP_SNS:
//                cInd = getControlIndexByNum(btnNum);
                controls.get(cInd).setSensType(IS_TEMP);
                controls.get(cInd).setUpText(getString(R.string.temperature));
                act.saveStr(BTN_UP_TEXT+suff, getString(R.string.temperature));
                View snsCell = (View) selectedView.getParent();
                TextView upText = snsCell.findViewById(R.id.sns_up_text);
                if (upText!=null){
                    upText.setText(controls.get(cInd).getUpText());
                }
                act.saveInt(SNS_TYPE+suff, IS_TEMP);
                int sInd = act.getSnsIndex(controls.get(cInd).getNumDev());
                if (sInd>=0){
                    ((TextView)selectedView).setText(act.sensors.get(sInd).getValue(IS_TEMP));
                }
                return true;

            case M_HUM_SNS:
//                cInd = getControlIndexByNum(btnNum);
                controls.get(cInd).setSensType(IS_HUM);
                controls.get(cInd).setUpText(getString(R.string.humidity));
                act.saveStr(BTN_UP_TEXT+suff, getString(R.string.humidity));
                snsCell = (View) selectedView.getParent();
                upText = snsCell.findViewById(R.id.sns_up_text);
                if (upText!=null){
                    upText.setText(controls.get(cInd).getUpText());
                }
                act.saveInt(SNS_TYPE+suff, IS_HUM);
                sInd = act.getSnsIndex(controls.get(cInd).getNumDev());
                if (sInd>=0){
                    ((TextView)selectedView).setText(act.sensors.get(sInd).getValue(IS_HUM));
                }
                return true;

            case M_PRES_SNS:
//                cInd = getControlIndexByNum(btnNum);
                controls.get(cInd).setSensType(IS_PRESS);
                controls.get(cInd).setUpText(getString(R.string.pressure));
                act.saveStr(BTN_UP_TEXT+suff, getString(R.string.pressure));
                snsCell = (View) selectedView.getParent();
                upText = snsCell.findViewById(R.id.sns_up_text);
                if (upText!=null){
                    upText.setText(controls.get(cInd).getUpText());
                }
                act.saveInt(SNS_TYPE+suff, IS_PRESS);
                sInd = act.getSnsIndex(controls.get(cInd).getNumDev());
                if (sInd>=0){
                    ((TextView)selectedView).setText(act.sensors.get(sInd).getValue(IS_PRESS));
                }
                return true;

            case M_SHOW_STAT:
//                cInd = getControlIndexByNum(btnNum);
                int typ = controls.get(cInd).getSensType();
                dNum = controls.get(cInd).getNumDev();
                int model = controls.get(cInd).getSensModel();
                byte[] buf = {SET_W_COMMAND, (byte) dNum, 3, (byte) CMD_ASK_STATISTIC, (byte) typ};
                if (act.askUDP(buf, MSG_RE_SENT_W, CMD_MSG_STATISTIC)){

                    byte[] stat = act.sUDP.getWB();
                    int count = stat[7]&0xFF;
                    ArrayList<Float> data = new ArrayList<>();
                    for (int i = count-1; i >=0 ; i--) {
                        int val;
                        switch (stat[4]){
                            case IS_PRESS:
                                val = ((stat[i*3+8]&0xFF)<<16 | (stat[i*3+9]&0xFF)<<8 | stat[i*2+10]&0xFF);
                                break;
                            default:
                                val = ((stat[i*2+8]&0xFF)<<8 | stat[i*2+9]&0xFF);
                                break;

                        }
                        if (val>0){
                            data.add(getFloatSensorValue(val, model, typ));
                        }
                    }


                    Intent intent = new Intent();
                    intent.putExtra("snsNum", dNum);
                    intent.putExtra("statBuff", data);
                    intent.putExtra("snsType", typ);
                    intent.putExtra("period", (stat[5]&0xFF)<<8 | stat[6]&0xFF);
//                    intent.putExtra("deviceIP", act.deviceIP);
//                    intent.putExtra("devPort", act.devPort);
                    intent.putExtra("localPort", act.localPort);
                    intent.putExtra("measureTyp", controls.get(cInd).getUpText());
                    intent.putExtra("snsText", controls.get(cInd).getText());
                    intent.setClass(act.getApplicationContext(), StatActivity.class);
                    startActivity(intent);
                }
                return true;

            case M_DEL_SNS:
//                cInd = getControlIndexByNum(btnNum);
                if (controls.get(cInd).isBothSide()){
                    controls.get(cInd).setLocation(orientation, 0);
                    act.delKey(orientTag  + suff);

                } else {
                    String suffN = "N"+String.format(Locale.getDefault(), "%02d%03d", mPage, controls.get(cInd).getNum());
                    List<String> keys = act.readKeys(suffN);
                    for (int i = 0; i <keys.size() ; i++) {
                        Log.i(TAG, " delete key: " + keys.get(i));
                        act.delKey(keys.get(i));
                    }

                    controls.remove(cInd);

                }
//                LinearLayout txtgroup = (LinearLayout) selectedView.getParent();
                ctrElm = (LinearLayout) selectedView.getParent();
                cell = (LinearLayout) ctrElm.getParent();
                cell.removeAllViews();
//                TableRow.LayoutParams sparams = (TableRow.LayoutParams) cell.getLayoutParams();
//                sparams.span = 1;
//                cell.setLayoutParams(sparams);
                registerForContextMenu(cell);

                return true;

//----------------------------------------------------------------END---------------------------------------------------------------------

            case M_CANCEL:
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    BroadcastReceiver udpReciever = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            byte[] inBuf = intent.getByteArrayExtra("Buffer");
            byte[] rbuff = Arrays.copyOfRange(inBuf, 7, inBuf.length);
            Log.i(TAG, " onUDPreceive in fragment "+mPage+": = "+act.byteArrayToHex(inBuf, inBuf.length));
            parceFromHub(rbuff);
        }

    };

    private void parceFromHub(byte[] buf){
        switch (buf[0]){
            case MSG_RE_SENT_W:
                parceFromDevice(buf);
                break;
        }
    }

    private void parceFromDevice(byte[] buf){
        int devN = buf[4]&0xFF;
        int outState = buf[5]&0xFF;
        int cmd = buf[3]&0xFF;
        int orientation = act.getResources().getConfiguration().orientation;
        boolean found;
        int ind;
        switch (cmd){
            case MSG_DEV_TYPE:
                /*
                Log.i(TAG, " PageFragment, get cmd_Msg_DevType for "+devN+",  = " + outState);
                if (buf[6]>0){
                    found = false;
                    for (int i = 0; i <execDevs.size() ; i++) {
                        if (execDevs.get(i).getDevNum()==(buf[1]&0xFF)){
                            found = true;
                        }
                    }
                    if (!found){
                        execDevs.add(new ExecDevice(buf[1], (byte) 0));
                    Log.i(TAG, " Fragment, add Device. execDevs.size() = "+execDevs.size());
                    }
                }
                break;
                */
            case CMD_SEND_COMMAND:
//                alastCommand = (buf[4]&0xFF)*0x100 + buf[5]&0xFF;
                break;

            case MSG_OUT_STATE:
                updateButtonsState(devN, outState);
                break;

            case MSG_STATE:
                updateButtonsState(devN, outState);
                break;

            case MSG_DEVICE_KIND:
                /*
                if (getSnsIndex(buf[1]&0xFF)<0) {
                    sensors.add(new SensorDevice(buf[1] & 0xFF, buf[4] & 0xFF, 0, 0, 0));
                }
                */

                break;

            case CMD_MSG_ANALOG_DATA:
                updateSensorState(buf[4]&0xFF);
                break;

            case MSG_SENSOR_STATE:
                Log.i(TAG, "MSG_SENSOR_STATE in fragment");
                updateSensorState(buf[1]&0xFF);
                break;

        }
    }


    private String getTypeSensor(int typ){
        switch (typ){
            case IS_DS18B20 : return "DS18B20";
            case IS_SHT21 : return "SHT21";
            case IS_BMP180 : return "BMP180";
            default: return "";
        }
    }



    private void updateButtonsState(int numDev, int outState){
        int rows = mTable.getChildCount();
        int orientation = act.getResources().getConfiguration().orientation;
        for (int row = 0; row <rows ; row++) {
            ViewGroup vcols = (ViewGroup) mTable.getChildAt(row);
            int cols = vcols.getChildCount();
            for (int col = 0; col <cols ; col++) {
                View cell = vcols.getChildAt(col);
                View ctrlView = cell.findViewById(R.id.ctr_elm);
                if (ctrlView !=null){
                    int cInd = getControlIndexByLocation(orientation, String.format(Locale.getDefault(), "%02d%02d", row+1, col+1));
                    if (cInd>=0){
                        if (controls.get(cInd).getNumDev()==numDev){
                            View btn = ctrlView.findViewById(R.id.ctr_button);
                            setButtonVisualState((Button) btn, cInd, outState);
                        }
                    }
                }
            }
        }
    }

    private void updateSensorState(int numDev){
        int rows = mTable.getChildCount();
        int sensInd = act.getSnsIndex(numDev);
        if (sensInd>=0){
            int orientation = act.getResources().getConfiguration().orientation;
            for (int row = 0; row <rows ; row++) {
                ViewGroup vcols = (ViewGroup) mTable.getChildAt(row);
                int cols = vcols.getChildCount();
                for (int col = 0; col <cols ; col++) {
                    View cell = vcols.getChildAt(col);
                    TextView snsValue = cell.findViewById(R.id.sns_value);
                    if (snsValue !=null){
                        int cInd = getControlIndexByLocation(orientation, String.format(Locale.getDefault(), "%02d%02d", row+1, col+1));
                        if (cInd>=0){
                            if (controls.get(cInd).getNumDev()==numDev){
                                snsValue.setText(act.sensors.get(sensInd).getValue(controls.get(cInd).getSensType()));
                            }
                        }
                    }
                }
            }

        }
    }




    private void setButtonVisualState(Button btn, int controlIndex, int outState){
        if (btn!=null){
            int mask = controls.get(controlIndex).getOutMask();
            if ((mask&outState)>0){
                btn.setBackgroundResource(R.drawable.sq_btn_activ_color);
            }else{
                btn.setBackgroundResource(R.drawable.sq_btn_color);
            }
//            ((GradientDrawable)btn.getBackground().getCurrent()).setShape(GradientDrawable.OVAL);
        }
    }

    private void setSensorData(TextView sns, int snsIndex, int sensType){
        if (sns!=null){
            sns.setText(act.sensors.get(snsIndex).getValue(sensType));
        }
    }

    private View findViewAiLocation(int aRow, int aCol){
        ViewGroup vcols = (ViewGroup) mTable.getChildAt(aRow-1);
        return vcols.getChildAt(aCol-1);
    }


//-----------------------------------------------controls functions---------------------------------------------------------

    private int getControlIndexByNum(int num){
        int ind = controls.size();
        do {
            ind--;
        } while ((ind>=0)&&(controls.get(ind).getNum()!=num));
        return ind;
    }

    private int getControlIndexByLocation(int orientation, String location){
        int ind = controls.size();
        do {
            ind--;
        } while ((ind>=0)&&(!controls.get(ind).getLocation(orientation).equals(location)));
        return ind;
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



}
