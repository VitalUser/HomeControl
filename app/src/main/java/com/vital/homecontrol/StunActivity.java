package com.vital.homecontrol;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class StunActivity extends AppCompatActivity {

    private static final String TAG = "MyclassSTUN";
    private static final String UDP_PACKET_RCV = "UDP_PacketReceived";
    private static final int STUN_RESPONCE = 0x0101;

//  https://apprtc.appspot.com/ - Google public signalling server
// stun.l.google.com:19302

    SharedPreferences prefs;
    Button startSTUN;
    UDPserver sUDP;
    TextView test1;
    TextView test2;
    TextView test3;
    TextView test4;
    TextView natType;
    TextView tvOutIP;
    ProgressBar prBar;

    private String changedIP = "";
    private int changedPort = 0;
    private int rcvID = 0;
    byte[] inBuf;
    String stMA = "";
    String stSA = "";
    String stCA = "";
    String stXA = "";

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
        tvOutIP = (TextView)findViewById(R.id.tv_OutIP);

        prBar = (ProgressBar)findViewById(R.id.prBar_STUN);

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


    /*
    private ProgressDialog progressDialog = null;

    private void showProgress(String text){
        if (progressDialog == null){
            try {
                progressDialog = ProgressDialog.show(this, "", text);
                progressDialog.setCancelable(false);
            } catch (Exception e){

            }
        }
    }

    public void hideProgress(){
        if (progressDialog != null){
            progressDialog.dismiss();
            progressDialog = null;
        }
    }


     */

    private Boolean gotResponce(String ip, int port, byte param){
        int att = 0;
        while ((att<8) && (!sendRequest(ip, port, param))){
            att++;
        }
        return (att<8);
    }

    private void checkSTUN(){
        String defIP = "216.93.246.18";
        int defPort = 3478;
        prBar.setVisibility(View.VISIBLE);
//        showProgress("");
        test1.setText("");
        test2.setText("");
        test3.setText("");
        test4.setText("");
        natType.setText("");
        tvOutIP.setText("");
        final String ip = defIP;
        final int port = defPort;


        if (sUDP == null) {
            sUDP = new UDPserver(this, "", 0, 55550, 0);
            Log.i(TAG, " StunUDP, new sUDP" );
            sUDP.start();
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                Bundle bundle = new Bundle();
                Message msg0 = handler.obtainMessage();
                if (gotResponce(ip, port, (byte) 0)){
//                    test1.setText("Test 1 pass");
                    Message msgMA = handler.obtainMessage();
                    bundle.putString("OutIP", stMA);
                    msgMA.setData(bundle);
                    handler.sendMessage(msgMA);
                    Message msg = handler.obtainMessage();
                    bundle.putString("Test1", "Test 1 pass");
                    msg.setData(bundle);
                    handler.sendMessage(msg);
                    String map1 = stMA;
                    Message msg1 = handler.obtainMessage();
                    if (!gotResponce(ip, port, (byte) 6)){
//                        test2.setText("Test 2 fail");
                        bundle.putString("Test2", "Test 2 fail");
                        msg1.setData(bundle);
                        handler.sendMessage(msg1);
                        gotResponce(changedIP, changedPort, (byte) 0);
                        Message msg2 = handler.obtainMessage();
                        if (stMA.equals(map1)){
//                            test3.setText("Test 3 pass");
                            bundle.putString("Test3", "Test 3 pass");
                            msg2.setData(bundle);
                            handler.sendMessage(msg2);
                            Message msg3 = handler.obtainMessage();
                            if (gotResponce(ip, port, (byte) 2)){
//                                test4.setText("Test 4 pass");
                                bundle.putString("Test4", "Test 4 pass");
                                msg3.setData(bundle);
                                handler.sendMessage(msg3);
//                                natType.setText("Address restricted NAT");
                                bundle.putString("NatType", "Address restricted NAT");
                            }else{
//                                test3.setText("Test 4 fail");
                                bundle.putString("Test4", "Test 4 fail");
                                msg3.setData(bundle);
                                handler.sendMessage(msg3);
 //                               natType.setText("Port restricted NAT");
                                bundle.putString("NatType", "Port restricted NAT");
                            }
                        }else{
//                            test3.setText("Test 3 fail");
                            bundle.putString("Test3", "Test 3 fail");
                            msg2.setData(bundle);
                            handler.sendMessage(msg2);
 //                           natType.setText("Symmetric NAT");
                            bundle.putString("NatType", "Symmetric NAT");
                        }
                    }else{
                        bundle.putString("Test2", "Test 2 pass");
                        msg1.setData(bundle);
                        handler.sendMessage(msg1);
//                        natType.setText("Full cone NAT");
                        bundle.putString("NatType", "Full cone NAT");
                    }
                }else{
//                    natType.setText("UDP blocked");
                    bundle.putString("NatType", "UDP blocked");
                }
                msg0.setData(bundle);
                handler.sendMessage(msg0);


            }
        }).start();

    }

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            if (bundle.getString("Test1")!=null){
                test1.setText(bundle.getString("Test1"));
            }
            if (bundle.getString("Test2")!=null){
                test2.setText(bundle.getString("Test2"));
            }
            if (bundle.getString("Test3")!=null){
                test3.setText(bundle.getString("Test3"));
            }
            if (bundle.getString("Test4")!=null){
                test4.setText(bundle.getString("Test4"));
            }
            if (bundle.getString("NatType")!=null){
                prBar.setVisibility(View.INVISIBLE);
//                hideProgress();
                natType.setText(bundle.getString("NatType"));
            }
            if (bundle.getString("OutIP")!=null){
                tvOutIP.setText(bundle.getString("OutIP"));
            }
        }
    };

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
        while ((!sUDP.getPacketOk())&&(att<200)){
            try {
                TimeUnit.MILLISECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            att++;
        }
        Log.i(TAG, " SendPacket, time = " + att + "mS");

        if (att<200){
            parceUDPpacket(sUDP.getRBuffer());
        }

        return ((att<200)&&(rcvID==0xFF00+param));
    }

    private BroadcastReceiver udpReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            inBuf = intent.getByteArrayExtra("UDPpacket");
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
                stMA = "";
                stSA = "";
                stCA = "";
                stXA = "";
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
