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
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.support.design.widget.TabLayout;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static java.lang.Thread.currentThread;


// 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 - token

public class MainActivity extends AppCompatActivity implements UDPserver.UDPlistener {

    private static final String TAG = "MyclassMain";
    
    private Config config;
    private Properties namesFile;
    private String cardStorageDir;
//    public String namesFileName;
    SharedPreferences prefs;

    private static final String UDP_RCV = "UDP_received";
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

    static final int CMD_KEEP_LINK              =  0xDD;
    static final int CMD_ASK_STATISTIC          =  0xEA;

    static final int NO_CONFIRM                 =  0xFF;

    public static final int REQUEST_WRITE_PERMISSION   = 23401;

    static final int MSG_END_CONNECTING         =  0x01;
    static final int MSG_END_CONNECTTASK        =  0x02;
    static final int MSG_GOT_MAP_ADDR           =  0x03;
    static final int MSG_GOT_PEER_ADDR          =  0x04;
    static final int MSG_NO_HOST                =  0x05;
    static final int MSG_NO_STUN_ANSWER         =  0x06;
    static final int MSG_NO_SIGNAL_ANSWER       =  0x07;
    static final int MSG_END_GET_COMMAND        =  0x08;
    static final int MSG_NEED_CHECK_SETTINGS    =  0x10;


    ViewPager viewPager;
    ProgressBar pBar;
    ProgressBar progressLinkDevs;
    RoomAdapter roomAdapter;
    TabLayout tabLayout;
    TextView statusText;
    TextView mappedIpText;
//    TextView localIPtext;
    TextView devCountText;
    TextView sensCountText;
    TextView linkTText;
    UDPserver sUDP;


    Timer timer;
    TimerTask task;
//    Timer startTimer;
//    TimerTask startTask;



    private NetworkInfo netInfo;
    private WifiManager wifiMgr;
    private Boolean netRecieverRegistered = false;
    private Boolean workWiFi = false;
    private Boolean remote = false;
//    private boolean confirmOk = false;
    private Boolean staticIP = false;
    private String remoteIP = "0.0.0.0";
    private int remPort = 0;
    private String mappedIP = "0.0.0.0";
    private int mappedPort = 0;
    private String signalIP = "0.0.0.0";
    private String peerIP = "0.0.0.0";
    private int peerPort = 0;
    public int defaultPort = 55555;
    public String devLocalIP;
    private String liconIP = "";
    public boolean connected;
    private int pass;
    private int serial = 0;
    private int curserial;
    public List<ExecDevice> execDevs = new ArrayList<>();
    public List<SensorDevice> sensors = new ArrayList<>();
    private final List<Integer> devs = new ArrayList<>();
    private final List<Integer> snss = new ArrayList<>();
    private final List<Integer> devsFound = new ArrayList<>();
    private final List<Integer> snStFound = new ArrayList<>();
    public int lastCommand = 0;
    public int changedState = 0;
    private String theme;
    private int timeout;
    private boolean sucsessReadMem;
    private String liconName = "";
    private boolean cardEnable;
    private boolean extDenied = false;

    private final List<String> logList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        copyFile( storageDir+"/Preferences/preferences.xml", fPref);

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

        if (!prefs.getBoolean("id_rotate", false)){
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            else
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pBar = findViewById(R.id.progressBar);
        pBar.setVisibility(View.INVISIBLE);
//        pBar.setLayoutParams(new LinearLayout.LayoutParams(0, 0));
        statusText = findViewById(R.id.status_text);
        statusText.setText("");
        mappedIpText = findViewById(R.id.mapped_IP_text);
        mappedIpText.setText("");
//        localIPtext = findViewById(R.id.local_IP_text);
//        localIPtext.setText("");
        devCountText = findViewById(R.id.devcount_text);
        devCountText.setText("0");
        sensCountText = findViewById(R.id.senscount_text);
        sensCountText.setText("0");
        linkTText = findViewById(R.id.link_text);

        progressLinkDevs = findViewById(R.id.progressDevs);
        progressLinkDevs.setProgress(0);
        progressLinkDevs.setMax(0);

        if (config==null){
            config = new Config(this.getFilesDir().toString());
        }

        readSetting();
        initiateStorage();
        namesFile = new Properties();

        viewPager = findViewById(R.id.viewPager);
        roomAdapter = new RoomAdapter(getSupportFragmentManager(), config.getValues(ROOM_NAME_KEY));
        viewPager.setAdapter(roomAdapter);



        tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager, true);

        wifiMgr = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        sUDP = (UDPserver)getLastCustomNonConfigurationInstance();

        if (sUDP==null){
            sUDP = new UDPserver(getApplicationContext(), pass, this);
            sUDP.setSignalIP(signalIP);
            sUDP.start();
            Log.i(TAG, "new sUDP: "+sUDP.toString() );
        }
        Log.i(TAG, "OnCreate" );


    }

    private void readSetting(){
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        remoteIP = prefs.getString("key_remIP", "0.0.0.0");
        signalIP = prefs.getString("key_signalIP", "178.124.206.163");
        staticIP = prefs.getBoolean("id_cb_StaticIP", false);
        remPort = Integer.parseInt(Objects.requireNonNull(prefs.getString("key_port", "0")));
        serial = Integer.parseInt(Objects.requireNonNull(prefs.getString("key_set_serial", "0")));
        pass = Integer.parseInt(Objects.requireNonNull(prefs.getString("key_udppass", "0"))) | 0x800000;
        workWiFi = prefs.getBoolean("id_cb_WorkWiFi", false);
        timeout = Integer.parseInt(Objects.requireNonNull(prefs.getString("key_timeout", "500")));
        extDenied = prefs.getBoolean("key_ExtMemDenied", false);


    }

    private void initiateStorage(){

        //https://toster.ru/q/302804

        cardEnable = false;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){        // API23, Android 6.0
                cardEnable = (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED);
                if (!cardEnable){
//                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    if (!extDenied) {
                        Log.i(TAG, "shouldShowRequestPermissionRationale");
                        AlertDialog.Builder dlg = new AlertDialog.Builder(this);
                        TextView text = new TextView(this);
                        text.setText(R.string.write_permit);
                        text.setGravity(Gravity.CENTER);
                        dlg.setView(text);
                        dlg.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_PERMISSION);

                            }
                        });
                        dlg.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                extDenied = true;
                                SharedPreferences.Editor editor = prefs.edit();
                                editor.putBoolean("key_ExtMemDenied", extDenied);
                                editor.apply();
                                dialog.cancel();
                            }
                        });
                        dlg.show();
                    }
                }
            }else{
                cardEnable =true;
            }
        }
        if (cardEnable){
            cardStorageDir = Environment.getExternalStorageDirectory().toString() + "/HomeControl";   // "/storage/emulated/0/HomeControl"
            File rootDir = new File(cardStorageDir);
            if (!rootDir.exists()) {
                if (!rootDir.mkdir()){
                    cardEnable = false;
                }
            }
        }
        if (cardEnable){
            Bundle bundle = new Bundle();
            Message msg = handler.obtainMessage();
            bundle.putInt("ThreadEnd", MSG_NEED_CHECK_SETTINGS);
            msg.setData(bundle);
            handler.sendMessage(msg);
        }
    }


    private void checkForSavedSettings(){
        String cardStorageDir = Environment.getExternalStorageDirectory().toString() + "/HomeControl";   // "/storage/emulated/0/HomeControl"
        final String configPath = cardStorageDir + "/Config";
        File cfile = new File(configPath, "Config.ini");
        boolean confExist = cfile.exists();
        final String prefPath = cardStorageDir + "/Preferences";
        File pfile = new File(prefPath, "Preferences.xml");
        boolean prefExist = pfile.exists();
        if ((confExist || prefExist)){
            AlertDialog.Builder dlg = new AlertDialog.Builder(this);
            TextView text = new TextView(this);
            text.setText(R.string.found_settings);
            text.setGravity(Gravity.CENTER);
            dlg.setView(text);
            dlg.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    copyFile(configPath + "/Config.ini", getApplicationContext().getFilesDir().toString() + "/Config.ini");
                    String fPrefFile = getApplicationContext().getPackageName()+ "_preferences.xml";   // "com.vital.homecontrol_preferences.xml"
//                    String fPref = "data/data/"+getApplicationContext().getPackageName()+"/shared_prefs/"+fPrefFile;
                    String prefDir = "data/data/"+getApplicationContext().getPackageName()+"/shared_prefs";
                    File fprefDir = new File(prefDir);
                    boolean fprefDirExist = fprefDir.exists();
                    if (!fprefDirExist){
                        fprefDirExist = fprefDir.mkdir();
                    }
                    if (fprefDirExist){
                        copyFile(prefPath + "/Preferences.xml", prefDir + "/" + fPrefFile);
                    }

                    final AlertDialog.Builder dlgRestart = new AlertDialog.Builder(MainActivity.this);
                    TextView text = new TextView(MainActivity.this);
                    text.setText(R.string.need_restart);
                    text.setGravity(Gravity.CENTER);

                    dlgRestart.setView(text);
                    dlgRestart.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            saveInt("key_needCheckSetting", 1);
                            Intent mStartActivity = new Intent(MainActivity.this, MainActivity.class);
                            int mPendingIntentId = 123456;
                            @SuppressLint("UnspecifiedImmutableFlag") PendingIntent mPendingIntent = PendingIntent.getActivity(MainActivity.this, mPendingIntentId, mStartActivity,
                                    PendingIntent.FLAG_CANCEL_CURRENT);
                            AlarmManager mgr = (AlarmManager) MainActivity.this.getSystemService(Context.ALARM_SERVICE);
                            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 200, mPendingIntent);
                            System.exit(0);
                        }
                    });
                    dlgRestart.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            saveInt("key_needCheckSetting", 1);
                            dialog.cancel();
                        }
                    });
                    dlgRestart.show();
               }
            });
            dlg.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    saveInt("key_needCheckSetting", 1);
                    dialog.cancel();
                }
            });
            dlg.show();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_WRITE_PERMISSION) {
            // https://stackoverflow.com/questions/15564614/how-to-restart-an-android-application-programmatically
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                extDenied = false;
                cardEnable =true;
                Bundle bundle = new Bundle();
                Message msg = handler.obtainMessage();
                bundle.putInt("ThreadEnd", MSG_NEED_CHECK_SETTINGS);
                msg.setData(bundle);
                handler.sendMessage(msg);
                Log.e("TAG", "Пользователь дал разрешение");
            } else {
                extDenied = true;
                Log.e("TAG", "Пользователь отклонил разрешение");
            }
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("key_ExtMemDenied", extDenied);
            editor.apply();
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
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
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
        if (timer!=null){
            timer.cancel();
        }
        timer = null;
        Log.i(TAG, " onPause in MainActivity " );
        if (sUDP!=null){
            /*
            waitCondition(new Callable<Boolean>() {
                @Override
                public Boolean call() {
                    return null;
                }
            }, new byte[]{BREAK_LINK, 0});
            */
            byte[] outBuf = {BREAK_LINK, 0};
            askUDP(outBuf, 0, 0);
        }
        if (netRecieverRegistered){
            unregisterReceiver(netReciever);
            netRecieverRegistered=false;
        }
        cloneConfig();
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.i(TAG, " onResume in MainActivity " );
        task = new TimerTask() {
            @Override
            public void run() {
                if (netInfo!=null){
                    if (connected){
                        sUDP.incCurrentID();
                        byte[] buf = {(byte) CMD_KEEP_LINK};
                        if (prefs.getBoolean("key_showKeep", false))
                           logList.add("S: " + byteArrayToHex(buf, buf.length));
                        sUDP.send(buf, (byte) NO_CONFIRM, 0, 0);
                        Log.i(TAG, "TimerTask" );
                    }

                }

            }
        };
        timer = new Timer();
        timer.schedule(task, 12000, 12000);
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
//        clonePrefs();
        Log.i(TAG, " onDestroy in MainActivity ");
        super.onDestroy();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
// https://habr.com/post/222295/ - Menu
    }

    @SuppressLint("NonConstantResourceId")
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
                if (ind == rooms-1){
                    viewPager.setCurrentItem(ind-1);
                }
                int count = deletePage(ind+1, rooms);
                Toast.makeText(this, "Deleted "+count+ " entries", Toast.LENGTH_SHORT).show();
                recreate();
                return true;

            case R.id.action_update:
                tryConnect();
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
            case R.id.action_ShowLog:
                showLog();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    private void showLog() {
        AlertDialog.Builder logDlg = new AlertDialog.Builder(this);
        final String[] log = logList.toArray(new String[0]);

        final LogAdapter adapter = new LogAdapter(this, R.layout.log_list_item, Arrays.asList(log));

        final ListView lv = new ListView(this);
        lv.setAdapter(adapter);

        logDlg.setView(lv);
        logDlg.setNeutralButton("Clear and exit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                logList.clear();
                dialogInterface.cancel();
            }
        });
        logDlg.setNegativeButton("Exit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        logDlg.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, " onActivityResult " + requestCode+" "+resultCode);
        if (requestCode==RK_SETTING){
            clonePreferences();
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

    private void cloneConfig(){
        if (cardEnable){
            final String configPath = cardStorageDir + "/Config";
            File configDir = new File(configPath);
            boolean configDirExist = configDir.exists();
            if (!configDirExist){
                configDirExist = configDir.mkdir();
            }
            if (configDirExist){
                copyFile(getApplicationContext().getFilesDir().toString() + "/Config.ini", configPath + "/Config.ini" );
            }
        }
    }
    private void clonePreferences(){
        if (cardEnable){
            final String prefPath = cardStorageDir + "/Preferences";
            File prefDir = new File(prefPath);
            boolean prefDirExist = prefDir.exists();
            if (!prefDirExist){
                prefDirExist = prefDir.mkdir();
            }
            if (prefDirExist){
                String fPrefFile = getApplicationContext().getPackageName()+ "_preferences.xml";   // "com.vital.homecontrol_preferences.xml"
                String fPref = "data/data/"+getApplicationContext().getPackageName()+"/shared_prefs/"+fPrefFile;
                copyFile(fPref, prefPath + "/Preferences.xml");
            }
        }
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

    private final BroadcastReceiver netReciever = new BroadcastReceiver() {


        @Override
        public void onReceive(Context context, Intent intent) {
            tryConnect();
       }
    };

    private void tryConnect(){
        netInfo = ((ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        Log.i(TAG, "On Broadcast receive: " + (netInfo==null? "null" : netInfo.getTypeName()));

        if (netInfo == null){
            setTitle(getString(R.string.app_name) + " : " + getString(R.string.no_net));
            if (wifiMgr.getWifiState() == WifiManager.WIFI_STATE_DISABLED) {       // WiFi is OFF
                if (workWiFi) {
                    wifiOnDialog();
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
                    statusText.setText(getString(R.string.connecting_local));
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
                            statusText.setText(getString(R.string.connecting_rem));
                            connectingToHost();
                        }
                    }else{
                        statusText.setText(getString(R.string.connecting_rem));
                        connectingThrouSignal();

                    }
                    break;
            }
        }

    }

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
//                        clonePrefs();
//                        isShowDialog = false;
                    }
                })
                .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Log.i(TAG, " wifiOnDialog - yes");
//                        statusText.setText(R.string.connecting);
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
        devLocalIP="";
        /*
        return waitCondition(new Callable<Boolean>() {
            @Override
            public Boolean call() {
                return !devLocalIP.equals("");
            }
        }, new byte[]{ASK_IP});
        */
        byte[] outBuf = {ASK_IP};
        return  askUDP(outBuf, MSG_ANSW_IP, 0);
    }

    private boolean isIpFound(){
        connected = askIP();
//        if (connected){
//            devLocalIP = String.format(Locale.getDefault(),"%d.%d.%d.%d", sUDP.getWBbyte(1),sUDP.getWBbyte(2),sUDP.getWBbyte(3),sUDP.getWBbyte(4)); // & 0xFF need for unsigned
//            Log.i(TAG, " got answer IP: "+devLocalIP );

//            if (!remote){
//                sUDP.setDestIP(devLocalIP);
//            }
//        }
        return connected;
    }

    private void connectingToHost(){
        linkTText.setText("");
        if (sUDP.getPass()==0x800000){
            statusText.setText(R.string.no_pass_set);
        }else{
//            statusText.setText(getString(R.string.connecting));
            connected = false;
//            isP2P = false;

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
    }

    private void connectingThrouSignal(){
        linkTText.setText("");
        if (sUDP.getPass()==0x800000){
            statusText.setText(R.string.no_pass_set);
        }else{
//            statusText.setText(getString(R.string.connecting));
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
                    Log.i(TAG, "connectingThrouSignal, time = "+att+"mS, peerIP = "+peerIP);
                    if (!peerIP.equals("0.0.0.0")){
                        Log.i(TAG, "connectingThrouSignal, connect to "+peerIP+" : "+peerPort);
                        sUDP.setDestIP(peerIP);
                        sUDP.setDestPort(peerPort);
                        remote = true;
//                        isP2P = true;
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
                           link = mappedIP + " : " + mappedPort + " ("+ getLocalIP() + ") <--> " + peerIP + " : " + peerPort +" ("+ devLocalIP + ")";

                       }else{
                           link = getLocalIP() + " <--> " + devLocalIP;
                       }
                       linkTText.setText(link);
                       taskAfterConnect();
                   }else{
                       if (remote){
                           if (staticIP){
                               st = getString(R.string.no_answer) + " " + remoteIP + " : "+remPort;
                           }else{
                               st = getString(R.string.no_answer) + " " + peerIP + " : "+peerPort;
                           }
                           statusText.setText(R.string.no_connect);
                           linkTText.setText("");
                           pBar.setVisibility(View.INVISIBLE);
                           Toast.makeText(getApplicationContext(), st, Toast.LENGTH_LONG).show();
                       }else{
                           if (staticIP){
                               if ((remoteIP.equals("0.0.0.0"))||(remPort==0)){
                                   Toast.makeText(getApplicationContext(), getText(R.string.no_remIP), Toast.LENGTH_LONG).show();
                                   statusText.setText(R.string.no_connect);
                                   linkTText.setText("");
                                   pBar.setVisibility(View.INVISIBLE);
                               }else{
                                   remote = true;
                                   sUDP.setDestIP(remoteIP);
                                   sUDP.setDestPort(remPort);
                                   statusText.setText(getString(R.string.connecting_rem));
                                   connectingToHost();
                               }
                           }else{
                               if ((!peerIP.equals(""))&&(peerPort!=0)){
                                   remote = true;
                                   sUDP.setDestIP(peerIP);
                                   sUDP.setDestPort(peerPort);
                                   statusText.setText(getString(R.string.connecting_rem));
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
//                   statusText.setText(R.string.connected);
                   progressLinkDevs.setProgress(0);
                   break;
               case MSG_GOT_MAP_ADDR:
                   st = mappedIP +" : " + mappedPort;
                   mappedIpText.setText(st);
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
                   linkTText.setText("");
                   pBar.setVisibility(View.INVISIBLE);
                   Toast.makeText(getApplicationContext(), getText(R.string.no_signal_IP) +" "+ signalIP, Toast.LENGTH_LONG).show();
                   break;
               case MSG_NO_HOST:
                   statusText.setText(R.string.no_connect);
                   linkTText.setText("");
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
                       linkTText.setText("");
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
               case MSG_NEED_CHECK_SETTINGS:
                   int needCheckSetting = readInt("key_needCheckSetting");
                   if (needCheckSetting==0)
                       checkForSavedSettings();
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
        while (sUDP.waitForStunUDP() && (att<300)){
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
                    execDevs.get(di).setReadMem(false);
                    byte[] askBuf = {SET_W_COMMAND, (byte) execDevs.get(di).getDevNum(), 0x05, (byte) CMD_ASK_MEM, 1, 0, (byte) 0xB0};



                    if (askUDP(askBuf, MSG_RE_SENT_W, MSG_EEP_DATA)){
//                        int count = sUDP.getWBbyte(7);
                        int count = 0;
                        int wInd = sUDP.getWaitIndex(MSG_RE_SENT_W, MSG_EEP_DATA);
                        if (wInd>=0){
                           count = sUDP.waitBuf.get(wInd).packet[14]&0xFF;
                        }
                        if (count == 0xFF) count = 0;
                        if (count > 67) count = 67;     // max command count;
                        if (count>0){
                            execDevs.get(di).addMem(new byte[]{(byte) count});
                            int bytesToRead = count*5;
                            int offs = 0xB0+1;
                            while ((bytesToRead>0)&&(sucsessReadMem)){
                                int countRead;
                                if ((bytesToRead / 128)>0){
                                    countRead = 128;
                                }else{
                                    countRead = bytesToRead % 128;
                                }
                                askBuf[4] = (byte) countRead;
                                askBuf[5] = (byte) (offs >> 8);
                                askBuf[6] = (byte) (offs & 0xFF);
                                if (askUDP(askBuf, MSG_RE_SENT_W, MSG_EEP_DATA)){
                                    wInd = sUDP.getWaitIndex(MSG_RE_SENT_W, MSG_EEP_DATA);
                                    if (wInd>=0){
                                        byte[] eep = Arrays.copyOfRange(sUDP.waitBuf.get(wInd).packet, 14, 14+countRead);
                                        execDevs.get(di).addMem(eep);
                                    }
//                                    byte[] eep = Arrays.copyOfRange(sUDP.getWB(), 7, 7+countRead);
                                }else{
                                    sucsessReadMem = false;
                                }
                                bytesToRead -= countRead;
                                offs += countRead;
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
                    if (!cmdAr.contains(cmd)){
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

    public boolean waitCondition(Callable<Boolean> callback, byte[] inBuf){
        if (netInfo!=null){
            logList.add("S: " + byteArrayToHex(inBuf, inBuf.length));
            boolean dd = false;
            for (int i = 1; i <4 ; i++) {
                sUDP.incCurrentID();
                sUDP.send(inBuf, (byte) NO_CONFIRM, 0, 0);
                Log.i(TAG, " OutMain: "+  byteArrayToHex(inBuf, inBuf.length));
                int tm = 0;
                while ((!dd)&&(tm<timeout)){
                    try {
                        dd = callback.call();
                        TimeUnit.MILLISECONDS.sleep(1);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    tm++;
                }
                if (tm<timeout){
                    Log.i(TAG, "send in waitCondition " +byteArrayToHex(inBuf, inBuf.length)+ ",  time = "+tm+"mS"+", att = "+i);
                    return true;
                }
            }
            Log.i(TAG, "No answer to "+byteArrayToHex(inBuf, inBuf.length)+", in waitCondition");
        }
        return false;
    }

    /*
    public boolean waitForConfirm(byte[] inBuf){
        if (netInfo!=null){
            sUDP.setConfirm(false);
            sUDP.incCurrentID();
            for (int i = 1; i <4 ; i++) {
                sUDP.send(inBuf, (byte) i);
                Log.i(TAG, " OutMain: "+  byteArrayToHex(inBuf, inBuf.length));
                logList.add("S: " + byteArrayToHex(inBuf, inBuf.length));
                int tm = 0;
                while ((!sUDP.getConfirm())&&(tm<timeout)){
                    try {
                        TimeUnit.MILLISECONDS.sleep(1);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    tm++;
                }
                if (tm<timeout){
                    Log.i(TAG, "send in waitForConfirm " +byteArrayToHex(inBuf, inBuf.length)+ ",  time = "+tm+"mS"+", att = "+i);
                    return true;
                }
            }
            Log.i(TAG, "No answer to "+byteArrayToHex(inBuf, inBuf.length)+", in waitForConfirm");
        }
        return false;
    }
    */


    private void taskAfterConnect(){
        progressLinkDevs.setMax(0);
        progressLinkDevs.setProgress(0);

        new Thread(new Runnable() {
            @Override
            public void run() {

                curserial=-1;
//                byte[] outBuf = {(byte) CMD_ASK_TYPE};
                boolean res = waitCondition(new Callable<Boolean>() {
                    @Override
                    public Boolean call() {
                        return curserial>-1;
                    }
                }, new byte[] {(byte) CMD_ASK_TYPE});

                if (res){
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putInt("curSerial",curserial);
                    editor.apply();
                }

                devs.clear();
                devsFound.clear();
//                outBuf[0] = ASK_COUNT_DEVS;
                res = waitCondition(new Callable<Boolean>() {
                    @Override
                    public Boolean call() {
                        return devs.size()>0;
                    }
                }, new byte[] {ASK_COUNT_DEVS});

                if (res){
                    for (int i = 0; i <devs.size(); i++) {
                        final int nd = devs.get(i)&0xFF;
                        byte[] bufCount = {SET_W_COMMAND, (byte) nd, 2, (byte) CMD_ASK_TYPE};
                        waitCondition(new Callable<Boolean>() {
                            @Override
                            public Boolean call() {
                                /*
                                boolean found = false;
                                for (int j = 0; j < execDevs.size(); j++) {

                                    if (getDevIndex(nd)>-1){
                                        found = true;
                                        break;
                                    }
                                }
                                */
                                return devsFound.contains(nd);
                            }
                        }, bufCount);

                    }

                }

                snss.clear();
                snStFound.clear();
//                outBuf[0] = ASK_COUNT_SENSORS;
                res = waitCondition(new Callable<Boolean>() {
                    @Override
                    public Boolean call() {
                        return snss.size()>0;
                    }
                }, new byte[] {ASK_COUNT_SENSORS});

                if (res){
                    for (int i = 0; i <snss.size(); i++){
                        final int nd = snss.get(i)&0xFF;
                        byte[] bufCount = {SET_W_COMMAND, (byte) nd, 2, (byte) CMD_ASK_DEVICE_KIND};
                        final boolean snsOk = waitCondition(new Callable<Boolean>() {
                            @Override
                            public Boolean call() {
                                boolean found = false;
                                for (int j = 0; j < sensors.size(); j++) {
                                    if (getSnsIndex(nd)>-1){
                                        found = true;
                                        break;
                                    }
                                }
                                return found;
                            }
                        }, bufCount);

                        if (snsOk){
                            byte[] bufState = {SET_W_COMMAND, (byte) nd, 2, (byte) CMD_ASK_SENSOR_STATE};
                            waitCondition(new Callable<Boolean>() {
                                @Override
                                public Boolean call() {
                                    return snStFound.contains(nd);
                                }
                            }, bufState);

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
//                waitForConfirm(buffTime);

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
            case MSG_RCV_OK:
                break;

            case MSG_ANSW_IP:
                devLocalIP = String.format(Locale.getDefault(),"%d.%d.%d.%d", buf[1]&0xFF,buf[2]&0xFF,buf[3]&0xFF,buf[4]&0xFF); // & 0xFF need for unsigned
                Log.i(TAG, " got answer IP: "+devLocalIP );
                if (!remote){
                    sUDP.setDestIP(devLocalIP);
                }
                statusText.setText(R.string.connected);
                break;

            case MSG_DEV_TYPE:
                if (buf.length>=7){
                    curserial=(buf[3]&0xFF)*0x1000000+(buf[4]&0xFF)*0x10000+(buf[5]&0xFF)*0x100+(buf[6]&0xFF);
                }else{
                    curserial=0;
                }
                break;

            case MSG_LIST_DEVS:
                int devsCount = buf[1]&0xFF;
                for (int i = 0; i <devsCount ; i++) {
                    devs.add(buf[2+i]&0xFF);
                }
                progressLinkDevs.setMax(progressLinkDevs.getMax() + devsCount);
                Log.i(TAG, " get List Devices: "+ devsCount);
                break;

            case MSG_LIST_SENSORS:
                devsCount = buf[1]&0xFF;
                for (int i = 0; i <devsCount ; i++) {
                    snss.add(buf[2+i]&0xFF);
                }
                progressLinkDevs.setMax(progressLinkDevs.getMax() + devsCount);
                Log.i(TAG, " get List Sensors: "+ devsCount);
                break;

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
                break;

        }
    }

    private void parceFromDevice(byte[] buf){
        int cmd = buf[3]&0xFF;
        int devInd;
        switch (cmd){
            case MSG_DEV_TYPE:
                progressLinkDevs.setProgress(progressLinkDevs.getProgress()+1);
                int outsCount = buf[6]&0xFF;
                if (!devsFound.contains(buf[1]&0xFF))
                    devsFound.add(buf[1]&0xFF);
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
                devN = buf[4]&0xFF;
                devInd = getDevIndex(devN);
                if (devInd>=0){
                    execDevs.get(devInd).setOutState((byte) (buf[5]&0xFF));
                }
                break;

            case MSG_DEVICE_KIND:
                progressLinkDevs.setProgress(progressLinkDevs.getProgress()+1);
                devN = buf[1] & 0xFF;
                if (devN>0x3F && devN<0x60) {               // is sensor (0x40..0x5F)
                    devInd = getSnsIndex(devN);
                    if (devInd<0){
                        sensors.add(new SensorDevice(devN, buf[4] & 0xFF));
                        sensCountText.setText(String.format(Locale.getDefault(), "%d", sensors.size()));
                    }
                }
                break;

            case MSG_SENSOR_STATE:
                devN = buf[1] & 0xFF;
                if (!snStFound.contains(devN))
                    snStFound.add(devN);
                int snd = getSnsIndex(devN);
                sensors.get(snd).setData(buf);
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
            case CMD_MSG_STATISTIC:
                devNum = buf[1]&0xFF;
                typ = buf[6]&0xFF;
                sInd = getSnsIndex(devNum);
                if (sInd>=0){
                    sensors.get(sInd).mStat.clear();
                    for (int i = 2; i < buf.length; i++) {
                        sensors.get(sInd).mStat.add(buf[i]);
                    }
                }
                break;


        }
//        String txt = getLastCommand() + " | " + getDevNumChange() +" | " +getMaskChange();
//        statusText.setText(txt);

    }

    public void sendCommand(int cmd){
        if (connected){
            byte[] bufState = {SET_W_COMMAND, BC_Dev, 4, (byte) CMD_SEND_COMMAND, (byte) ((cmd>>8)&0xFF), (byte) (cmd&0xFF)};
            if (!askUDP(bufState, MSG_RCV_OK, 0))
                Toast.makeText(this, "Not confirmed", Toast.LENGTH_SHORT).show();
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
            logList.add("S: " + byteArrayToHex(inBuf, inBuf.length));
            if (hostCmd==MSG_RCV_OK)
                sUDP.incCurrentID();
            for (int i = 1; i <4 ; i++) {
                if (hostCmd==MSG_RCV_OK){
                    sUDP.send(inBuf, (byte) i, hostCmd, devCmd);
                }else {
                    sUDP.incCurrentID();
                    sUDP.send(inBuf, (byte) NO_CONFIRM, hostCmd, devCmd);
                }
                Log.i(TAG, " OutMain: "+  byteArrayToHex(inBuf, inBuf.length));
                int tm = 0;
                int pInd=-1;
                boolean found = false;
                while (((pInd<0)||(!found))&&(tm<timeout)){
                    try {
                        pInd = sUDP.getWaitIndex(hostCmd, devCmd);
                        if (pInd>=0)
                            found = sUDP.waitBuf.get(pInd).received;
                        TimeUnit.MILLISECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    tm++;
                }
                if (pInd>=0)
                    sUDP.waitBuf.get(pInd).received = false;
                if (tm<timeout){
                    Log.i(TAG, "askUDP " +byteArrayToHex(inBuf, inBuf.length)+ " for ("+Integer.toHexString(hostCmd)+", "+Integer.toHexString(devCmd)+"), time = "+tm+"mS"+", att = "+i);
                    return true;
                }else{
                    Log.i(TAG, "askUDP timeout");

                }
            }
            Log.i(TAG, "No answer to "+byteArrayToHex(inBuf, inBuf.length)+", hostCmd = "+Integer.toHexString(hostCmd)+", devCmd = "+Integer.toHexString(devCmd));
        }
        return false;
    }

    boolean askSigUDP(byte[] inBuf) {
        if (netInfo!=null){
            for (int i = 1; i <4 ; i++) {
                sUDP.incCurrentID();
                sUDP.sendToSignal(inBuf);
                int tm = 0;

                while (!(sUDP.signUDPok) && (tm<timeout)){
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

        Intent intent = new Intent(UDP_RCV);
        intent.putExtra("Buffer", Arrays.copyOf(inData, len));
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);


        if (len>7){

            byte[] recvBuff = Arrays.copyOfRange(inData,7,len);                      // total - 7
            sUDP.lastID= (byte) (inData[3]&0xFF);
            Log.i(TAG, " In:  "+ byteArrayToHex(inData, len));
            if (recvBuff[0]!=(byte)MSG_RCV_OK){
                parceFromHub(recvBuff);
            }

        }
        if ((inData[7]&0xFF)== 0xDF){
            if (prefs.getBoolean("key_showKeep", false))
                logList.add("R: " + byteArrayToHex(inData, len));
        }else{
            logList.add("R: " + byteArrayToHex(inData, len));
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
//                                SharedPreferences.Editor editor = prefs.edit();
//                                editor.putString("key_names", file.getName());
//                                editor.apply();
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
//                                clonePrefs();
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
            config = new Config(this.getFilesDir().toString());
        }

    }


    private class LogAdapter extends ArrayAdapter<String> {

        private final List<String> logs;
//        private int selection;

        public LogAdapter(@NonNull Context context, int resource, @NonNull List<String> objects) {
            super(context, resource, objects);
            logs = objects;
//            selection = -1;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater inflater = getLayoutInflater();
            @SuppressLint("ViewHolder") View item = inflater.inflate(R.layout.log_list_item, parent, false);
            TextView tDir = item.findViewById(R.id.text_direct);
            TextView tID = item.findViewById(R.id.text_ID);
            TextView tAtt = item.findViewById(R.id.text_attempt);
            TextView tLog = item.findViewById(R.id.text_logStr);
            TextView tCont = item.findViewById(R.id.text_content);
            String st = String.copyValueOf(logs.get(position).toCharArray(), 0, 1);
            boolean issend = (st.equals("S"));
            String txt;

            if (issend){
                st="Out";
                tDir.setText(st);
                tID.setText("");
                tAtt.setText("");
                st = String.copyValueOf(logs.get(position).toCharArray(), 3, logs.get(position).length()-3);
                tLog.setText(st);
                tCont.setText(decodeContent(st));

            }else{
                st="In";
                tDir.setText(st);

                st = String.copyValueOf(logs.get(position).toCharArray(), 12, 2);
                txt ="ID=" + st;
                tID.setText(txt);

                st = String.copyValueOf(logs.get(position).toCharArray(), 15, 2);
                if (st.equals("FF")){
                    tAtt.setText("");

                }else{
                    txt="Att " + st;
                    tAtt.setText(txt);

                }

                if (prefs.getBoolean("key_fullLog", false))
                    st = String.copyValueOf(logs.get(position).toCharArray(), 3, logs.get(position).length()-3);
                else
                    st = String.copyValueOf(logs.get(position).toCharArray(), 24, logs.get(position).length()-24);


                tLog.setText(st);
                tCont.setText(decodeContent(st));

            }

            return item;
        }

        private String decodeContent(String logString){
            String res = "";
            String byte1 = String.copyValueOf(logString.toCharArray(), 0, 2);
//            String byte2 = String.copyValueOf(logString.toCharArray(), 3, 2);
            switch (byte1){
                case "01":
                    res = "Ask IP";
                    break;
                case "9E":
                    res = "Ask Type";
                    break;
                case "6E":
                    res = "Type is " + String.copyValueOf(logString.toCharArray(), 3, logString.length()-3);
                    break;
                case "1B":
                    res = "Dev count " + String.copyValueOf(logString.toCharArray(), 3, 2);
                    break;
                case "31":
                    res = "Set Time";
                    break;
                case "A5":
                    res = "Confirm";
                    break;
                case "04":
                    res = "Ask Dev count";
                    break;
                case "05":
                case "11":
                    res = decodeCmd(String.copyValueOf(logString.toCharArray(), 3, logString.length()-3));
                    break;
                case "DD":
                    res = "keep link";
                    break;
                case "DF":
                    res = "answer keep link";
                    break;
                case "0A":
                    res = "ask sensor count";
                    break;
                case "19":
                    res = "IP is " + String.copyValueOf(logString.toCharArray(), 3, logString.length()-6);
                    break;
                case "1A":
                    res = "sensors count " + String.copyValueOf(logString.toCharArray(), 3, 2);
                    break;
                case "00":
                    res = "**";
                    break;
            }
            return res;
        }

        private String decodeCmd(String cmdStr){
            String res;
            String dev = String.copyValueOf(cmdStr.toCharArray(), 0, 2);
            String bcdev = "";
            String cmd = String.copyValueOf(cmdStr.toCharArray(), 6, 2);
            String data = String.copyValueOf(cmdStr.toCharArray(), 9, cmdStr.length()-9);
            String bcdata = "";
            if (dev.equals("7F")){
                res = "BC: ";
                bcdev = String.copyValueOf(cmdStr.toCharArray(), 9, 2);
                bcdata = String.copyValueOf(cmdStr.toCharArray(), 12, cmdStr.length()-12);

            }
            else
                res = "Dev " + dev + ": ";

            switch (cmd){
                case "9E":
                    res += "Ask type";
                    break;
                case "6E":
                    res += "type is " + data;
                    break;
                case "C0":
                    res += "ask dev kind";
                    break;
                case "CF":
                    res += "dev kind is " + data;
                    break;
                case "97":
                    res += "ask sensor state";
                    break;
                case "67":
                    res += "sensor state is " + data;
                    break;
                case "91":
                    res += "ask state";
                    break;
                case "61":
                    res += "state of " + bcdev + " is " + bcdata;
                    break;
                case "63":
                    res += "at " + bcdev + " activate input " + bcdata;
                    break;
                case "64":
                    res += "out state of " + bcdev + " is " + bcdata;
                    break;
                case "88":
                    if (cmdStr.length()>15){
                        bcdev = String.copyValueOf(cmdStr.toCharArray(), 15, 2);
                    }else{
                        bcdev = "itself";
                    }
                    bcdata = String.copyValueOf(cmdStr.toCharArray(), 9, 5);
                    res += "command "+ bcdata + " from " + bcdev;
                    break;
                case "00":
                    res += "--";
                    break;
            }
            return res;
        }

//        public void setSelect(int position){
//            this.selection = position;
//        }

//        public int getSelect(){
//            return this.selection;
//        }


    }


//-------------------------------------------------------------------------------------------------------------------------------------------------

}

//https://guides.codepath.com/android/sliding-tabs-with-pagerslidingtabstrip
//http://www.javarticles.com/2015/09/android-sliding-tab-layout-example.html