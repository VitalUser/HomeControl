package com.vital.homecontrol;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
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
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.support.design.widget.TabLayout;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static java.lang.Thread.currentThread;
import static java.lang.Thread.sleep;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MyclassMain";
    
    private Config config;
    private Properties namesFile;
    private String configPath;
    public String namesFileName;
    SharedPreferences prefs;

    static final String ROOM_NAME_KEY = "RoomName";
    static final int RK_SETTING = 1001;
    static final int RK_STUN = 1002;
    public int defaultPort = 55555;
    private int remPort = 0;
    static final int localPort = 55550;
    static final int BC_Dev = 0x7F;
    static final String UDP_RCV = "UDP_received";
    static final String MSG_RCV = "MSG_received";
    static final String STATE_CONNECTED = "Connected";
    static final String STATE_WIFI = "WorkWiFi";
    static final String STATE_EXECDEVS = "ExecDevices";
    static final String STATE_SENSORS = "Sensors";
    static final String STATE_DESTIP = "DestIP";
    static final String STATE_REMOTE = "Remote";
    static final String STATE_LASTSMD = "LastDevNum";
    static final String STATE_LASTCHNG = "LastChange";

    static final int STUN_RESPONCE = 0x0101;

    static final int ASK_IP                     =  0x01;
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

    static final int MSG_RCV_OK                 =  0xA5;
    static final int CMD_SEND_COMMAND           =  0x88;

    static final int MSG_STATE                  =  0x61;
    static final int MSG_OUT_STATE              =  0x64;
    static final int MSG_SENSOR_STATE           =  0x67;
    static final int MSG_DEV_TYPE               =  0x6E;

    static final int CMD_ASK_STATE              =  0x91;
    static final int CMD_ASK_SENSOR_STATE       =  0x97;
    static final int CMD_ASK_TYPE               =  0x9E;

    static final int CMD_ASK_DEVICE_KIND        =  0xC0;
    static final int CMD_MSG_ANALOG_DATA        =  0xC1;
    static final int CMD_MSG_STATISTIC          =  0xCA;
    static final int MSG_DEVICE_KIND            =  0xCF;

    static final int CMD_ASK_STATISTIC          =  0xEA;

    static final int NO_CONFIRM                 =  0xFF;

    public static final int NUMBER_OF_REQUEST   = 23401;
    static final int MSG_END_CONNECTING         =  0x01;
    static final int BR_MSG_END_SETTING         =  0x02;


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


    private ConnectivityManager connMgr;
    private WifiManager wifiMgr;
    private Boolean isShowDialog = false;
    private Boolean netRecieverRegistered;
    private Boolean udpRecieverRegistered;
//    private Boolean msgRecieverRegistered;
    private Boolean workWiFi;
    private Boolean remote;
    private String remoteIP = "0.0.0.0";
//    public String deviceIP;
    public String devLocalIP;
    public boolean connected;
    public List<ExecDevice> execDevs = new ArrayList<>();
    public List<SensorDevice> sensors = new ArrayList<>();
    public int lastCommand = 0;
    public int changedState = 0;
//    private String titleStr = "";
    private String theme;
    public String storageDir;
    private int timeout;
    public boolean cnf;


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
        switch (theme){
            case "Dark":
                setTheme(R.style.AppThemeDark);
                break;
            case "Light":
                setTheme(R.style.AppTheme);
                break;
        }

        if (savedInstanceState==null){
//            Log.i(TAG, " First onCreate" );
//            connected = false;
            netRecieverRegistered = false;
            udpRecieverRegistered = false;
//            msgRecieverRegistered = false;
            workWiFi = false;
//            deviceIP = stringIP(getBroadcastWiFiIP());
            devLocalIP = "";
            remote=false;
            remoteIP = prefs.getString("key_remIP", "0.0.0.0");
            if (prefs.getString("key_port", "0")!=null){
                remPort = Integer.parseInt(prefs.getString("key_port", "0"));
            }
        }
        super.onCreate(savedInstanceState);


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

        namesFileName = prefs.getString("key_names", "");
        namesFile = new Properties();

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
            file = new File(storageDir+"/Names", "");
            if (!file.exists()){
                if (!file.mkdirs()){
                    Toast.makeText(this, "Unable create Names directory", Toast.LENGTH_SHORT).show();
                    Log.i(TAG, "directory "+ namesFileName +" not created");
                }
            }
            file = new File(storageDir+"/Preferences", "");
            if (!file.exists()){
                if (!file.mkdirs()){
//                    Toast.makeText(this, "Unable create Names directory", Toast.LENGTH_SHORT).show();
                    Log.i(TAG, "directory /Preferences not created");
                }
            }
            if (!namesFileName.equals("")){
                file = new File(namesFileName, "");
                if (file.exists()){
                    try {
                        namesFile.load(new FileInputStream(file));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }


        }


//        namesFileName = Environment.getExternalStorageDirectory().toString()+"/HomeControl/Names";


        setContentView(R.layout.activity_main);

        pBar = findViewById(R.id.progressBar);
        pBar.setVisibility(View.INVISIBLE);
        pBar.setLayoutParams(new LinearLayout.LayoutParams(0, 0));
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

        updateConfig();

        viewPager = (ViewPager) findViewById(R.id.viewPager);
        roomAdapter = new RoomAdapter(getSupportFragmentManager(), config.getValues(ROOM_NAME_KEY));
        viewPager.setAdapter(roomAdapter);

        tabLayout = (TabLayout)findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager, true);

        wifiMgr = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        connMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

        sUDP = (UDPserver)getLastCustomNonConfigurationInstance();

        Log.i(TAG, "OnCreate" );


    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case NUMBER_OF_REQUEST: {
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
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, " onPause in MainActivity " );
        if (netRecieverRegistered){
            unregisterReceiver(netReciever);
            netRecieverRegistered=false;
        }
        if (udpRecieverRegistered){
            LocalBroadcastManager.getInstance(this).unregisterReceiver(udpReciever);
            udpRecieverRegistered=false;
        }
//        if (msgRecieverRegistered){
//            LocalBroadcastManager.getInstance(this).unregisterReceiver(msgReciever);
//            msgRecieverRegistered=false;
//        }
        String fPrefFile = this.getPackageName()+ "_preferences.xml";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            fPrefFile = PreferenceManager.getDefaultSharedPreferencesName(this) + ".xml";
        }
        String fPref = "data/data/"+this.getPackageName()+"/shared_prefs/"+fPrefFile;
        copyFile(fPref, storageDir+"/Preferences/preferences.xml");
    }

    @Override
    protected void onResume() {
        super.onResume();
//        Log.i(TAG, " execDevs.size() = "+execDevs.size());
//        connected=false;
//        if (connectIsValid()){
        if (connected){
//            connected=true;
            devCountText.setText(String.format(Locale.getDefault(),"%d", execDevs.size()));
            sensCountText.setText(String.format(Locale.getDefault(),"%d", sensors.size()));
            String link;
            if (remote){
                link = "MapAddr ("+ getLocalIP() + ") <--> " + "RemIP ("+ devLocalIP + ")";

            }else{
                link = getLocalIP() + " <--> " + devLocalIP;
            }
            fText.setText(link);
        }else {
            if (!devLocalIP.equals("")){
                if (remote){
                    if ((!remoteIP.equals("0.0.0.0"))&&(remPort!=0)){
                        connectToHost(remoteIP, remPort, true);
                    }
                }else{
                    connectToHost(devLocalIP, defaultPort, false);
                }
            }
        }
        Log.i(TAG, " onResume in MainActivity " );
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

        LocalBroadcastManager.getInstance(this).registerReceiver(udpReciever, new IntentFilter(UDP_RCV));
        udpRecieverRegistered = true;

//        LocalBroadcastManager.getInstance(this).registerReceiver(msgReciever, new IntentFilter(MSG_RCV));
//        msgRecieverRegistered = true;
    }

    @Override
    protected void onDestroy() {
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
            case R.id.action_del_room:
                int rooms = roomAdapter.getCount();
                Log.i(TAG, "rooms = " + rooms);
                int ind = viewPager.getCurrentItem();


//                viewPager.removeViewAt(ind);
//                roomAdapter.delPage(ind);
//                roomAdapter.notifyDataSetChanged();

                rooms = roomAdapter.getCount();
                Log.i(TAG, "rooms = " + rooms);


                int count = deletePage(ind+1, rooms);
                Toast.makeText(this, "Deleted "+count+ " entries", Toast.LENGTH_SHORT).show();
                recreate();
                return true;

            case R.id.action_update:

                if (remote){
                    if ((!remoteIP.equals("0.0.0.0"))&&(remPort!=0)){
                        connectToHost(remoteIP, remPort, true);
                    }
                }else{
                    connectToHost(stringIP(getBroadcastWiFiIP()), defaultPort, false);
                }
                return true;

            case R.id.action_settings:
                Intent intent = new Intent();
                intent.setClass(this, SettingActivity.class);
                startActivityForResult(intent, RK_SETTING);
                return true;

            case R.id.action_nat:
                Intent intStun = new Intent();
                intStun.setClass(this, StunActivity.class);
//                startActivityForResult(intStun, RK_STUN);
                startActivity(intStun);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, " onActivityResult " + requestCode+" "+resultCode);
        if (requestCode==RK_SETTING){
            remoteIP = prefs.getString("key_remIP", "");
            int pass = Integer.parseInt(prefs.getString("key_udppass", "0"));
            if (sUDP!=null){
                sUDP.setPass(pass);
            }
            if (theme.equals(prefs.getString("key_theme", ""))){
 //               if (connMgr.getActiveNetworkInfo()!=null){
//                    connectToHost();
//                }
            }else{

                recreate();
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
        if (connMgr.getActiveNetworkInfo()==null){
            item = menu.findItem(R.id.action_update);
            item.setVisible(false);
        }else{
            item = menu.findItem(R.id.action_update);
            item.setVisible(true);
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
        alert.show();
    }
//-------------------------------------------------------------------------------------------------------------------------------------------------

    private BroadcastReceiver netReciever = new BroadcastReceiver() {

        private NetworkInfo netInfo;

        @Override
        public void onReceive(Context context, Intent intent) {
            String intnt = intent.toString();
            Log.i(TAG, "On Broadcast receive" + intnt);
            netInfo = connMgr.getActiveNetworkInfo();
            workWiFi = prefs.getBoolean("id_cb_WorkWiFi", false);



            if (netInfo != null){
//                titleStr = " connected "+ netInfo.getTypeName();
//                setTitle(getString(R.string.app_name)  + titleStr);
//                localIPtext.setText(getLocalIP());
            }else{
                Log.i(TAG, " NetInfoReceiver - netInfo = null" );
                String titleStr = " no network";
                setTitle(getString(R.string.app_name)  + titleStr);
            }

            if (wifiMgr.getWifiState() == WifiManager.WIFI_STATE_DISABLED){       // WiFi is OFF
                if (workWiFi){
                    if (!isShowDialog){
                        wifiOnDialog();
                    }
                }else{
                    if (netInfo != null){
                        remoteIP = prefs.getString("key_remIP", "");
                        if (remoteIP.equals("0.0.0.0")){
                            Toast.makeText(getApplicationContext(), "WiFi is OFF\nRemote IP not set", Toast.LENGTH_LONG).show();
                        }else{
//                            if (!connected){
                                connectToHost(remoteIP, remPort, true);
//                            }
                        }
                    }else{
                        Toast.makeText(getApplicationContext(), "WiFi is OFF, no network", Toast.LENGTH_LONG).show();
                        destIPtext.setText(getString(R.string.no_net));
                    }
                }
            }

            if (wifiMgr.getWifiState() == WifiManager.WIFI_STATE_ENABLED){       // WiFi is ON
                if (netInfo != null){               // Net present
//                    Toast.makeText(getApplicationContext(), "WiFi is ON, "+netInfo.getTypeName(), Toast.LENGTH_LONG).show();
                    if (netInfo.getType()==ConnectivityManager.TYPE_WIFI){          // WiFi is ON, connected to acsess point
                        if (!connected){
                            connectToHost(stringIP(getBroadcastWiFiIP()), defaultPort, false);
                        }

                    }else{
                        if (netInfo.getType()==ConnectivityManager.TYPE_MOBILE){    // WiFi is ON, but no acsess point found, MobileNet is ON
                            Log.i(TAG, " broadcast:  WiFi is ON, net = mobile try tryMobileConnect");
                            String tn = getString(R.string.connected)+" "+netInfo.getTypeName();
                            statusText.setText(tn);
//                            titleStr = " WiFi is ON, "+netInfo.getTypeName();
//                            setTitle(getString(R.string.app_name)  + titleStr);
//                            tryMobileConnect();
                        }
                    }

                }else{                                  // WiFi is ON, but no acsess point found, MobileNet is OFF
//                    titleStr = "WiFi ON, No Net";
//                    setTitle(getString(R.string.app_name)  + titleStr);
                    Log.i(TAG, " broadcast:  WiFi is ON, No network");
                    statusText.setText(getString(R.string.no_net));
                }
            }
//            Log.i(TAG, " end_netReciever  -------------------------------------------------" );
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
                        isShowDialog = false;
                    }
                })
                .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Log.i(TAG, " wifiOnDialog - yes");
//                        titleStr = " connecting..." ;
//                        setTitle(getString(R.string.app_name)  + titleStr);
                        statusText.setText(R.string.connecting);
//                        progress.setVisibility(View.VISIBLE);
                        wifiMgr.setWifiEnabled(true);
                        isShowDialog = false;
                    }
                });
        AlertDialog alert = builder.create();
        isShowDialog = true;
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

    private void connectToHost(String ip, int port, final Boolean remote ){

        statusText.setText(getString(R.string.connecting));
        connected = false;

//        devPort = Integer.parseInt(prefs.getString("key_port", "55555"));
//        deviceIP = stringIP(getBroadcastWiFiIP());
        if (sUDP!=null){
            sUDP.setDestIP(ip);
            sUDP.setDestPort(port);
        }else{
            int pass = Integer.parseInt(prefs.getString("key_udppass", "0"));
            sUDP = new UDPserver(this, ip, port, localPort, pass);
            Log.i(TAG, " askUDP, new sUDP" );
            sUDP.start();

        }

        pBar.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        pBar.setVisibility(View.VISIBLE);

        new  Thread(new Runnable() {
            @Override
            public void run() {
//                sendUI_Msg("", "", "", "connecting...");

//                String devLocalIP="";
                Log.i(TAG, " Create connecting thread: "+currentThread() );
                byte[] outBuf = {ASK_IP};
                if (askUDP(outBuf, MSG_ANSW_IP, 0)){
                    devLocalIP = String.format(Locale.getDefault(),"%d.%d.%d.%d", sUDP.getRecvBuff(1),sUDP.getRecvBuff(2),sUDP.getRecvBuff(3),sUDP.getRecvBuff(4)); // & 0xFF need for unsigned
                    Log.i(TAG, " got answer IP: "+devLocalIP );
                    connected = true;
//                    sendUI_Msg("", "", "", deviceIP);
//                    bundle.putString("Taget", deviceIP);
//                    msg.setData(bundle);
//                    handler.sendMessage(msg);

                    if (!remote){
                        sUDP.setDestIP(devLocalIP);
                    }
                }else{
                    if (askUDP(outBuf, MSG_ANSW_IP, 0)){
                        devLocalIP = String.format(Locale.getDefault(),"%d.%d.%d.%d", sUDP.getRecvBuff(1),sUDP.getRecvBuff(2),sUDP.getRecvBuff(3),sUDP.getRecvBuff(4)); // & 0xFF need for unsigned
                        Log.i(TAG, " got answer IP: "+devLocalIP );
                        connected = true;
//                    sendUI_Msg("", "", "", deviceIP);
//                        bundle.putString("Taget", deviceIP);
//                        msg.setData(bundle);
//                        handler.sendMessage(msg);

                        if (!remote){
                            sUDP.setDestIP(devLocalIP);
                        }
                    }else {
//                        sendUI_Msg("", "", "", getString(R.string.noIP));
//                        bundle.putString("Taget", getString(R.string.noIP));
//                        msg.setData(bundle);
//                        handler.sendMessage(msg);
                    }
                }
                if (connected){
                    outBuf[0] = ASK_COUNT_DEVS;
                    if (askUDP(outBuf, MSG_LIST_DEVS, 0)){
                        int devsCount = sUDP.getRecvBuff(1);
                        int[] devs = new int[devsCount];
                        for (int i = 0; i <devsCount ; i++) {
                            devs[i]=sUDP.getRecvBuff(2+i);
                        }
                        Log.i(TAG, " get List Devices: "+ devsCount);
                        for (int i = 0; i <devsCount; i++) {
                            byte[] bufCount = {SET_W_COMMAND, (byte) devs[i], 2, (byte) CMD_ASK_TYPE};
                            askUDP(bufCount, MSG_RE_SENT_W, MSG_DEV_TYPE);
                        }
                    }else{
//                        sendUI_Msg("", "No answer List Device", "", "");
//                        bundle.putString("Status", "No answer List Device");
//                        msg.setData(bundle);
//                        handler.sendMessage(msg);
                    }

                    outBuf[0] = ASK_COUNT_SENSORS;
                    if (askUDP(outBuf, MSG_LIST_SENSORS, 0)){
                        int sensCount = sUDP.getRecvBuff(1);
                        int[] sens = new int[sensCount];
                        for (int i = 0; i <sensCount ; i++) {
                            sens[i]=sUDP.getRecvBuff(2+i);
                        }
                        Log.i(TAG, " get List Sensors: "+ sensCount);
                        for (int i = 0; i <sensCount; i++) {
                            byte[] bufCount = {SET_W_COMMAND, (byte) sens[i], 2, (byte) CMD_ASK_DEVICE_KIND};
                            if (askUDP(bufCount, MSG_RE_SENT_W, MSG_DEVICE_KIND)){
                                int devN = sUDP.getRecvBuff(1);
                                int sInd = getSnsIndex(devN);
                                if (sInd<0){
                                    sInd=sensors.size();
                                    sensors.add(new SensorDevice(devN, sUDP.getRecvBuff(4)));
//                                    sensCountText.setText(String.format(Locale.getDefault(), "%d", sensors.size()));
                                }

                                byte[] bufState = {SET_W_COMMAND, (byte) devN, 2, (byte) CMD_ASK_SENSOR_STATE};
                                if (askUDP(bufState, MSG_RE_SENT_W, MSG_SENSOR_STATE)){
                                    int count = sUDP.getRecvBuff(2)-3;
                                    byte[] buf = Arrays.copyOfRange(sUDP.getRBuffer(), 5, count+5) ;
                                    sensors.get(sInd).setData(buf);
                                    Log.i(TAG, "MSG_SENSOR_STATE in Main");



                                }


                            }

                        }
                    }



//                    sendUI_Msg("", "", getString(R.string.connected), "");
//                    bundle.putString("Toast", getString(R.string.connected));
//                    Message msg1 = handler.obtainMessage();
//                    msg1.setData(bundle);
//                    handler.sendMessage(msg1);
                }
                Log.i(TAG, "Connected is "+ connected+ ", Send MSG_END_CONNECTING");
//                sendUI_Msg(BR_MSG_END_CONNECTING);
                Bundle bundle = new Bundle();
                Message msg = handler.obtainMessage();
                bundle.putInt("ThreadEnd", MSG_END_CONNECTING);
                bundle.putBoolean("Remote", remote);
 //               bundle.putString("DevLocIP", devLocalIP);
                msg.setData(bundle);
                handler.sendMessage(msg);

            }
        }).start();


    }

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
//            String status = bundle.getString("Status");
//            String tagetIP = bundle.getString("Taget");
//            String toastText = bundle.getString("Toast");
           int state = bundle.getInt("ThreadEnd", 0);
           if (state==MSG_END_CONNECTING){
               Log.i(TAG, " get MSG_END_CONNECTING");
               pBar.setVisibility(View.INVISIBLE);
               pBar.setLayoutParams(new LinearLayout.LayoutParams(0, 0));
               if (connected){
//                   Boolean rem = ;

//                 String locIP = bundle.getString("Local");
                   remote=bundle.getBoolean("Remote");
                   String link;
                   if (remote){
                       link = "MapAddr ("+ getLocalIP() + ") <--> " + "RemIP ("+ devLocalIP + ")";

                   }else{
                       link = getLocalIP() + " <--> " + devLocalIP;
                   }
                   fText.setText(link);
                   devCountText.setText(String.format(Locale.getDefault(),"%d", execDevs.size()));
                   sensCountText.setText(String.format(Locale.getDefault(), "%d", sensors.size()));
                   statusText.setText(R.string.connected);
                   taskAfterConnect();
               }else{
                   statusText.setText(R.string.no_connect);
                   fText.setText("");
               }

           }
        }
    };

    private void taskAfterConnect(){

        new Thread(new Runnable() {
            @Override
            public void run() {
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

            }
        }).start();

    }


    private void parceFromHub(byte[] buf){
        switch (buf[0]&0xFF){
            case MSG_RE_SENT_W:
                parceFromDevice(buf);
                break;
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
        byte[] bufState = {SET_W_COMMAND, BC_Dev, 4, (byte) CMD_SEND_COMMAND, (byte) ((cmd>>8)&0xFF), (byte) (cmd&0xFF)};
//        sUDP.send(bufState, bufState.length);
        askUDP(bufState, MSG_RCV_OK, 0);
    }

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


    public boolean askUDP(byte[] inBuf, int hostCmd, int devCmd) {
        /*
        if (sUDP == null) {
            int pass = Integer.parseInt(prefs.getString("key_udppass", "0"));
            sUDP = new UDPserver(this, deviceIP, devPort, localPort, pass);
            Log.i(TAG, " askUDP, new sUDP" );
            sUDP.start();
        }
        */
        int curID = sUDP.getCurrentID();
        curID++;
        sUDP.setCurrentID((byte) curID);
        int att;
//        drawRcvStatus(0, false);
        for (int i = 1; i <4 ; i++) {
            if (hostCmd==MSG_RCV_OK){
                att=i;
            }else {
                att=NO_CONFIRM;
            }
            sUDP.send(inBuf, inBuf.length, (byte) att, hostCmd, devCmd);
            if (waitForConfirm(Integer.parseInt(prefs.getString("key_timeout", "500")))){
                Log.i(TAG, " askUDP : confirm "+Integer.toHexString(hostCmd)+ " :" + i);
//                drawRcvStatus(i, true);
                return true;
            }
        }
//        sendToast(getString(R.string.no_answer));
//        sendStatusText("No answer to " + byteArrayToHex(inBuf, inBuf.length));
        Log.i(TAG, "No answer to "+byteArrayToHex(inBuf, inBuf.length)+", hostCmd = "+Integer.toHexString(sUDP.hostCmd)+", devCmd = "+Integer.toHexString(sUDP.devCmd));
        sendUI_Msg("No answer to " + byteArrayToHex(inBuf, inBuf.length), getString(R.string.no_answer), "");
//        drawRcvStatus(3, false);
        return false;
    }

    public boolean waitForConfirm(int timeout){
        int att = 0;
        while ((!sUDP.getConfirm())&(att<timeout)){
            try {
                TimeUnit.MILLISECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            att++;
        }
        Log.i(TAG, " waitForConfirm, time = " + att + "mS");
        return (att<timeout);
    }

    public boolean connectIsValid(){
        if (!devLocalIP.equals("")){
            byte[] outBuf = {ASK_IP};
            /*
            if (sUDP == null) {
                int pass = Integer.parseInt(prefs.getString("key_udppass", "0"));
                sUDP = new UDPserver(this, deviceIP, devPort, localPort, pass);
                Log.i(TAG, " connectIsValid, new sUDP" );
                sUDP.start();
            }
            */
            for (int i = 1; i <4 ; i++) {
                sUDP.send(outBuf, outBuf.length, (byte) NO_CONFIRM, MSG_ANSW_IP, 0);
                if (waitForConfirm(50)){
                    Log.i(TAG, " connectIsValid : confirm "+Integer.toHexString(MSG_ANSW_IP)+ " :" + i);
                    return true;
                }
            }
        }
        return false;
    }





    //-------------------------------------------------------------------------------------------------------------------------------------------------
    private BroadcastReceiver udpReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
//            int attempt = intent.getIntExtra("Attempt", 0);
            byte[] inBuf = intent.getByteArrayExtra("Buffer");
            if ((inBuf!=null) && (inBuf.length>0)){
                Log.i(TAG, " onReceive in Main: inbuf: = "+byteArrayToHex(inBuf, inBuf.length));
                parceFromHub(inBuf);
            }

        }
    };

//-------------------------------------------------------------------------------------------------------------------------------------------------

    /*
    private BroadcastReceiver msgReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String statText = intent.getStringExtra("isStatusText");
            if (!statText.equals("")){
                Log.i(TAG, " msgReciever: isStatusText: = "+statText);
                statusText.setText(statText);
            }
            String toastMsg = intent.getStringExtra("isToast");
            if (!toastMsg.equals("")){
                Log.i(TAG, " msgReciever: isToast: = "+toastMsg);
                Toast.makeText(getApplicationContext(), toastMsg, Toast.LENGTH_SHORT).show();
            }

            String target = intent.getStringExtra("isTarget");
            if (!target.equals("")){
                Log.i(TAG, " msgReciever: isTarget: = "+target);
                destIPtext.setText(target);
            }
            int msgID = intent.getIntExtra("isNumMsg", 0);
            Log.i(TAG, " msgReciever: msgID: = "+msgID);
            switch (msgID){
                case BR_MSG_END_CONNECTING:
                    Log.i(TAG, " get BR_MSG_END_CONNECTING");
                    pBar.setVisibility(View.INVISIBLE);
                    pBar.setLayoutParams(new LinearLayout.LayoutParams(0, 0));
                    if (connected){
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


                        devCountText.setText(String.format(Locale.getDefault(),"%d", execDevs.size()));
                        sensCountText.setText(String.format(Locale.getDefault(), "%d", sensors.size()));
                        statusText.setText(R.string.connected);
                        for (int i = 0; i <execDevs.size() ; i++) {
                            byte[] bufState = {SET_W_COMMAND, (byte) execDevs.get(i).getDevNum(), 2, (byte) CMD_ASK_STATE};
                            askUDP(bufState, MSG_RE_SENT_W, MSG_STATE);
                        }
//                        for (int i = 0; i <sensors.size() ; i++) {
//                            byte[] bufState = {SET_W_COMMAND, (byte) sensors.get(i).getNum(), 2, (byte) CMD_ASK_STATE};
//                            askUDP(bufState, MSG_RE_SENT_W, 0);
//                        }

                    }else{
                        statusText.setText(R.string.no_connect);
                    }
                    break;

//                case BR_MSG_END_SETTING:
//                    if (connMgr.getActiveNetworkInfo()!=null){
//                        connectToHost();
//                    }
//                   break;
            }

            byte[] buf = intent.getByteArrayExtra("buffer");
            if (buf!=null){
                if (buf.length>0){
                    int hCmd = buf[0];
                    int dCmd = buf[1];
                    byte[] sendBuf = Arrays.copyOfRange(buf, 2, buf.length-2);
                    askUDP(sendBuf, hCmd, dCmd);
                }
            }

        }
    };

    */

    private void sendUI_Msg(String statText, String toastMsg, String target){
        Intent intent = new Intent(MSG_RCV);
        intent.putExtra("isStatusText", statText);
        intent.putExtra("isToast", toastMsg);
        intent.putExtra("isTarget", target);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }



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
                InputStream in = null;
                try {
                    in = new FileInputStream(from);
                    OutputStream out = new FileOutputStream(to);
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    out.close();
                    in.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }
//-----------------------------------------------------------Config functions----------------------------------------------------------------------------

    public int deletePage(int page, int end){
        int count = 0;

        String pagestr = String.format(Locale.getDefault(),"%02d", page);
//        Log.i(TAG, " delRoom end : " + end);

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
        for (int i = page; i <end ; i++) {
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
            Log.i(TAG, " remame page: " + (i+1) + " to " + i);
        }
        Log.i(TAG, " delete key: " + ROOM_NAME_KEY + String.format(Locale.getDefault(),"%02d", end));
        config.delete(ROOM_NAME_KEY + String.format(Locale.getDefault(),"%02d", end));
        count++;
//        viewPager.removeView();
        roomAdapter.delPage(page-1);
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

    public List<String> readValues(String key){
        return config.getValues(key);
    }

    public List<String> readKeysforValue(String value){
        return config.getKeysforValue(value);
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