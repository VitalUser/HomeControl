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
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

class UDPserver {

    private static final String TAG = "MyclassUDPserver";
//    private static final String UDP_PACKET_RCV = "UDP_PacketReceived";

//    private static final int DEF_PASS        =  0xA8A929;
    private static final int SIGN_PASS       =  0xAFA55A;
    private static final int SIGN_PORT       =  16133;
    private static final int MSG_RCV_OK      =  0xA5;
    private static final int NO_CONFIRM      =  0xFF;

    private static final int localPort = 55550;
    private static final int liconPort = 55300;
    private static final String defStunIP = "216.93.246.18";
    private static final int defStunPort = 3478;

    private final UDPlistener uL;
    private final Context context;
    private String destIP;
    private String signalIP;
    private int destPort;
    private int pass;
    byte lastID;
    private byte currentID;
    public boolean signUDPok;
    private boolean stunUDPok;
//    private boolean listen;

    private byte[] signBuff;
    private byte[] stunBuff;


//    public List<ReceivedPacket> rBuffer = new ArrayList<>();
//    private int pIndex;

    public List<WaitPacket> waitBuf = new ArrayList<>();


    UDPserver(Context context, int pass, UDPlistener uL){
        this.context = context;
        this.destIP = "0.0.0.0";
        this.signalIP = "0.0.0.0";
        this.destPort = 0;
        this.signBuff = new byte[255];
        this.stunBuff = new byte[511];
        this.pass = pass;
        this.lastID = 0;
        this.currentID = 1;
        this.signUDPok = false;
        this.stunUDPok = false;
        this.uL = uL;
        Log.i(TAG, " New UDP-server listen ");

    }

    public interface UDPlistener{
        void onRxUDP(byte[] buf);
    }



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
//                                    workBuff = Arrays.copyOfRange(inData,7,len);
                                    lastID= (byte) (inData[3]&0xFF);
                                    Log.i(TAG, " In:  "+ byteArrayToHex(inData, len));

                                    int wInd = getWaitIndex(inData[7]&0xFF, inData[10]&0xFF);
                                    if (wInd>=0){
                                        waitBuf.get(wInd).packet = Arrays.copyOfRange(inData,0,len);
                                        waitBuf.get(wInd).received=true;
                                    }

                                    Bundle bundle = new Bundle();
                                    Message msg = handler.obtainMessage();
                                    bundle.putByteArray("NewPacket", Arrays.copyOf(inData, len));
//                                    bundle.putInt("NewPacketID", pIndex);
                                    msg.setData(bundle);
                                    handler.sendMessage(msg);


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
                                    sendToSignal(buf);
                                }
                                if ((inData[3]&0xFF)!=lastID){
                                    signBuff = Arrays.copyOfRange(inData,7,len);
                                    lastID= (byte) (inData[3]&0xFF);
                                    Log.i(TAG, " In from Sign:  "+ byteArrayToHex(inData, len));

                                    signUDPok = true;


                                }
                                else{
                                    Log.i(TAG, " Receive repeat signal:  "+ inData[4]);
                                }
                            }
                            if ((ps & 0xFFFF00) == 0x010100){                   // STUN responce
                                stunBuff = Arrays.copyOf(inData, len);
                                stunUDPok = true;
                                Log.i(TAG, "In from STUN:  "+ byteArrayToHex(inData, len));

                            }
                        }
                   }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }).start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    DatagramSocket lUDP = new DatagramSocket(null);
                    lUDP.setReuseAddress(true);
                    lUDP.bind(new InetSocketAddress(55301));
                    byte[] inData = new byte[512];
                    while (true){
                        DatagramPacket inUDP = new DatagramPacket(inData, inData.length);
                        lUDP.receive(inUDP);
                        int len = inUDP.getLength();
                        if (len>7){
                            int ps = (((inData[0]&0xFF) << 16) +((inData[1]&0xFF)<<8)+(inData[2]&0xFF));
                            if (ps == 0xA8A929){
                                String ip = inUDP.getAddress().getHostName();
                                if ((inData[4]&0xFF)!=NO_CONFIRM){
                                    byte[] buf = {(byte) MSG_RCV_OK};
                                    Log.i(TAG, " In: sentOk to "+(inData[4]&0xFF));
                                    sendLiCon(buf, (byte) NO_CONFIRM, ip);
                                }
                                if ((inData[3]&0xFF)!=lastID){
//                                    liconBuff = Arrays.copyOfRange(inData,7,len);
                                    lastID= (byte) (inData[3]&0xFF);
                                    Log.i(TAG, " In from LiCon:  "+ byteArrayToHex(inData, len));

                                    Bundle bundle = new Bundle();
                                    Message msg = handler.obtainMessage();
                                    bundle.putByteArray("NewPacket", Arrays.copyOf(inData, len));
//                                    bundle.putInt("NewPacketID", pIndex);
                                    msg.setData(bundle);
                                    handler.sendMessage(msg);

                                }
                                else{
                                    Log.i(TAG, " Receive repeat work:  "+ inData[4]);
                                }
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
    private final Handler handler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            byte[] buf = bundle.getByteArray("NewPacket");
            uL.onRxUDP(buf);
        }
    };

    void sendAnyUDP(final byte[] buffer, String ip, int port){
        incCurrentID();
        buffer[3] = currentID;
        try (DatagramSocket socketUDP = new DatagramSocket(null)) {
            socketUDP.setReuseAddress(true);
            socketUDP.bind(new InetSocketAddress(localPort));
            DatagramPacket outUDP = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(ip), port);
            socketUDP.send(outUDP);
            Log.i(TAG, " Out sendAnyUDP: "+  ip + " : "+port+" - "+byteArrayToHex(buffer, buffer.length));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }



    void send(final byte[] buffer, final byte attempt, int hCmd, int dCmd){
//        hostCmd=hCmd;
//        devCmd=dCmd;
        if (hCmd>0){
            int ind = getWaitIndex(hCmd, dCmd);
            if (ind<0){
                waitBuf.add(new WaitPacket(hCmd, dCmd));
            }else{
                waitBuf.get(ind).received = false;
                waitBuf.get(ind).packet = new byte[]{};
            }

        }

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

    void sendLiCon(final byte[] buffer, final byte attempt, final String liconIP){
        new Thread(new Runnable() {
            @Override
            public void run() {
                byte[] outData = new byte[buffer.length+7];
                System.arraycopy(buffer, 0, outData, 7, buffer.length);
                outData[0] = (byte) 0xA8;
                outData[1] = (byte) 0xA9;
                outData[2] = (byte) 0x29;
                outData[3] = (byte) (new Random().nextInt(255));
                outData[4] = attempt;
                outData[5] = 0;
                outData[6] = 0;
                Log.i(TAG, " Out sendLiCon: "+  liconIP + " : "+liconPort+" - "+byteArrayToHex(outData, outData.length));
                try (DatagramSocket socketUDP = new DatagramSocket(null)) {
                    socketUDP.setReuseAddress(true);
                    socketUDP.bind(new InetSocketAddress(55301));
                    DatagramPacket outUDP = new DatagramPacket(outData, outData.length, InetAddress.getByName(liconIP), liconPort);
                    socketUDP.send(outUDP);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    void sendToSignal(final byte[] buffer){
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
                outData[4] = (byte) 0xFF;
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

    /*
    public int getRcvIndex(int pID){
        int pInd = this.rBuffer.size()-1;
        while ((pInd>0)&&(this.rBuffer.get(pInd).index!=pID)){
            pInd--;
        }
        return pInd;
    }
    */

    public int getWaitIndex(int hCmd, int dCmd){
        int pInd = waitBuf.size();
        boolean found = false;
        while ((pInd>0)&&(!found)){
            pInd--;
            if (waitBuf.get(pInd).devCmd>0){
                if ((waitBuf.get(pInd).hostCmd==hCmd)&&(waitBuf.get(pInd).devCmd==dCmd)){
                    found=true;
                }
            }else{
                if (waitBuf.get(pInd).hostCmd==hCmd)
                    found=true;
            }
        }
        if (!found)
            pInd=-1;
        return pInd;
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

    int getPass(){
        return this.pass;
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

    /*
    public class ReceivedPacket{
        public int index;
        public byte[] packet;

        public ReceivedPacket(int index, byte[] packet) {
            this.index = index;
            this.packet = packet;
        }
    }
    */

    public static class WaitPacket{
        int hostCmd;
        int devCmd;
        boolean received;
        byte[] packet;

        public WaitPacket(int hostCmd, int devCmd){
            this.hostCmd=hostCmd;
            this.devCmd=devCmd;
            this.received=false;
            this.packet= new byte[]{};
        }
    }



}
