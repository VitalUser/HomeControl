package com.vital.homecontrol;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Arrays;

class UDPserver {

    private static final String TAG = "MyclassUDPserver";
    private static final String UDP_RCV = "UDP_received";
//    private static final String UDP_PACKET_RCV = "UDP_PacketReceived";

//    private static final int DEF_PASS        =  0xA8A929;
    private static final int SIGN_PASS       =  0xAFA55A;
    private static final int SIGN_PORT       =  16133;
    private static final int MSG_RCV_OK      =  0xA5;
    private static final int NO_CONFIRM      =  0xFF;

    private static final int localPort = 55550;
    private static final String defStunIP = "216.93.246.18";
    private static final int defStunPort = 3478;

    UDPlistener uL;
    private Context context;
    private String destIP;
    private String signalIP;
    private int destPort;
    private int pass;
    byte lastID;
    private byte currentID;
    int hostCmd;
    int devCmd;
    private boolean confirmOk;
//    private boolean packetUDPok;
    private boolean signUDPok;
    private boolean stunUDPok;
//    private boolean listen;

    private byte[] workBuff;
    private byte[] signBuff;
    private byte[] stunBuff;

    UDPserver(Context context, int pass, UDPlistener uL){
        this.context = context;
        this.destIP = "0.0.0.0";
        this.signalIP = "0.0.0.0";
        this.destPort = 0;
        this.workBuff = new byte[255];
        this.signBuff = new byte[255];
        this.stunBuff = new byte[511];
        this.pass = pass;
        this.lastID = 0;
        this.currentID = 1;
        this.confirmOk = false;
//        this.packetUDPok = false;
        this.signUDPok = false;
        this.stunUDPok = false;
        this.hostCmd = 0;
        this.devCmd = 0;
        this.uL = uL;
        Log.i(TAG, " New UDP-server listen ");

    }

    public interface UDPlistener{
        void onRxUDP(byte[] buf);
    }

    /*
    void start(){
        ListenServer ls = new ListenServer();
        ls.execute();

    }

    private class ListenServer extends AsyncTask<Void, byte[],Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                DatagramSocket sUDP = new DatagramSocket(null);
                sUDP.setReuseAddress(true);
                sUDP.bind(new InetSocketAddress(localPort));
                byte[] inData = new byte[512];
                while (true){
                    DatagramPacket inUDP = new DatagramPacket(inData, inData.length);    // first 3 bytes - pass
                    sUDP.receive(inUDP);                                                // next byte - packet ID, next - attempt, next 2 - reserved
                    int len = inUDP.getLength();                                        // total - 7
                    if (len>7){
                        int ps = (((inData[0]&0xFF) << 16) +((inData[1]&0xFF)<<8)+(inData[2]&0xFF));
                        if (ps == pass){
                            if ((inData[4]&0xFF)!=NO_CONFIRM){
                                currentID++;
                                byte[] buf = {(byte) MSG_RCV_OK};
                                Log.i(TAG, " In: sentOk to "+(inData[4]&0xFF));
                                send(buf, (byte) NO_CONFIRM, 0, 0);
                            }
                            if ((inData[3]&0xFF)!=lastID){
                                workBuff = Arrays.copyOfRange(inData,7,len);
                                lastID= (byte) (inData[3]&0xFF);
                                Log.i(TAG, " In:  "+ byteArrayToHex(inData, len));

                                if ((inData[7]&0xFF)==hostCmd){
                                    if (devCmd==0){
                                        confirmOk=true;
                                    }else{
                                        if ((inData[10]&0xFF)==devCmd){
                                            confirmOk=true;
                                        }
                                    }
                                }
                                Log.i(TAG, " In: confirmOk = "+ confirmOk);
                                publishProgress(Arrays.copyOf(inData, len));


                                Intent intent = new Intent(UDP_RCV);
                                intent.putExtra("Buffer", Arrays.copyOf(inData, len));
                                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

                            }
                            else{
                                Log.i(TAG, " Receive repeat work:  "+ inData[4]);
                            }
                        }
                        if (ps == SIGN_PASS){
                            if ((inData[4]&0xFF)!=NO_CONFIRM){
                                currentID++;
                                byte[] buf = {(byte) MSG_RCV_OK};
                                Log.i(TAG, " In: sentOk to "+(inData[4]&0xFF));
                                sendToSignal(buf, (byte) NO_CONFIRM);
                            }
                            if ((inData[3]&0xFF)!=lastID){
                                signBuff = Arrays.copyOfRange(inData,7,len);
                                lastID= (byte) (inData[3]&0xFF);
                                Log.i(TAG, " In:  "+ byteArrayToHex(inData, len));

                                signUDPok = true;


                            }
                            else{
                                Log.i(TAG, " Receive repeat signal:  "+ inData[4]);
                            }
                        }
                        if ((ps & 0xFFFF00) == 0x010100){                   // STUN responce
                            stunBuff = Arrays.copyOf(inData, len);
                            stunUDPok = true;
                            Log.i(TAG, "Alt In:  "+ byteArrayToHex(inData, len));

                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(byte[]... values) {
            super.onProgressUpdate(values);
            uL.onRxUDP(values[0]);
        }
    }

     */


    void start(){
        new Thread(new Runnable(){

            @Override
            public void run() {
                try {
                    DatagramSocket sUDP = new DatagramSocket(null);
                    sUDP.setReuseAddress(true);
                    sUDP.bind(new InetSocketAddress(localPort));
                    byte[] inData = new byte[512];
                    while (true){
                        DatagramPacket inUDP = new DatagramPacket(inData, inData.length);    // first 3 bytes - pass
                        sUDP.receive(inUDP);                                                // next byte - packet ID, next - attempt, next 2 - reserved
                        int len = inUDP.getLength();                                        // total - 7
                        if (len>7){
                            int ps = (((inData[0]&0xFF) << 16) +((inData[1]&0xFF)<<8)+(inData[2]&0xFF));
                            if (ps == pass){
                                if ((inData[4]&0xFF)!=NO_CONFIRM){
                                    currentID++;
                                    byte[] buf = {(byte) MSG_RCV_OK};
                                    Log.i(TAG, " In: sentOk to "+(inData[4]&0xFF));
                                    send(buf, (byte) NO_CONFIRM, 0, 0);
                                }
                                if ((inData[3]&0xFF)!=lastID){
                                    workBuff = Arrays.copyOfRange(inData,7,len);
                                    lastID= (byte) (inData[3]&0xFF);
                                    Log.i(TAG, " In:  "+ byteArrayToHex(inData, len));

                                    if ((inData[7]&0xFF)==hostCmd){
                                        if (devCmd==0){
                                            confirmOk=true;
                                        }else{
                                            if ((inData[10]&0xFF)==devCmd){
                                                confirmOk=true;
                                            }
                                        }
                                    }
                                    Log.i(TAG, " In: confirmOk = "+ confirmOk);

                                    Bundle bundle = new Bundle();
                                    Message msg = handler.obtainMessage();
                                    bundle.putByteArray("NewPacket", Arrays.copyOf(inData, len));
                                    msg.setData(bundle);
                                    handler.sendMessage(msg);


                                    Intent intent = new Intent(UDP_RCV);
                                    intent.putExtra("Buffer", Arrays.copyOf(inData, len));
                                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent);


                                }
                                else{
                                    Log.i(TAG, " Receive repeat work:  "+ inData[4]);
                                }
                            }
                            if (ps == SIGN_PASS){
                                if ((inData[4]&0xFF)!=NO_CONFIRM){
                                    currentID++;
                                    byte[] buf = {(byte) MSG_RCV_OK};
                                    Log.i(TAG, " In: sentOk to "+(inData[4]&0xFF));
                                    sendToSignal(buf, (byte) NO_CONFIRM);
                                }
                                if ((inData[3]&0xFF)!=lastID){
                                    signBuff = Arrays.copyOfRange(inData,7,len);
                                    lastID= (byte) (inData[3]&0xFF);
                                    Log.i(TAG, " In:  "+ byteArrayToHex(inData, len));

                                    signUDPok = true;


                                }
                                else{
                                    Log.i(TAG, " Receive repeat signal:  "+ inData[4]);
                                }
                            }
                            if ((ps & 0xFFFF00) == 0x010100){                   // STUN responce
                                stunBuff = Arrays.copyOf(inData, len);
                                stunUDPok = true;
                                Log.i(TAG, "Alt In:  "+ byteArrayToHex(inData, len));

                            }
                        }
                   }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }).start();

    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            byte[] buf = bundle.getByteArray("NewPacket");
            uL.onRxUDP(buf);
        }
    };


    void send(final byte[] buffer, final byte attempt, int hCmd, int dCmd){
        hostCmd=hCmd;
        devCmd=dCmd;
        confirmOk=false;
        new Thread(new Runnable() {
            @Override
            public void run() {

                byte[] outData = new byte[buffer.length+7];
                System.arraycopy(buffer, 0, outData, 7, buffer.length);
                outData[0] = (byte) (pass/0x10000);
                outData[1] = (byte) (pass/0x100);
                outData[2] = (byte) (pass & 0xFF);
                outData[3] = (byte) (currentID & 0xFF);
                outData[4] = attempt;
                outData[5] = 0;
                outData[6] = 0;
                Log.i(TAG, " Out: "+  destIP + " : "+destPort+" - "+byteArrayToHex(outData, outData.length));
                try (DatagramSocket socketUDP = new DatagramSocket(null)) {
                    socketUDP.setReuseAddress(true);
                    socketUDP.bind(new InetSocketAddress(localPort));
                    DatagramPacket outUDP = new DatagramPacket(outData, outData.length, InetAddress.getByName(destIP), destPort);
                    socketUDP.send(outUDP);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    void sendToSignal(final byte[] buffer, final byte attempt){
        signUDPok=false;
        new Thread(new Runnable() {
            @Override
            public void run() {

                byte[] outData = new byte[buffer.length+7];
                System.arraycopy(buffer, 0, outData, 7, buffer.length);
                outData[0] = (byte) (SIGN_PASS/0x10000);
                outData[1] = (byte) (SIGN_PASS/0x100);
                outData[2] = (byte) (SIGN_PASS & 0xFF);
                outData[3] = (byte) (currentID & 0xFF);
                outData[4] = attempt;
                outData[5] = 0;
                outData[6] = 0;
                Log.i(TAG, " Out: "+  signalIP + " : "+SIGN_PORT+" - "+byteArrayToHex(outData, outData.length));
                try (DatagramSocket socketUDP = new DatagramSocket(null)) {
                    socketUDP.setReuseAddress(true);
                    socketUDP.bind(new InetSocketAddress(localPort));
                    DatagramPacket outUDP = new DatagramPacket(outData, outData.length, InetAddress.getByName(signalIP), SIGN_PORT);
                    socketUDP.send(outUDP);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    void sendStunPacket(final byte[] buffer){
        stunUDPok=false;
        new Thread(new Runnable() {
            @Override
            public void run() {

                Log.i(TAG, " Out: "+  defStunIP + " : "+defStunPort);
                Log.i(TAG, " Out: "+  byteArrayToHex(buffer, buffer.length));
                try (DatagramSocket socketUDP = new DatagramSocket(null)) {
                    socketUDP.setReuseAddress(true);
                    socketUDP.bind(new InetSocketAddress(localPort));
                    DatagramPacket outUDP = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(defStunIP), defStunPort);
                    socketUDP.send(outUDP);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }


    int getWBbyte(int index){
        if (index<this.workBuff.length){
            return this.workBuff[index]&0xFF;
        }else{
            return 0;
        }
    }

    byte[] getWBpart(int from, int to){
        if ((from<to)&&(to<=this.workBuff.length)){
            return Arrays.copyOfRange(this.workBuff, from, to);
        }else{
            return Arrays.copyOfRange(this.workBuff, 0, 0);
        }
    }

    byte[] getWB(){
        return this.workBuff;
    }

    /*
    int getStunByte(int index){
        if (index<this.stunBuff.length){
            return this.stunBuff[index]&0xFF;
        }else{
            return 0;
        }
    }


    byte[] getStunPart(int from, int to){
        if (from<=to){
            if (to<this.stunBuff.length){
                return Arrays.copyOfRange(this.stunBuff, from, to);
            }else{
                return Arrays.copyOfRange(this.stunBuff, 0, this.stunBuff.length);
            }
        }
        return Arrays.copyOfRange(this.stunBuff, 0, this.stunBuff.length);
    }
     */

    byte[] getMappetData(){
        if (stunBuff.length>32){
            return Arrays.copyOfRange(this.stunBuff, 26, 32);
        }else{
            return Arrays.copyOfRange(this.stunBuff, 0, this.stunBuff.length);
        }
    }

    byte[] getSignalBuffer(){
        return this.signBuff;
    }

    boolean waitForConfirm(){
        return !confirmOk;
    }





    boolean waitForSignalUDP(){
        return !signUDPok;
    }

    boolean waitForStunUDP(){
        return !stunUDPok;
    }


    byte getCurrentID(){
        return this.currentID;
    }

    void setCurrentID(byte value){
        this.currentID=value;
    }
    void incCurrentID(){
        this.currentID++;
    }

    void setDestIP(String destIP){
        this.destIP = destIP;
        Log.i(TAG, " UDPserver.setDestIP: "+ destIP);
    }

    void setSignalIP(String ip){
        this.signalIP=ip;
    }

    void setPass(int pass){
        this.pass=pass;
    }

    void setDestPort(int port){
        this.destPort=port;
    }



    private String byteArrayToHex(byte[] b, int len){
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



}
