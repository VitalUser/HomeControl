package com.vital.homecontrol;

import android.content.Context;
import android.content.Intent;
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

    private static final int DEF_PASS        =  0xA8A929;
    private static final int SIGN_PASS       =  0xAFA55A;
    private static final int SIGN_PORT       =  16133;
    private static final int MSG_RCV_OK      =  0xA5;
    private static final int NO_CONFIRM      =  0xFF;

    private static final int localPort = 55550;

    private Context context;
    private String destIP;
    private int destPort;
    private int pass;
    byte lastID;
    private byte currentID;
    int hostCmd;
    int devCmd;
    private boolean confirmOk;
    private boolean packetUDPok;
//    private boolean listen;

    private byte[] workBuff;
    private byte[] allBuff;

    UDPserver(Context context, int pass){
        this.context = context;
        this.destIP = "0.0.0.0";
        this.destPort = 0;
        this.workBuff = new byte[255];
        this.allBuff = new byte[511];
        this.pass = pass;
        this.lastID = 0;
        this.currentID = 1;
        this.confirmOk = false;
        this.packetUDPok = false;
        this.hostCmd = 0;
        this.devCmd = 0;
        Log.i(TAG, " New UDP-server listen ");

    }

    void start(){
        new Thread(new Runnable(){

            @Override
            public void run() {
                try {
                    confirmOk = false;
                    packetUDPok = false;
                    DatagramSocket sUDP = new DatagramSocket(null);
                    sUDP.setReuseAddress(true);
                    sUDP.bind(new InetSocketAddress(localPort));
                    byte[] inData = new byte[512];
                    while (true){
                        DatagramPacket inUDP = new DatagramPacket(inData, inData.length);    // first 3 bytes - pass
                        sUDP.receive(inUDP);                                                // next byte - packet ID, next - attempt, next 2 - reserved
                        int len = inUDP.getLength();                                        // total - 7
                        if (len>7){
                            int ps = (((inData[0]&0x7F) << 16) +((inData[1]&0xFF)<<8)+(inData[2]&0xFF));
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

                                    Intent intent = new Intent(UDP_RCV);
                                    intent.putExtra("Buffer", Arrays.copyOf(inData, len));
                                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

                                }
                                else{
                                    Log.i(TAG, " Receive repeat:  "+ inData[4]);
                                }
                            }else{
                                allBuff = Arrays.copyOf(inData, len);
                                packetUDPok = true;
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

    void send(final byte[] buffer, final byte attempt, int hCmd, int dCmd){
        hostCmd=hCmd;
        devCmd=dCmd;
        confirmOk=false;
        new Thread(new Runnable() {
            @Override
            public void run() {

                byte[] outData = new byte[buffer.length+7];
                System.arraycopy(buffer, 0, outData, 7, buffer.length);
                outData[0] = (byte) ((pass/0x10000) | 0x80);
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

    void sendToSignal(final byte[] buffer, final byte attempt, final String dstIP){
        packetUDPok=false;
        new Thread(new Runnable() {
            @Override
            public void run() {

                byte[] outData = new byte[buffer.length+7];
                System.arraycopy(buffer, 0, outData, 7, buffer.length);
                outData[0] = (byte) ((DEF_PASS/0x10000) | 0x80);
                outData[1] = (byte) (DEF_PASS/0x100);
                outData[2] = (byte) (DEF_PASS & 0xFF);
                outData[3] = (byte) (currentID & 0xFF);
                outData[4] = attempt;
                outData[5] = 0;
                outData[6] = 0;
                Log.i(TAG, " Out: "+  dstIP + " : "+SIGN_PORT+" - "+byteArrayToHex(outData, outData.length));
                try (DatagramSocket socketUDP = new DatagramSocket(null)) {
                    socketUDP.setReuseAddress(true);
                    socketUDP.bind(new InetSocketAddress(localPort));
                    DatagramPacket outUDP = new DatagramPacket(outData, outData.length, InetAddress.getByName(dstIP), SIGN_PORT);
                    socketUDP.send(outUDP);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    void sendUdpPacket(final byte[] buffer, final String ip, final int port){
        packetUDPok=false;
        new Thread(new Runnable() {
            @Override
            public void run() {

                Log.i(TAG, " Out: "+  ip + " : "+port);
                Log.i(TAG, " Out: "+  byteArrayToHex(buffer, buffer.length));
                try (DatagramSocket socketUDP = new DatagramSocket(null)) {
                    socketUDP.setReuseAddress(true);
                    socketUDP.bind(new InetSocketAddress(localPort));
                    DatagramPacket outUDP = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(ip), port);
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

    int getABbyte(int index){
        if (index<this.allBuff.length){
            return this.allBuff[index]&0xFF;
        }else{
            return 0;
        }
    }

    byte[] getABpart(int from, int to){
        if (from<=to){
            if (to<this.allBuff.length){
                return Arrays.copyOfRange(this.allBuff, from, to);
            }else{
                return Arrays.copyOfRange(this.allBuff, 0, this.allBuff.length);
            }
        }
        return Arrays.copyOfRange(this.allBuff, 0, this.allBuff.length);
    }

    byte[] getAB(){
        return this.allBuff;
    }

    boolean getConfirm(){
        return this.confirmOk;
    }





    boolean getPacketOk(){
        return this.packetUDPok;
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

    void setPass(int pass){
        this.pass=pass;
    }

    void setDestPort(int port){
        this.destPort=port;
    }

    int getLastID(){
        return this.lastID;
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
