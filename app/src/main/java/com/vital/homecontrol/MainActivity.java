package com.vital.homecontrol;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.support.design.widget.TabLayout;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static java.lang.Thread.currentThread;
import static java.lang.Thread.sleep;


public class MainActivity extends AppCompatActivity implements UDPserver.UDPlistener {

    private static final String TAG = "MyclassMain";
    
    private Config config;
    private Properties namesFile;
    private String configPath;
//    public String namesFileName;
    SharedPreferences prefs;

    static final String ROOM_NAME_KEY = "RoomName";
    static final int RK_SETTING = 1001;
    static final int BC_Dev = 0x7F;
//    static final String UDP_RCV = "UDP_received";
    static final String STATE_CONNECTED = "Connected";
    static final String STATE_WIFI = "WorkWiFi";
    static final String STATE_EXECDEVS = "ExecDevices";
    static final String STATE_SENSORS = "Sensors";
    static final String STATE_DESTIP = "DestIP";
    static final String STATE_LICONIP = "LiConIP";
    static final String STATE_REMOTE = "Remote";
    static final String STATE_LASTSMD = "LastDevNum";
    static final String STATE_LASTCHNG = "LastChange";

    static final int STUN_RESPONCE = 0x0101;
    static final byte ASK_FOR_DATA = 0x41;
    static final byte DATA_NOT_LOAD = 0x42;
//    static final byte DATA_SUCSESS = 0x43;
    static final byte LICON_NAMES = 0x45;
//    static final byte CMD_DATA = 0x46;

    static final int ASK_IP                     =  0x01;
    static final int BREAK_LINK                 =  0x02;
    static final int ASK_COUNT_DEVS             =  0x04;
    static final int SET_W_COMMAND		   	    =  0x05;

    static final int SET_ALT_UD_PPORT           =  0x06;
    static final int READ_ALT_UD_PPORT          =  0x07;
    static final int ASK_LEASING                =  0x09;
    static final int ASK_COUNT_SENSORS          =  0x0A;
    static final int SET_ALT_PREFIX             =  0x0B;
    static final int READ_ALT_PREFIX            =  0x0C;
    static final int FIND_DEV                   =  0x0E;

    static final int MSG_RE_SENT_W              =  0x11;
    static final int MSG_ANSW_LEASING           =  0x12;
    static final int MSG_PREFIX                 =  0x13;
    static final int MSG_BAD_SEND_CRC           =  0x16;
    static final int MSG_ALT_UD_PPORT           =  0x17;
    static final int MSG_LIST_ADD_ON_DEVICES    =  0x18;
    static final int MSG_ANSW_IP                =  0x19;
    static final int MSG_LIST_SENSORS           =  0x1A;
    static final int MSG_LIST_DEVS              =  0x1B;
    static final int MSG_ADD_DEVICE             =  0x1C;
    static final int MSG_FOUND_DEV              =  0x1E;
    static final int MSG_W_ERROR                =  0x1F;
    static final int CMD_SET_TIME               =  0x31;
    static final int ASK_SERIAL                 =  0x32;


    static final int MSG_SERIAL                 =  0x4E;
    static final int Msg_LiconIP 	    	    =  0x55;


    static final int MSG_RCV_OK                 =  0xA5;
    static final int CMD_SEND_COMMAND           =  0x88;

    static final int MSG_STATE                  =  0x61;
    static final int MSG_OUT_STATE              =  0x64;
    static final int MSG_EEP_DATA               =  0x65;
    static final int MSG_SENSOR_STATE           =  0x67;
    static final int MSG_DEV_TYPE               =  0x6E;

    static final int CMD_ASK_STATE              =  0x91;
    static final int CMD_ASK_MEM                =  0x92;
    static final int CMD_ASK_SENSOR_STATE       =  0x97;
    static final int CMD_ASK_TYPE               =  0x9E;

    static final int PLACE_GUEST_DATA           =  0xA2;
    static final int MSG_CELL_DATA              =  0xA9;
    static final int MSG_GUEST_DATA_PLACED      =  0xAB;
    static final int MSG_HOST_NOT_FOUND         =  0xAF;

    static final int CMD_ASK_DEVICE_KIND        =  0xC0;
    static final int CMD_MSG_ANALOG_DATA        =  0xC1;
    static final int CMD_MSG_STATISTIC          =  0xCA;
    static final int MSG_DEVICE_KIND            =  0xCF;

    static final int CMD_ASK_STATISTIC          =  0xEA;

    static final int NO_CONFIRM                 =  0xFF;

    public static final int NUMBER_OF_REQUEST   = 23401;

    static final int MSG_END_CONNECTING         =  0x01;
    static final int MSG_END_CONNECTTASK        =  0x02;
    static final int MSG_GOT_MAP_ADDR           =  0x03;
    static final int MSG_GOT_PEER_ADDR          =  0x04;
    static final int MSG_NO_HOST                =  0x05;
    static final int MSG_NO_STUN_ANSWER         =  0x06;
    static final int MSG_NO_SIGNAL_ANSWER       =  0x07;
    static final int MSG_END_GET_COMMAND        =  0x08;


    ViewPager viewPager;
    ProgressBar pBar;
    RoomAdapter roomAdapter;
    TabLayout tabLayout;
    TextView statusText;
    TextView destIPtext;
    TextView localIPtext;
    TextView devCountText;
    TextView sensCountText;
    TextView fText;
    UDPserver sUDP;


//    Timer timer;
//    TimerTask task;


//    private ConnectivityManager connMgr;
//    private OnFragmentInteractionListener mListener;
    private NetworkInfo netInfo;
    private WifiManager wifiMgr;
//    private Boolean isShowDialog = false;
    private Boolean netRecieverRegistered = false;
//    private Boolean udpRecieverRegistered = false;
//    private Boolean msgRecieverRegistered;
    private Boolean workWiFi = false;
    private Boolean remote = false;
    private Boolean staticIP = false;
    private String remoteIP = "0.0.0.0";
    private int remPort = 0;
    private String mappedIP = "0.0.0.0";
    private int mappedPort = 0;
    private String signalIP = "0.0.0.0";
    private String peerIP = "0.0.0.0";
    private int peerPort = 0;
//    private int signalPort = 16133;
    public int defaultPort = 55555;
    public String devLocalIP;
    private String liconIP = "";
    public boolean connected;
    private int pass;
    private int serial = 0;
    private int curserial;
    public List<ExecDevice> execDevs = new ArrayList<>();
    public List<SensorDevice> sensors = new ArrayList<>();
    public int lastCommand = 0;
    public int changedState = 0;
    private String theme;
    public String storageDir;
    private int timeout;
    private boolean sucsessReadMem;
    private String liconName = "";
//    public boolean cnf;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        storageDir = Environment.getExternalStorageDirectory().toString()+"/HomeControl";
        String fPrefFile = this.getPackageName()+ "_preferences.xml";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            fPrefFile = PreferenceManager.getDefaultSharedPreferencesName(this) + ".xml";
        }
        String fPref = "data/data/"+this.getPackageName()+"/shared_prefs/"+fPrefFile;
        copyFile( storageDir+"/Preferences/preferences.xml", fPref);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        theme = prefs.getString("key_theme", "");
        switch (Objects.requireNonNull(theme)){
            case "Dark":
                setTheme(R.style.AppThemeDark);
                break;
            case "Light":
                setTheme(R.style.AppTheme);
                break;
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pBar = findViewById(R.id.progressBar);
        pBar.setVisibility(View.INVISIBLE);
//        pBar.setLayoutParams(new LinearLayout.LayoutParams(0, 0));
        statusText = findViewById(R.id.status_text);
        statusText.setText("");
        destIPtext = findViewById(R.id.target_IP_text);
        destIPtext.setText("");
        localIPtext = findViewById(R.id.local_IP_text);
        localIPtext.setText("");
        devCountText = findViewById(R.id.devcount_text);
        devCountText.setText("0");
        sensCountText = findViewById(R.id.senscount_text);
        sensCountText.setText("0");
        fText = findViewById(R.id.f_text);



        initiateStorage();

        updateConfig();

        viewPager = findViewById(R.id.viewPager);
        roomAdapter = new RoomAdapter(getSupportFragmentManager(), config.getValues(ROOM_NAME_KEY));
        viewPager.setAdapter(roomAdapter);

        tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager, true);

        wifiMgr = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        readSetting();

        sUDP = (UDPserver)getLastCustomNonConfigurationInstance();

        if (sUDP==null){
            sUDP = new UDPserver(getApplicationContext(), pass, this);
            sUDP.setSignalIP(signalIP);
            sUDP.start();
            Log.i(TAG, "new sUDP: "+sUDP.toString() );
        }
        Log.i(TAG, "OnCreate" );


        /*
        task = new TimerTask() {
            @Override
            public void run() {
                Log.i(TAG, "TimerTask" );

            }
        };

        timer = new Timer();

         */

    }

    private void readSetting(){
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        remoteIP = prefs.getString("key_remIP", "0.0.0.0");
        signalIP = prefs.getString("key_signalIP", "0.0.0.0");
        staticIP = prefs.getBoolean("id_cb_StaticIP", false);
        remPort = Integer.parseInt(Objects.requireNonNull(prefs.getString("key_port", "0")));
        serial = Integer.parseInt(Objects.requireNonNull(prefs.getString("key_set_serial", "0")));
        pass = Integer.parseInt(Objects.requireNonNull(prefs.getString("key_udppass", "0"))) | 0x800000;
        workWiFi = prefs.getBoolean("id_cb_WorkWiFi", false);
        timeout = Integer.parseInt(Objects.requireNonNull(prefs.getString("key_timeout", "500")));


    }

    private void initiateStorage(){

        //https://toster.ru/q/302804

        boolean canWrite;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){        // API23, Android 6.0
            canWrite = (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED);
            if (!canWrite){
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, NUMBER_OF_REQUEST);
            }
        }else{
            canWrite =true;
        }

        namesFile = new Properties();
        String namesFileName = prefs.getString("key_names", "");
        if (!Objects.equals(namesFileName, "")){
            File fileN = new File(this.getFilesDir().toString(), namesFileName);
            if (fileN.exists()){
                try {
                    namesFile.load(new FileInputStream(fileN));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        if (canWrite){
            File file = new File(storageDir, "");
            if (!file.exists()){
                if (!file.mkdirs()){
                    storageDir = this.getFilesDir().toString();
                    canWrite = false;
                    configPath = storageDir;
                    Log.i(TAG, "directory "+storageDir+" not created, save to FilesDir");
                }
            }
        }

        if (canWrite){
            configPath = storageDir + "/Config";
            File file = new File(configPath, "");
            if (!file.exists()){
                if (!file.mkdirs()){
                    configPath = storageDir;
                    Log.i(TAG, "directory "+configPath+" not created, save to FilesDir");
                }
            }
            file = new File(storageDir+"/Preferences", "");
            if (!file.exists()){
                if (!file.mkdirs()){
//                    Toast.makeText(this, "Unable create Names directory", Toast.LENGTH_SHORT).show();
                    Log.i(TAG, "directory /Preferences not created");
                }
            }


        }


    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == NUMBER_OF_REQUEST) {
            // https://stackoverflow.com/questions/15564614/how-to-restart-an-android-application-programmatically
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.e("TAG", "Пользователь дал разрешение");
                Intent mStartActivity = new Intent(MainActivity.this, MainActivity.class);
                int mPendingIntentId = 123456;
                PendingIntent mPendingIntent = PendingIntent.getActivity(MainActivity.this, mPendingIntentId, mStartActivity,
                        PendingIntent.FLAG_CANCEL_CURRENT);
                AlarmManager mgr = (AlarmManager) MainActivity.this.getSystemService(Context.ALARM_SERVICE);
                mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 200, mPendingIntent);
                System.exit(0);
            } else {
                Log.e("TAG", "Пользователь отклонил разрешение");
            }
        }
    }

    @Override
    public UDPserver onRetainCustomNonConfigurationInstance() {
        Log.i(TAG, "onRetainCustomNonConfigurationInstance, sUDP = "+ (sUDP==null? "null" : sUDP.toString()));
        return sUDP;
    }

    // http://developer.alexanderklimov.ru/android/theory/activity_methods.php
// http://developer.alexanderklimov.ru/android/theory/parcelable.php
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.i(TAG, " onSaveInstanceState, deviceIP = " + devLocalIP );
        outState.putBoolean(STATE_CONNECTED, connected);
        outState.putString(STATE_DESTIP, devLocalIP);
        outState.putBoolean(STATE_WIFI, workWiFi);
        outState.putBoolean(STATE_REMOTE, remote);
        outState.putParcelableArrayList(STATE_EXECDEVS, (ArrayList<? extends Parcelable>) execDevs);
        outState.putParcelableArrayList(STATE_SENSORS, (ArrayList<? extends Parcelable>) sensors);
        outState.putInt(STATE_LASTSMD, lastCommand);
        outState.putInt(STATE_LASTCHNG, changedState);
        outState.putString(STATE_LICONIP, liconIP);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        connected = savedInstanceState.getBoolean(STATE_CONNECTED);
        devLocalIP = savedInstanceState.getString(STATE_DESTIP);
        workWiFi = savedInstanceState.getBoolean(STATE_WIFI);
        remote = savedInstanceState.getBoolean(STATE_REMOTE);
        execDevs = savedInstanceState.getParcelableArrayList(STATE_EXECDEVS);
        sensors = savedInstanceState.getParcelableArrayList(STATE_SENSORS);
        lastCommand = savedInstanceState.getInt(STATE_LASTSMD);
        changedState = savedInstanceState.getInt(STATE_LASTCHNG);
        liconIP = savedInstanceState.getString(STATE_LICONIP);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, " onPause in MainActivity " );
        if (sUDP!=null){
            byte[] outBuf = {BREAK_LINK, 0};
//            sUDP.send(outBuf, outBuf.length, (byte) 0, 0, 0);
            askUDP(outBuf, 0, 0);
        }
        if (netRecieverRegistered){
            unregisterReceiver(netReciever);
            netRecieverRegistered=false;
        }
//        if (udpRecieverRegistered){
//            LocalBroadcastManager.getInstance(this).unregisterReceiver(udpReciever);
//            udpRecieverRegistered=false;
//        }


//        timer.cancel();
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.i(TAG, " onResume in MainActivity " );
//        timer.schedule(task, 1000, 2000);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i(TAG, " onRestart in MainActivity " );
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, " onStart in MainActivity ");
        IntentFilter ifilter = new IntentFilter();
        ifilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

        registerReceiver(netReciever,ifilter);
        netRecieverRegistered=true;

//        LocalBroadcastManager.getInstance(this).registerReceiver(udpReciever, new IntentFilter(UDP_RCV));
//        udpRecieverRegistered = true;

    }

    @Override
    protected void onDestroy() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("id_cb_WorkWiFi",workWiFi);
        editor.apply();
        clonePrefs();
        Log.i(TAG, " onDestroy in MainActivity ");
        super.onDestroy();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
// https://habr.com/post/222295/ - Menu
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_add_room:
                roomNameDialog("",false);
                return true;

            case R.id.action_ren_room:
                roomNameDialog((String) roomAdapter.getPageTitle(viewPager.getCurrentItem()),true);
                return true;

// https://stackoverflow.com/questions/10396321/remove-fragment-page-from-viewpager-in-android

// look https://stackoverflow.com/questions/13664155/dynamically-add-and-remove-view-to-viewpager
            case R.id.action_del_room:
                int rooms = roomAdapter.getCount();
                Log.i(TAG, "rooms = " + rooms);
                int ind = viewPager.getCurrentItem();


//                roomAdapter.delPage(ind);
//                roomAdapter.notifyDataSetChanged();

//                rooms = roomAdapter.getCount();
//                Log.i(TAG, "rooms = " + rooms);



                if (ind == rooms-1){
                    viewPager.setCurrentItem(ind-1);
                }
//                viewPager.removeViewAt(ind);
//                roomAdapter.notifyDataSetChanged();
                int count = deletePage(ind+1, rooms);
                Toast.makeText(this, "Deleted "+count+ " entries", Toast.LENGTH_SHORT).show();
                recreate();

/*
                Intent mStartActivity = new Intent(MainActivity.this, MainActivity.class);
                int mPendingIntentId = 123456;
                PendingIntent mPendingIntent = PendingIntent.getActivity(MainActivity.this, mPendingIntentId, mStartActivity,
                        PendingIntent.FLAG_CANCEL_CURRENT);
                AlarmManager mgr = (AlarmManager) MainActivity.this.getSystemService(Context.ALARM_SERVICE);
                mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 50, mPendingIntent);
                System.exit(0);
 */

                return true;

            case R.id.action_update:

                /*
                if (remote){
                    connectToHost(remoteIP, remPort);
                }else{
                    connectToHost(stringIP(getBroadcastWiFiIP()), defaultPort);
                }

                 */
                return true;

            case R.id.action_settings:
                Intent intent = new Intent();
                intent.setClass(this, SettingActivity.class);
                startActivityForResult(intent, RK_SETTING);
                return true;

            case R.id.action_GetNames:
                askLiConData();
                return true;

            case R.id.action_GetCommands:
                getModulesCommands();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, " onActivityResult " + requestCode+" "+resultCode);
        if (requestCode==RK_SETTING){
            clonePrefs();
            readSetting();

            if (sUDP!=null){
                sUDP.setPass(pass);
                sUDP.setSignalIP(signalIP);
//                sUDP.setDestPort(remPort);
//                sUDP.setDestIP(remoteIP);
            }
            if (!theme.equals(prefs.getString("key_theme", ""))){
                recreate();
            }
        }
    }

    private void clonePrefs(){
        String fPrefFile = this.getPackageName()+ "_preferences.xml";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            fPrefFile = PreferenceManager.getDefaultSharedPreferencesName(this) + ".xml";
        }
        String fPref = "data/data/"+this.getPackageName()+"/shared_prefs/"+fPrefFile;
        copyFile(fPref, storageDir+"/Preferences/preferences.xml");

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item;
        if (roomAdapter.getCount()==0){
            item = menu.findItem(R.id.action_ren_room);
            item.setVisible(false);
            item = menu.findItem(R.id.action_del_room);
            item.setVisible(false);
        }else{
            item = menu.findItem(R.id.action_ren_room);
            item.setVisible(true);
            item = menu.findItem(R.id.action_del_room);
            item.setVisible(true);
        }
        if (netInfo==null){
            item = menu.findItem(R.id.action_update);
            item.setVisible(false);
        }else{
            item = menu.findItem(R.id.action_update);
            item.setVisible(true);
        }
        if (liconIP.equals("")){
            menu.findItem(R.id.action_GetNames).setVisible(false);

        }else{
            item = menu.findItem(R.id.action_GetNames);
            item.setVisible(true);
            String titl;
            if (liconName.equals("")){
                titl = getText(R.string.getnames)+" "+liconIP;
            }else{
                titl = getText(R.string.getnames)+" "+liconName;
            }
            item.setTitle(titl);
        }
        if (execDevs.size()>0){
            item =menu.findItem(R.id.action_GetCommands);
            item.setVisible(true);
        }else{
            item =menu.findItem(R.id.action_GetCommands);
            item.setVisible(false);
        }


        return super.onPrepareOptionsMenu(menu);
    }

    public void addRoom (String name){
        int ind = 1;




        while (!config.getStr(ROOM_NAME_KEY + String.format(Locale.getDefault(), "%02d", ind)).equals("")){
            ind++;
        }
        Log.i(TAG, " Add Room: " + ind);

        if (name.length() == 0){
            name = getString(R.string.defaultPageName)+ " " + String.format(Locale.getDefault(),"%02d", ind);
        }
        config.setStr(String.format(Locale.getDefault(),"%s%02d", ROOM_NAME_KEY, ind), name);
        roomAdapter.addRoom(name);
        viewPager.setCurrentItem(ind);


    }

    public void roomNameDialog(String name, final boolean rename){
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        final EditText input = new EditText(this);
        alert.setView(input);
        if (rename){
            alert.setTitle(R.string.msg_rename);
            input.setText(name);
            input.setSelectAllOnFocus(true);
            input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        }else{
            alert.setTitle(R.string.add_page);
        }

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String value = input.getText().toString().trim();
                if (rename){
                    config.setStr(String.format(Locale.getDefault(),"%s%02d", ROOM_NAME_KEY, viewPager.getCurrentItem()+1), value);
//                    roomAdapter.setPageName(value, viewPager.getCurrentItem());
                    recreate();
                }else{
                    addRoom(value);
                }
            }
        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        if (rename){
            Objects.requireNonNull(alert.show().getWindow()).setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }else{
            alert.show();
        }
    }
//-------------------------------------------------------------------------------------------------------------------------------------------------

    private BroadcastReceiver netReciever = new BroadcastReceiver() {


        @Override
        public void onReceive(Context context, Intent intent) {
//            connMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
            netInfo = ((ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
            Log.i(TAG, "On Broadcast receive: " + (netInfo==null? "null" : netInfo.getTypeName()));

            if (netInfo == null){
                setTitle(getString(R.string.app_name) + " : " + getString(R.string.no_net));
                if (wifiMgr.getWifiState() == WifiManager.WIFI_STATE_DISABLED) {       // WiFi is OFF
                    if (workWiFi) {
  //                      if (!isShowDialog) {
                            wifiOnDialog();
 //                       }
                    }
                }

            }else{
                if (!staticIP){
                    getMappedAddress();
                }
                switch (netInfo.getType()){
                    case ConnectivityManager.TYPE_WIFI:
                        setTitle(getString(R.string.app_name)  +" : " + getString(R.string.typ_WiFi));

                        byte[] b = {0x51};
                        sUDP.sendLiCon(b, (byte) 0x01, stringIP(getBroadcastWiFiIP()));

                        workWiFi=true;
                        remote=false;
                        sUDP.setDestIP(stringIP(getBroadcastWiFiIP()));
                        sUDP.setDestPort(defaultPort);
                        connectingToHost();
                        break;
                    case ConnectivityManager.TYPE_MOBILE:
                        setTitle(getString(R.string.app_name)  +" : " + getString(R.string.typ_Mobile));
                        remote=true;
                        if (staticIP){
                            if ((remoteIP.equals("0.0.0.0"))||(remPort==0)){
                                Toast.makeText(getApplicationContext(), getText(R.string.no_remIP), Toast.LENGTH_LONG).show();
                            }else{
                                sUDP.setDestIP(remoteIP);
                                sUDP.setDestPort(remPort);
                                connectingToHost();
                            }
                        }else{
                            connectingThrouSignal();

                        }
                        break;
                }
            }
        }
    };

    public void wifiOnDialog(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Служба WiFi отключена")
                .setMessage("Включить WiFi?")
                .setCancelable(false)
                .setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        workWiFi = false;
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putBoolean("id_cb_WorkWiFi",workWiFi);
                        editor.apply();
                        clonePrefs();
//                        isShowDialog = false;
                    }
                })
                .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Log.i(TAG, " wifiOnDialog - yes");
                        statusText.setText(R.string.connecting);
                        wifiMgr.setWifiEnabled(true);
//                        isShowDialog = false;
                    }
                });
        AlertDialog alert = builder.create();
//        isShowDialog = true;
        alert.show();
    }



    public int getBroadcastWiFiIP(){
        return ((wifiMgr.getConnectionInfo().getIpAddress() & 0xffffff)|0xff000000);
    }

    public String stringIP(int ipAddress){
        return String.format(Locale.getDefault(), "%d.%d.%d.%d", (ipAddress & 0xff),(ipAddress >> 8 & 0xff),
                (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
    }

    public String getLocalIP(){
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();){
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIP = intf.getInetAddresses(); enumIP.hasMoreElements();){
                    InetAddress inetAddress = enumIP.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address){
                        return inetAddress.getHostAddress();
                    }
                }

            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return "";
    }

    /*  Variant with AsyncTask
    public class ConnectTask extends AsyncTask<String, Integer, Boolean> {


        @Override
        protected Boolean doInBackground(String... strings) {
            return null;
        }
    }
    */

    private boolean askIP(){
        byte[] outBuf = {ASK_IP};
        return  askUDP(outBuf, MSG_ANSW_IP, 0);
    }

    private boolean isIpFound(){
        connected = askIP();
        if (connected){
            devLocalIP = String.format(Locale.getDefault(),"%d.%d.%d.%d", sUDP.getWBbyte(1),sUDP.getWBbyte(2),sUDP.getWBbyte(3),sUDP.getWBbyte(4)); // & 0xFF need for unsigned
            Log.i(TAG, " got answer IP: "+devLocalIP );

            if (!remote){
                sUDP.setDestIP(devLocalIP);
            }
        }
        return connected;
    }

    private void connectingToHost(){

        statusText.setText(getString(R.string.connecting));
        connected = false;

        pBar.setVisibility(View.VISIBLE);

        new  Thread(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, " Create connecting thread: "+currentThread() + ", remote = " +remote );

                if (!isIpFound()){
                    isIpFound();
                }

                Log.i(TAG, "Send MSG_END_CONNECTING");
                Bundle bundle = new Bundle();
                Message msg = handler.obtainMessage();
                bundle.putInt("ThreadEnd", MSG_END_CONNECTING);
                msg.setData(bundle);
                handler.sendMessage(msg);

            }
        }).start();
    }

    private void connectingThrouSignal(){
        statusText.setText(getString(R.string.connecting));
        connected = false;
        pBar.setVisibility(View.VISIBLE);
        Log.i(TAG, "Start connectingThrouSignal");
        new Thread(new Runnable() {
            @Override
            public void run() {
                int att = 0;
                while ((peerIP.equals("0.0.0.0"))&&(att<2000)){
                    try {
                        TimeUnit.MILLISECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    att++;
                }
                Log.i(TAG, "connectingThrouSignal, time = "+att+"mS");
                if (att<2000){
                    Log.i(TAG, "connectingThrouSignal, connect to "+peerIP+" : "+peerPort);
                    sUDP.setDestIP(peerIP);
                    sUDP.setDestPort(peerPort);
                    remote = true;
                    if (!isIpFound()){
                        isIpFound();
                    }
                    Bundle bundle = new Bundle();
                    Message msg = handler.obtainMessage();
                    bundle.putInt("ThreadEnd", MSG_END_CONNECTING);
                    msg.setData(bundle);
                    handler.sendMessage(msg);
                }

            }
        }).start();
    }

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
           int state = bundle.getInt("ThreadEnd", 0);
           String st;
           switch (state){
               case MSG_END_CONNECTING:
                   Log.i(TAG, " get MSG_END_CONNECTING");
//                   pBar.setVisibility(View.INVISIBLE);
                   if (connected){
                       String link;
                       if (remote){
                           link = mappedIP + " : " + mappedPort + " ("+ getLocalIP() + ") <--> " + remoteIP +" ("+ devLocalIP + ")";

                       }else{
                           link = getLocalIP() + " <--> " + devLocalIP;
                       }
                       fText.setText(link);
                       taskAfterConnect();
                   }else{
                       if (remote){
                           if (staticIP){
                               st = getString(R.string.no_answer) + " " + remoteIP + " : "+remPort;
                           }else{
                               st = getString(R.string.no_answer) + " " + peerIP + " : "+peerPort;
                           }
                           statusText.setText(R.string.no_connect);
                           fText.setText("");
                           pBar.setVisibility(View.INVISIBLE);
                           Toast.makeText(getApplicationContext(), st, Toast.LENGTH_LONG).show();
                       }else{
                           if (staticIP){
                               if ((remoteIP.equals("0.0.0.0"))||(remPort==0)){
                                   Toast.makeText(getApplicationContext(), getText(R.string.no_remIP), Toast.LENGTH_LONG).show();
                                   statusText.setText(R.string.no_connect);
                                   fText.setText("");
                                   pBar.setVisibility(View.INVISIBLE);
                               }else{
                                   remote = true;
                                   sUDP.setDestIP(remoteIP);
                                   sUDP.setDestPort(remPort);
                                   connectingToHost();
                               }
                           }
                       }
                   }
                   break;
               case MSG_END_CONNECTTASK:
                   pBar.setVisibility(View.INVISIBLE);
                   devCountText.setText(String.format(Locale.getDefault(),"%d", execDevs.size()));
                   sensCountText.setText(String.format(Locale.getDefault(), "%d", sensors.size()));
                   statusText.setText(R.string.connected);
                   break;
               case MSG_GOT_MAP_ADDR:
                   st = mappedIP +" : " + mappedPort;
                   localIPtext.setText(st);
                   Log.i(TAG, " get MSG_GOT_MAP_ADDR: " + st);
                   if (!connected){
                       if (signalIP.equals("0.0.0.0")){
                           Toast.makeText(getApplicationContext(), getText(R.string.not_set_signal_IP), Toast.LENGTH_SHORT).show();
                       }else{
                           if ((!mappedIP.equals("0.0.0.0"))&&(mappedPort!=0)){
                               askSignal();
                           }
                       }
                   }
                   break;
               case MSG_NO_SIGNAL_ANSWER:
                   statusText.setText(R.string.no_connect);
                   fText.setText("");
                   pBar.setVisibility(View.INVISIBLE);
                   Toast.makeText(getApplicationContext(), getText(R.string.no_signal_IP) +" "+ signalIP, Toast.LENGTH_LONG).show();
                   break;
               case MSG_NO_HOST:
                   statusText.setText(R.string.no_connect);
                   fText.setText("");
                   pBar.setVisibility(View.INVISIBLE);
                   st = getString(R.string.device) + " " + serial + " " + getText(R.string.no_host_at_signal_IP);
                   Toast.makeText(getApplicationContext(), st, Toast.LENGTH_LONG).show();
                   break;
               case MSG_GOT_PEER_ADDR:
//                   pBar.setVisibility(View.INVISIBLE);
//                   Toast.makeText(getApplicationContext(), st, Toast.LENGTH_LONG).show();
                   break;
               case MSG_NO_STUN_ANSWER:
                   pBar.setVisibility(View.INVISIBLE);
                   if (!connected){
                       statusText.setText(R.string.no_connect);
                       fText.setText("");
                       Toast.makeText(getApplicationContext(), getText(R.string.no_stun), Toast.LENGTH_LONG).show();
                   }
                   break;
               case MSG_END_GET_COMMAND:
                   pBar.setVisibility(View.INVISIBLE);
                   if (sucsessReadMem){
                       saveCommandsFile();
                       Toast.makeText(getApplicationContext(), getText(R.string.Success), Toast.LENGTH_LONG).show();
                   }else{
                       Toast.makeText(getApplicationContext(), getText(R.string.error), Toast.LENGTH_LONG).show();
                   }
                   break;
           }
        }
    };

    private int getStunResponce(){
        byte[] buf = new byte[20];
        buf[1]=1;
        buf[18]= (byte) 0xFF;
        buf[19]= 0 ;

        sUDP.sendStunPacket(buf);
        int att = 0;
        while (sUDP.waitForStunUDP() & (att<300)){
            try {
                TimeUnit.MILLISECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            att++;
        }
        return att;
    }

    private void getMappedAddress(){

        Log.i(TAG, "Send Stun Request");
        mappedIP ="0.0.0.0";
        mappedPort=0;
        new Thread(new Runnable() {
            @Override
            public void run() {

                Bundle bundle = new Bundle();
                Message msg = handler.obtainMessage();
                int ind = 0;
                int att;
                do {
                    att=getStunResponce();
                    ind++;

                }while ((att>=300)&&(ind<10));
                if (ind<10){
                    byte[] inBuf = sUDP.getMappetData();                          // from 26 to 31
                    mappedIP = (inBuf[2]&0xFF)+"."+(inBuf[3]&0xFF)+"."+(inBuf[4]&0xFF)+"."+(inBuf[5]&0xFF);
                    mappedPort = ((inBuf[0]<<8)&0xFF00)+(inBuf[1]&0xFF);
                    bundle.putInt("ThreadEnd", MSG_GOT_MAP_ADDR);
                    Log.i(TAG, "Stun Responce: " + att + "mS in "+ind+" attempt");
                }else{
                    bundle.putInt("ThreadEnd", MSG_NO_STUN_ANSWER);
                    Log.i(TAG, "No STUN responce");
                }
                msg.setData(bundle);
                handler.sendMessage(msg);

            }
        }).start();

    }

    private void askSignal(){
        if (serial!=0){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.i(TAG, "ask signal "+signalIP);
                    byte[] buf = {(byte) PLACE_GUEST_DATA, (byte) ((serial >> 24)& 0xFF), (byte) ((serial >> 16)& 0xFF), (byte) ((serial >> 8)& 0xFF),
                            (byte) (serial & 0xFF), 0, 0, 0, 0, (byte) ((mappedPort >> 8)& 0xFF), (byte) (mappedPort & 0xFF)};


                    try {
                        Bundle bundle = new Bundle();
                        Message msg = handler.obtainMessage();
                        byte[] ip = InetAddress.getByName(mappedIP).getAddress();
                        System.arraycopy(ip, 0, buf, 5, 4);
                        if (askSigUDP(buf)){
                            byte[] sbuf = sUDP.getSignalBuffer();
                            switch (sbuf[0]){
                                case (byte)MSG_GUEST_DATA_PLACED:
                                    peerIP = String.format(Locale.getDefault(), "%d.%d.%d.%d", sbuf[5]&0xFF, sbuf[6]&0xFF, sbuf[7]&0xFF, sbuf[8]&0xFF);
                                    peerPort = (sbuf[9]&0xFF)*0x100+(sbuf[10]&0xFF);
                                    bundle.putInt("ThreadEnd", MSG_GOT_PEER_ADDR);
                                    Log.i(TAG, "Signal answer: "+peerIP+" : "+peerPort);
                                    break;
                                case (byte)MSG_HOST_NOT_FOUND:
                                    Log.i(TAG, "Signal answer: Host not found");
                                    bundle.putInt("ThreadEnd", MSG_NO_HOST);
                                    break;

                            }
                        }else{
                            bundle.putInt("ThreadEnd", MSG_NO_SIGNAL_ANSWER);
                            Log.i(TAG, "Signal server no answer");

                        }
                        msg.setData(bundle);
                        handler.sendMessage(msg);
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    }

                }
            }).start();

        }else{
            Toast.makeText(this, getText(R.string.no_serial), Toast.LENGTH_SHORT).show();
        }
    }

    private void getModulesCommands(){
        pBar.setVisibility(View.VISIBLE);
        new Thread(new Runnable() {
            @Override
            public void run() {
                sucsessReadMem = true;
                for (int di = 0; di < execDevs.size(); di++) {
                    execDevs.get(di).clearMem();
                    byte[] askBuf = {SET_W_COMMAND, (byte) execDevs.get(di).getDevNum(), 0x05, (byte) CMD_ASK_MEM, 1, 0, (byte) 0xB0};
                    if (askUDP(askBuf, MSG_RE_SENT_W, MSG_EEP_DATA)){
                        int count = sUDP.getWBbyte(7);
                        if (count == 0xFF) count = 0;
                        if (count>0){
                            execDevs.get(di).addMem(new byte[]{(byte) count});
                            int bytesToRead = count*5;
                            int offs = 0xB0+1;
                            while (bytesToRead>0){
                                int countRead = bytesToRead % 128;
                                bytesToRead -= countRead;
                                askBuf[4] = (byte) countRead;
                                askBuf[5] = (byte) (offs >> 8);
                                askBuf[6] = (byte) (offs & 0xFF);
                                if (askUDP(askBuf, MSG_RE_SENT_W, MSG_EEP_DATA)){
                                    byte[] eep = Arrays.copyOfRange(sUDP.getWB(), 7, 0xFFFF);
                                    execDevs.get(di).addMem(eep);
                                }else{
                                    sucsessReadMem = false;
                                }
                            }
                        }
                    }else{
                        sucsessReadMem = false;
                    }
                }
                Bundle bundle = new Bundle();
                Message msg = handler.obtainMessage();
                bundle.putInt("ThreadEnd", MSG_END_GET_COMMAND);
                msg.setData(bundle);
                handler.sendMessage(msg);

            }
        }).start();
    }

    private void saveCommandsFile(){
        ArrayList<Integer> cmdAr = new ArrayList<>();
        for (int di = 0; di < execDevs.size(); di++) {
            if (!execDevs.get(di).memEmpty()){
                int count = execDevs.get(di).getCmdCount();
                for (int i = 0; i < count; i++) {
                    int cmd = execDevs.get(di).getNumCmd(i);
                    if (cmdAr.indexOf(cmd)<0){
                        cmdAr.add(cmdAr.size(), cmd);
                    }
                }
            }
        }
        Collections.sort(cmdAr);
        ArrayList<String> list = new ArrayList<>();
        for (int ci = 0; ci < cmdAr.size(); ci++) {
            StringBuilder stCmd = new StringBuilder(String.format(Locale.getDefault(), "%05d=", cmdAr.get(ci)));
            for (int di = 0; di < execDevs.size(); di++) {
                String numD = String.format(Locale.getDefault(), "%02x", execDevs.get(di).getDevNum());
                for (int i = 0; i < execDevs.get(di).getCmdCount(); i++) {
                    if (execDevs.get(di).getNumCmd(i) == cmdAr.get(ci)){
                        stCmd.append(numD).append(execDevs.get(di).getCmdParamStr(i)).append(";");
                    }
                }
            }
            list.add(list.size(), stCmd.toString());
        }

        String fileName = getApplicationContext().getFilesDir().toString() + "/Commands";
        try {
            FileWriter writer = new FileWriter(fileName);
            for (int i = 0; i < list.size(); i++) {
                String str = list.get(i);
                writer.write(str);
//                if (i<list.size()-1){         //если надо чтоб пустую строку не выводило в конце
                    writer.write("\n");
//                }
            }
            writer.close();
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("key_commands", "Commands");
            editor.apply();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void taskAfterConnect(){

        new Thread(new Runnable() {
            @Override
            public void run() {

                byte[] outBuf = {(byte) CMD_ASK_TYPE};
                curserial=0;
                if (askUDP(outBuf, MSG_DEV_TYPE, 0)){
                    byte[] bufType = sUDP.getWB();
                    if (bufType.length>=7){
                        curserial=(bufType[3]&0xFF)*0x1000000+(bufType[4]&0xFF)*0x10000+(bufType[5]&0xFF)*0x100+(bufType[6]&0xFF);
                    }
                }
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt("curSerial",curserial);
                editor.apply();
                clonePrefs();

                outBuf[0] = ASK_COUNT_DEVS;
                if (askUDP(outBuf, MSG_LIST_DEVS, 0)){
                    int devsCount = sUDP.getWBbyte(1);
                    int[] devs = new int[devsCount];
                    for (int i = 0; i <devsCount ; i++) {
                        devs[i]=sUDP.getWBbyte(2+i);
                    }
                    Log.i(TAG, " get List Devices: "+ devsCount);
                    for (int i = 0; i <devsCount; i++) {
                        byte[] bufCount = {SET_W_COMMAND, (byte) devs[i], 2, (byte) CMD_ASK_TYPE};
                        askUDP(bufCount, MSG_RE_SENT_W, MSG_DEV_TYPE);
                    }
                }

                outBuf[0] = ASK_COUNT_SENSORS;
                if (askUDP(outBuf, MSG_LIST_SENSORS, 0)){
                    int sensCount = sUDP.getWBbyte(1);
                    int[] sens = new int[sensCount];
                    for (int i = 0; i <sensCount ; i++) {
                        sens[i]=sUDP.getWBbyte(2+i);
                    }
                    Log.i(TAG, " get List Sensors: "+ sensCount);
                    for (int i = 0; i <sensCount; i++) {
                        byte[] bufCount = {SET_W_COMMAND, (byte) sens[i], 2, (byte) CMD_ASK_DEVICE_KIND};
                        if (askUDP(bufCount, MSG_RE_SENT_W, MSG_DEVICE_KIND)){
                            int devN = sUDP.getWBbyte(1);
                            int sInd = getSnsIndex(devN);
                            if (sInd<0){
                                sInd=sensors.size();
                                sensors.add(new SensorDevice(devN, sUDP.getWBbyte(4)));
//                                    sensCountText.setText(String.format(Locale.getDefault(), "%d", sensors.size()));
                            }

                            byte[] bufState = {SET_W_COMMAND, (byte) devN, 2, (byte) CMD_ASK_SENSOR_STATE};
                            if (askUDP(bufState, MSG_RE_SENT_W, MSG_SENSOR_STATE)){
                                int count = sUDP.getWBbyte(2)-3;
                                byte[] buf = sUDP.getWBpart(5, count+5);
                                sensors.get(sInd).setData(buf);
                                Log.i(TAG, "MSG_SENSOR_STATE in Main");
                            }
                        }
                    }
                }

                Calendar cl = Calendar.getInstance();
                byte[] buffTime = new  byte[9];
                buffTime[0] = CMD_SET_TIME;
                int dw = cl.get(Calendar.DAY_OF_WEEK);
                buffTime[1] = (byte) (dw==0 ? 7 : dw-1);
                buffTime[2] = (byte) ((cl.get(Calendar.YEAR)>>8)&0xFF);
                buffTime[3] = (byte) (cl.get(Calendar.YEAR)&0xFF);
                buffTime[4] = (byte) (cl.get(Calendar.MONTH)+1);
                buffTime[5] = (byte) cl.get(Calendar.DAY_OF_MONTH);
                buffTime[6] = (byte) cl.get(Calendar.HOUR_OF_DAY);
                buffTime[7] = (byte) cl.get(Calendar.MINUTE);
                buffTime[8] = (byte) cl.get(Calendar.SECOND);
                askUDP(buffTime, MSG_RCV_OK, 0);


                for (int i = 0; i <execDevs.size() ; i++) {
                    byte[] bufState = {SET_W_COMMAND, (byte) execDevs.get(i).getDevNum(), 2, (byte) CMD_ASK_STATE};
                    askUDP(bufState, MSG_RE_SENT_W, MSG_STATE);
                }
                Log.i(TAG, "Send MSG_END_CONNECTTASK, Exec = " + execDevs.size() + ", Sns = " + sensors.size());
                Bundle bundle = new Bundle();
                Message msg = handler.obtainMessage();
                bundle.putInt("ThreadEnd", MSG_END_CONNECTTASK);
                msg.setData(bundle);
                handler.sendMessage(msg);

            }
        }).start();

    }


    private void parceFromHub(byte[] buf){
        switch (buf[0]&0xFF){
            case MSG_RE_SENT_W:
                parceFromDevice(buf);
                break;
            case Msg_LiconIP:
                liconIP = String.format(Locale.getDefault(), "%d.%d.%d.%d", buf[1]&0xFF, buf[2]&0xFF, buf[3]&0xFF, buf[4]&0xFF);
                liconName="";
                if (buf.length>5){
                    liconName = new String(Arrays.copyOfRange(buf, 5, buf.length), StandardCharsets.UTF_8);
                }
                if (!liconName.equals(""))
                    Toast.makeText(this, "LiCon found at "+liconName, Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(this, "LiCon found at "+liconIP, Toast.LENGTH_SHORT).show();
            case 0:
                break;
        }
    }

    private void parceFromDevice(byte[] buf){
        int cmd = buf[3]&0xFF;
        int devInd;
        switch (cmd){
            case MSG_DEV_TYPE:
                int outsCount = buf[6]&0xFF;
                if (outsCount>0){                 // add only OutCount>0
                    if (getDevIndex(buf[1]&0xFF)<0){
                        int ind = execDevs.size();
                        execDevs.add(new ExecDevice(buf[1]&0xFF, (byte) 0, outsCount));

                        for (int i = 0; i <outsCount ; i++) {
                            if (execDevs.get(ind).getLampText(i).equals("")){
                                String dkey = String.format(Locale.getDefault(), "D%03d%02d", buf[1]&0xFF, i+1);
                                String key = namesFile.getProperty(dkey, "");
                                if (!key.equals("")){
                                    execDevs.get(ind).setLampText(i, key);
                                }
                            }
                        }
                    }
                }
                break;
            case MSG_OUT_STATE:
                int devN = buf[4]&0xFF;
                devInd = getDevIndex(devN);
                if (devInd>=0){
                    byte prevState = execDevs.get(devInd).getOutState();
                    int diff = ((buf[5]&0xFF)^prevState)&0xFF;
                    if (diff != 0){
                        changedState = devN*0x100 + diff;
                        Log.i(TAG, " Main, changedState = "+Integer.toHexString(changedState) );
                    }
                    execDevs.get(devInd).setOutState((byte) (buf[5]&0xFF));
                }

                break;
            case MSG_STATE:
                /*
                for (int i = 0; i <execDevs.size() ; i++) {
                    if (execDevs.get(i).getDevNum()==(buf[4]&0xFF)){
                        execDevs.get(i).setOutState((byte) (buf[5]&0xFF));
                    }
                }
                */
                devN = buf[4]&0xFF;
                devInd = getDevIndex(devN);
                if (devInd>=0){
                    execDevs.get(devInd).setOutState((byte) (buf[5]&0xFF));
                }

                break;

            case MSG_DEVICE_KIND:
                devN = buf[1] & 0xFF;
                if (devN>0x3F && devN<0x60) {               // is sensor (0x40..0x5F)
                    devInd = getSnsIndex(devN);
                    if (devInd<0){
                        sensors.add(new SensorDevice(devN, buf[4] & 0xFF));
                        sensCountText.setText(String.format(Locale.getDefault(), "%d", sensors.size()));
                    }
//                    byte[] bufKind = {SET_W_COMMAND, buf[1], 2, (byte) CMD_ASK_STATE};
//                    askUDP(bufKind, MSG_RE_SENT_W, 0);

                }
                break;

            case CMD_SEND_COMMAND:
                lastCommand = (buf[4]&0xFF)*0x100 + buf[5]&0xFF;
                Log.i(TAG, " lastCommand = "+lastCommand );
                break;

            case CMD_MSG_ANALOG_DATA:
                int devNum = buf[4]&0xFF;
                int typ = buf[6]&0xFF;
                int sInd = getSnsIndex(devNum);
                if (sInd>=0){
                    switch (typ){
                        case SensorDevice.IS_TEMP:
                            sensors.get(sInd).setTemp(((buf[7]&0xFF)<<8) | (buf[8]&0xFF));
                            break;
                        case SensorDevice.IS_HUM:
                            sensors.get(sInd).setHum(((buf[7]&0xFF)<<8) | (buf[8]&0xFF));
                            break;
                        case SensorDevice.IS_PRESS:
                            sensors.get(sInd).setPress(((buf[7]&0xFF)<<16) | ((buf[8]&0xFF)<<8) | (buf[9]&0xFF));
                            break;
                    }
                }
                break;


        }
        String txt = getLastCommand() + " | " + getDevNumChange() +" | " +getMaskChange();
        statusText.setText(txt);

    }

    public void sendCommand(int cmd){
        if (connected){
            byte[] bufState = {SET_W_COMMAND, BC_Dev, 4, (byte) CMD_SEND_COMMAND, (byte) ((cmd>>8)&0xFF), (byte) (cmd&0xFF)};
            askUDP(bufState, MSG_RCV_OK, 0);
        }else{
            Toast.makeText(this, "No connection", Toast.LENGTH_SHORT).show();
        }
    }

    /*
    public void drawRcvStatus(int attempt, boolean result){
        final SpannableStringBuilder text = new SpannableStringBuilder("*****");
        final ForegroundColorSpan styleRed = new ForegroundColorSpan(Color.rgb(255, 0, 0));
        final ForegroundColorSpan styleGreen = new ForegroundColorSpan(Color.rgb(0, 255, 0));
        final ForegroundColorSpan styleGrey = new ForegroundColorSpan(Color.rgb(32, 32, 32));
        if (result){
            text.setSpan(styleRed, 0, attempt-1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            text.setSpan(styleGreen, attempt-1, attempt, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            text.setSpan(styleGrey, attempt+1, 4, Spannable.SPAN_INCLUSIVE_INCLUSIVE);

        }else{
            if (attempt==0){
                text.setSpan(styleGrey, 0, 4, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            }else{
                text.setSpan(styleRed, 0, attempt, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                text.setSpan(styleGrey, attempt+1, 4, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            }

        }
        statusText.setText(text);
    }

     */


    public boolean askUDP(byte[] inBuf, int hostCmd, int devCmd) {
        if (netInfo!=null){
            sUDP.incCurrentID();
            for (int i = 1; i <4 ; i++) {
                if (hostCmd==MSG_RCV_OK){
                    sUDP.send(inBuf, (byte) i, hostCmd, devCmd);
                }else {
                    sUDP.send(inBuf, (byte) NO_CONFIRM, hostCmd, devCmd);
                }
                int tm = 0;
                while ((sUDP.waitForConfirm())&&(tm<timeout)){
                    try {
                        TimeUnit.MILLISECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    tm++;
                }
                if (tm<timeout){
                    Log.i(TAG, "askUDP " +byteArrayToHex(inBuf, inBuf.length)+ " for ("+Integer.toHexString(sUDP.hostCmd)+", "+Integer.toHexString(sUDP.devCmd)+"), time = "+tm+"mS"+", att = "+i);
                    return true;

                }
            }
            Log.i(TAG, "No answer to "+byteArrayToHex(inBuf, inBuf.length)+", hostCmd = "+Integer.toHexString(sUDP.hostCmd)+", devCmd = "+Integer.toHexString(sUDP.devCmd));
        }
        return false;
    }

    boolean askSigUDP(byte[] inBuf) {
        if (netInfo!=null){
            sUDP.incCurrentID();
            for (int i = 1; i <4 ; i++) {
                sUDP.sendToSignal(inBuf, (byte) NO_CONFIRM);
                int tm = 0;

                while (sUDP.waitForSignalUDP() && (tm<timeout)){
                    try {
                        TimeUnit.MILLISECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    tm++;
                }
                if (tm<timeout){
                    Log.i(TAG, " waitForSigUDP, time = " + tm + "mS, attempt " + i);
                    return true;

                }
            }
            Log.i(TAG, "No answer from signal server " + signalIP);
        }
        return false;
    }



    @Override
    public void onRxUDP(byte[] inData) {
        Log.i(TAG, "onRxUDP: " + byteArrayToHex(inData, inData.length));
        int len = inData.length;
        if (len>7){

            byte[] recvBuff = Arrays.copyOfRange(inData,7,len);                      // total - 7
            sUDP.lastID= (byte) (inData[3]&0xFF);
            Log.i(TAG, " In:  "+ byteArrayToHex(inData, len));
            if (recvBuff[0]!=(byte)MSG_RCV_OK){
                parceFromHub(recvBuff);
            }

        }
    }

    private void askLiConData(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                byte[] ask = {0, 0, 0, 1, ASK_FOR_DATA};
                int port = 55300;
                boolean doRead = true;
                try {
                    Socket socket = new Socket(liconIP, port);
                    OutputStream os = socket.getOutputStream();
                    os.write(ask, 0, ask.length);
                    os.flush();

//                    InputStream is;
                    while (doRead){
                        InputStream is = socket.getInputStream();
                        int first = is.read();
                        int count;
                        switch (first){
                            case DATA_NOT_LOAD:
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(), getText(R.string.no_licon_data), Toast.LENGTH_LONG).show();
                                    }
                                });
                                Log.i(TAG, " From LoCon:  project not load");
                                doRead=false;
                                is.close();
                                break;
                                /*
                            case DATA_SUCSESS:
                                count = is.available();
                                Log.i(TAG, " From LoCon:  got data sucsess, count = " + count);
                                clonePrefs();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(), getText(R.string.licon_data_got), Toast.LENGTH_LONG).show();
                                    }
                                });
                                is.close();
                                doRead=false;
                                break;

                                 */
                            case LICON_NAMES:
                                count = is.available();
                                byte[] cb = new byte[count];
                                count = is.read(cb, 0, count);
                                File file = new File(getApplicationContext().getFilesDir().toString(), "OutNames");
                                FileOutputStream fos = new FileOutputStream(file);
                                fos.write(cb, 0, count);
                                fos.flush();
                                fos.close();
                                SharedPreferences.Editor editor = prefs.edit();
                                editor.putString("key_names", file.getName());
                                editor.apply();
                                namesFile.load(new FileInputStream(file));
                                for (int ind = 0; ind<execDevs.size(); ind++){
                                    int outsCount = execDevs.get(ind).getOutCount();
                                    if (outsCount>0){
                                        for (int i = 0; i <outsCount ; i++) {
                                            if (execDevs.get(ind).getLampText(i).equals("")){
                                                String dkey = String.format(Locale.getDefault(), "D%03d%02d", execDevs.get(ind).getDevNum(), i+1);
                                                String key = namesFile.getProperty(dkey, "");
                                                if (!key.equals("")){
                                                    execDevs.get(ind).setLampText(i, key);
                                                }
                                            }
                                        }

                                    }
                                 }
                                clonePrefs();
                                is.close();
                                doRead=false;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(), getText(R.string.licon_data_got), Toast.LENGTH_LONG).show();
                                    }
                                });
                                Log.i(TAG, " From LoCon:  got names, count = " + count);
                                break;
                                /*
                            case CMD_DATA:
                                count = is.available();
                                byte[] cmb = new byte[count];
                                count = is.read(cmb, 0, count);
                                File fcmd = new File(getApplicationContext().getFilesDir().toString(), "Commands");
                                FileOutputStream fcos = new FileOutputStream(fcmd);
                                fcos.write(cmb, 0, count);
                                fcos.flush();
                                fcos.close();
                                editor.putString("key_commands", fcmd.getName());
                                editor.apply();
                                Log.i(TAG, " From LiCon:  got commands data, count = " + count);
                                break;

                                 */
                        }
//                        is.close();

                    }
                    os.close();
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }





    //-------------------------------------------------------------------------------------------------------------------------------------------------
    /*
    private BroadcastReceiver udpReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            byte[] inData = intent.getByteArrayExtra("Buffer");
            Log.i(TAG, "udpReciever In:  "+ byteArrayToHex(inData, inData.length));

        }
    };

     */

//-------------------------------------------------------------------------------------------------------------------------------------------------

    public int getDevIndex(int devNum){
        int ind = execDevs.size()-1;
        while ((ind>=0)&&(execDevs.get(ind).getDevNum()!=devNum)){
            ind--;
        }
        return ind;
    }

    public int getSnsIndex(int sensNum){
        int ind = sensors.size()-1;
        while ((ind>=0)&&(sensors.get(ind).getNum()!=sensNum)){
            ind--;
        }
        return ind;
    }

    public static String byteArrayToHex(byte[] b, int len){
        if (b.length<len){
            len = b.length;
        }
        StringBuilder strBuff = new StringBuilder(len * 3);
        for (int i = 0; i < len; i++) {
            int v = b[i] & 0xff;
            if (v < 16) {
                strBuff.append('0');
            }
            strBuff.append(Integer.toHexString(v));
            strBuff.append(' ');
        }
        return strBuff.toString().toUpperCase();
    }

    public int getDevNumChange(){
        return (this.changedState>>8)&0xFF;
    }

    public int getMaskChange(){
        return this.changedState&0xFF;
    }

    public int getLastCommand(){
        return this.lastCommand;
    }

    private void copyFile(String sourceFile, String destFile) {
        File from = new File(sourceFile);
        if (from.exists()){
            File to = new File(destFile);
            if (!to.exists()){
                try {
                    if (!to.createNewFile()){
                        Log.i(TAG, "File "+destFile+" not created");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (to.exists()){
                InputStream in;
                try {
                    in = new FileInputStream(from);
                    OutputStream out = new FileOutputStream(to);
                    int len = in.available();
                    byte[] buf = new byte[len];
                    len = in.read(buf, 0, len);
                    out.write(buf, 0, len);
                    out.flush();
//                    while ((len = in.read(buf)) > 0) {
//                    }
                    out.close();
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }


//-----------------------------------------------------------Config functions----------------------------------------------------------------------------

    public int deletePage(int curRoom, int endRoom){
        int count = 0;

        String pagestr = String.format(Locale.getDefault(),"%02d", curRoom);
//        Log.i(TAG, " delRoom endRoom : " + endRoom);

        /*
        String CELL_N = "CellNum";
        List<String> keys = config.getKeys(CELL_N + "_L_"+pagestr);
        count=count+keys.size();
        for (int i = 0; i <keys.size() ; i++) {
//            Log.i(TAG, " delete key: " + keys.get(i));
            config.delete(keys.get(i));
        }
        keys = config.getKeys(CELL_N + "_P_"+pagestr);
        count=count+keys.size();
        for (int i = 0; i <keys.size() ; i++) {
//            Log.i(TAG, " delete key: " + keys.get(i));
            config.delete(keys.get(i));
        }
        */
        List<String> keys = config.getKeys("N" + pagestr);
        count=count+keys.size();
        for (int i = 0; i <keys.size() ; i++) {
//            Log.i(TAG, " delete key: " + keys.get(i));
            config.delete(keys.get(i));
        }
        String temp;
        for (int i = curRoom; i <endRoom ; i++) {
            temp = config.getStr(ROOM_NAME_KEY + String.format(Locale.getDefault(),"%02d", i+1));
            config.setStr(ROOM_NAME_KEY + String.format(Locale.getDefault(),"%02d", i), temp);
            String newNum = String.format(Locale.getDefault(), "N%02d", i);
            String oldNum = String.format(Locale.getDefault(), "N%02d", i+1);
            keys = config.getKeys(oldNum);
            for (int j = 0; j <keys.size() ; j++) {
                temp = config.getStr(keys.get(j));
                config.delete(keys.get(j));
                config.setStr(keys.get(j).replace(oldNum, newNum), temp);
            }
            Log.i(TAG, " remame curRoom: " + (i+1) + " to " + i);
        }
        Log.i(TAG, " delete key: " + ROOM_NAME_KEY + String.format(Locale.getDefault(),"%02d", endRoom));
        config.delete(ROOM_NAME_KEY + String.format(Locale.getDefault(),"%02d", endRoom));
        count++;
        roomAdapter.delPage(curRoom-1);
        return count;
    }

    public void saveInt(String key, int value){
        config.setInt(key, value);
    }

    public void saveStr(String key, String value){
        config.setStr(key, value);
    }

    public void delKey(String key){
        config.delete(key);
    }

    public int readInt(String key){
        return config.getInt(key);
    }

    public String readStr(String key){
        return config.getStr(key);
    }

    public List<String> readKeys(String key){
        return config.getKeys(key);
    }

    public void updateConfig(){
        if (config==null){
            config = new Config(configPath);
        }

    }


//-------------------------------------------------------------------------------------------------------------------------------------------------

}

//https://guides.codepath.com/android/sliding-tabs-with-pagerslidingtabstrip
//http://www.javarticles.com/2015/09/android-sliding-tab-layout-example.html