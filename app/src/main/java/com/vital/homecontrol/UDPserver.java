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

public class UDPserver {

    private static final String TAG = "MyclassUDPserver";
    private static final String UDP_RCV = "UDP_received";
    private static final String UDP_PACKET_RCV = "UDP_PacketReceived";

    private static final int MSG_RCV_OK      =  0xA5;
    private static final int NO_CONFIRM      =  0xFF;

    private Context context;
    private String destIP;
    private int destPort;
    private int localPort;
    private int pass;
    byte lastID;
    private byte currentID;
    int hostCmd;
    int devCmd;
    private boolean confirmOk;
    private boolean packetUDPok;
    private boolean listen;

    private byte[] workBuff;
    private byte[] allBuff;
//    Boolean recieved;

    UDPserver(Context context, String destIP, int destPort, int localPort, int pass){
        this.context = context;
        this.destIP = destIP;
        this.destPort = destPort;
        this.localPort = localPort;
        this.workBuff = new byte[255];
        this.allBuff = new byte[511];
        this.pass = pass;
        this.lastID = 0;
        this.currentID = 1;
//        this.recieved = false;
        this.confirmOk = false;
        this.packetUDPok = false;
        this.hostCmd = 0;
        this.devCmd = 0;
        this.listen = false;
        Log.i(TAG, " New UDP-server listen "+ destIP);

    }

    void start(){
        listen = true;
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
                    while (listen){
                        DatagramPacket inUDP = new DatagramPacket(inData, inData.length);    // first 3 bytes - pass
                        sUDP.receive(inUDP);                                                // next byte - packet ID, next - attempt, next 2 - reserved
                        int len = inUDP.getLength();                                        // total - 7
                        if (len>0){
                            int ps = (((inData[0]&0x7F) << 16) +((inData[1]&0xFF)<<8)+(inData[2]&0xFF));
                            if (ps == pass){
                                if ((inData[3]&0xFF)!=lastID){
                                    workBuff = Arrays.copyOfRange(inData,7,len);
                                    lastID= (byte) (inData[3]&0xFF);
//                                recieved = true;
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
                                /*
                                String st;
                                if (recvBuff.length>3){
                                    st=" In: recvBuff[0]="+(recvBuff[0]&0xFF)+", hostCmd="+hostCmd+", recvBuff[3]="+(recvBuff[3]&0xFF)+", devCmd="+devCmd;
                                }else{
                                    st=" In: recvBuff[0]="+(recvBuff[0]&0xFF)+", hostCmd="+hostCmd;
                                }
                                Log.i(TAG, st +", "+ hashCode());

                                 */


                                    if (workBuff[0]!=(byte)MSG_RCV_OK){
                                        if ((inData[4]&0xFF)!=NO_CONFIRM){
                                            currentID++;
                                            byte[] buf = {(byte) MSG_RCV_OK};
                                            Log.i(TAG, " In: sentOk to "+(inData[4]&0xFF));
                                            send(buf, buf.length, (byte) NO_CONFIRM, 0, 0);
                                        }
                                    }


                                }
                                else{
                                    Log.i(TAG, " Receive repeat:  "+ inData[4]);
                                }
                                Intent intent = new Intent(UDP_RCV);
                                intent.putExtra("Buffer", Arrays.copyOf(inData, len));
                                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                            }else{
                                allBuff = Arrays.copyOf(inData, len);

                            }
                            packetUDPok = true;
                        }
                   }
                    Log.i(TAG, " Listen = " + listen);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }).start();

    }

    void send(final byte[] buffer, final int len, final byte attempt, int hCmd, int dCmd){
//        recieved = false;
        hostCmd=hCmd;
        devCmd=dCmd;
        confirmOk=false;
        packetUDPok=false;
//        Log.i(TAG, " UDPserver(send): hostCmd="+hostCmd+", devCmd="+devCmd +", "+ hashCode());
        new Thread(new Runnable() {
            @Override
            public void run() {
                DatagramSocket socketUDP = null;
                byte[] outData = new byte[len+7];
                System.arraycopy(buffer, 0, outData, 7, len);
                outData[0] = (byte) ((pass/0x10000) | 0x80);
                outData[1] = (byte) (pass/0x100);
                outData[2] = (byte) (pass & 0xFF);
                outData[3] = (byte) (currentID & 0xFF);
                outData[4] = attempt;
                outData[5] = 0;
                outData[6] = 0;
                String msg = destIP;
                Log.i(TAG, " Out: "+  destIP + " : "+destPort);
                Log.i(TAG, " Out: "+  byteArrayToHex(outData, outData.length));

                try {
                    socketUDP = new DatagramSocket(null);
                    socketUDP.setReuseAddress(true);
                    socketUDP.bind(new InetSocketAddress(localPort));
                    DatagramPacket outUDP = new DatagramPacket(outData, outData.length, InetAddress.getByName(destIP), destPort);
                    socketUDP.send(outUDP);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                finally {
                    if (socketUDP != null){
                        socketUDP.close();
                    }
                }
            }
        }).start();

    }

    void sendUdpPacket(final byte[] buffer, final int len, final String ip, final int port){
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


    public int getWBbyte(int index){
        if (index<this.workBuff.length){
            return this.workBuff[index]&0xFF;
        }else{
            return 0;
        }
    }

    public byte[] getWBpart(int from, int to){
        if ((from<to)&&(to<=this.workBuff.length)){
            return Arrays.copyOfRange(this.workBuff, from, to);
        }else{
            return Arrays.copyOfRange(this.workBuff, 0, 0);
        }
    }

    public byte[] getWB(){
        return this.workBuff;
    }

    public int getABbyte(int index){
        if (index<this.allBuff.length){
            return this.allBuff[index]&0xFF;
        }else{
            return 0;
        }
    }

    public byte[] getABpart(int from, int to){
        if ((from<to)&&(to<=this.allBuff.length)){
            return Arrays.copyOfRange(this.allBuff, from, to);
        }else{
            return Arrays.copyOfRange(this.allBuff, 0, 0);
        }
    }

    public byte[] getAB(){
        return this.allBuff;
    }

    public boolean getConfirm(){
        return this.confirmOk;
    }





    public boolean getPacketOk(){
        return this.packetUDPok;
    }


    public byte getCurrentID(){
        return this.currentID;
    }

    public void setCurrentID(byte value){
        this.currentID=value;
    }
    public void incCurrentID(){
        this.currentID++;
    }

    public void setDestIP(String destIP){
        this.destIP = destIP;
        Log.i(TAG, " UDPserver.setDestIP: "+ destIP);
    }

    public void setPass(int pass){
        this.pass=pass;
    }

    public void setDestPort(int port){
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
