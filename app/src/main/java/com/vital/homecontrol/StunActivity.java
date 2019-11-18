package com.vital.homecontrol;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class StunActivity extends AppCompatActivity {

    private static final String TAG = "MyclassSTUN";
    private static final String UDP_PACKET_RCV = "UDP_PacketReceived";
    private static final int STUN_RESPONCE = 0x0101;


    SharedPreferences prefs;
    Button startSTUN;
    UDPserver sUDP;
    TextView test1;
    TextView test2;
    TextView test3;
    TextView test4;
    TextView natType;

    private String changedIP = "";
    private int changedPort = 0;
    private int rcvID = 0;

    Boolean udpRecieverRegistered = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        switch (prefs.getString("key_theme", "")){
            case "Dark":
                setTheme(R.style.AppThemeDark);
                break;
            case "Light":
                setTheme(R.style.AppTheme);
                break;
        }

        super.onCreate(savedInstanceState);

        setContentView(R.layout.stun_layout);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        test1 = (TextView)findViewById(R.id.tv_STUNtest1);
        test2 = (TextView)findViewById(R.id.tv_STUNtest2);
        test3 = (TextView)findViewById(R.id.tv_STUNtest3);
        test4 = (TextView)findViewById(R.id.tv_STUNtest4);
        natType = (TextView)findViewById(R.id.tv_STUN_Type);

        startSTUN = (Button)findViewById(R.id.btn_startStun);
        startSTUN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkSTUN();
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver(udpReciever, new IntentFilter(UDP_PACKET_RCV));
        udpRecieverRegistered = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (udpRecieverRegistered){
            LocalBroadcastManager.getInstance(this).unregisterReceiver(udpReciever);
            udpRecieverRegistered=false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
            }
    }

    private void checkSTUN(){
        String defIP = "216.93.246.18";
        int defPort = 3478;

        if (sUDP == null) {
            sUDP = new UDPserver(this, "", 0, 55550, 0);
            Log.i(TAG, " StunUDP, new sUDP" );
            sUDP.start();
        }

        int att = 0;
        while ((att<8) && (!sendRequest(defIP, defPort, (byte) 0))){
            att++;
        }
        if (att<8){
            test1.setText(String.valueOf(changedPort) );
            test2.setText(changedIP);
        }
        test3.setText(String.valueOf(att));



    }

    private boolean sendRequest(String ip, int port, byte param){
        rcvID=0;
        byte[] buf;
        if (param==0){
            buf = new byte[20];
        }else{
            buf = new byte[28];
            buf[3]=8;
            buf[21]=4;
            buf[27]=param;
        }
        buf[1]=1;
        buf[18]= (byte) 0xFF;
        buf[19]= param ;
        sUDP.sendUdpPacket(buf, 20, ip, port);

        int att = 0;
        while ((!sUDP.getPacketOk())&(att<100)){
            try {
                TimeUnit.MILLISECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            att++;
        }
        Log.i(TAG, " waitForConfirm, time = " + att + "mS");
        return (att<100);
    }

    private BroadcastReceiver udpReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            byte[] inBuf = intent.getByteArrayExtra("UDPpacket");
            if ((inBuf != null) && (inBuf.length > 0)) {
                Log.i(TAG, " onReceive in STUN" );
                parceUDPpacket(inBuf);
            }
        }
    };

    private void parceUDPpacket(byte[] bufByte){
        short[] buf = new short[bufByte.length];
        for (int i = 0; i <bufByte.length ; i++) {
            buf[i]= (short) (bufByte[i]&0xFF);
        }
//        System.arraycopy(bufByte,0, buf, 0, bufByte.length);
        int twobytes = buf[0]*0x100+buf[1];
        if (twobytes==STUN_RESPONCE){
            int len = buf[2]*0x100+buf[3];
            if (len>0){
                int ofs = 20;
                String stMA = "";
                String stSA = "";
                String stCA = "";
                String stXA = "";
                while (ofs<len){
                    switch (buf[ofs]*0x100+buf[ofs+1]){
                        case 0x0001:
                            stMA= buf[ofs+8]+"."+buf[ofs+9]+"."+buf[ofs+10]+"."+buf[ofs+11]+":"+((buf[ofs+6]<<8)+buf[ofs+7]);
                            break;
                        case 0x0004:
                            stSA= buf[ofs+8]+"."+buf[ofs+9]+"."+buf[ofs+10]+"."+buf[ofs+11]+":"+((buf[ofs+6]<<8)+buf[ofs+7]);
                            break;
                        case 0x0005:
                            changedIP=buf[ofs+8]+"."+buf[ofs+9]+"."+buf[ofs+10]+"."+buf[ofs+11];
                            changedPort=(buf[ofs+6]<<8)+buf[ofs+7];
                            stCA= changedIP+":"+changedPort;
                            break;
                        case 0x8020:
                            stXA= buf[ofs+8]+"."+buf[ofs+9]+"."+buf[ofs+10]+"."+buf[ofs+11]+":"+((buf[ofs+6]<<8)+buf[ofs+7]);
                            break;
                    }
                    ofs=ofs+buf[ofs+2]*0x100+buf[ofs+3]+4;

                }
                rcvID=buf[18]*0x100+buf[19];
            }

        }
    }





}
