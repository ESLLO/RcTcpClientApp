package com.esllo.rccar.rccar;

import android.util.Log;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by Esllo on 2017-12-22.
 */

public class NetworkClient extends  Thread{
    private Socket socket;
    private String ip = "210.115.226.242";
    private int port = 5126;
    private InputThread inThread;
    public boolean runFlag = true;
    public NetworkClient(String ip){
        this.ip = ip;
        Log.d("sheet", "init : "+ip);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    socket = new Socket(NetworkClient.this.ip, port);
                    inThread = new InputThread(socket.getInputStream());
                    inThread.setDaemon(true);
                    inThread.start();
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void writeBytes(byte... bytes){
        try {
            socket.getOutputStream().write(bytes[0]);
            socket.getOutputStream().write(bytes[1]);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public byte[] convert(int angle, int acc, int brk, int left, int right){
        int bit = acc;
        bit = (bit<<2)|brk;
        bit = (bit<<1)|left;
        bit = (bit<<1)|right;
        return new byte[]{(byte)angle, (byte)bit};
    }

    class InputThread extends Thread{
        private InputStream in;
        public InputThread(InputStream in){
            this.in = in;
        }

        @Override
        public void run() {
            int read = 0;
            try {
                while (runFlag && (read = in.read()) != -1) {
                    Log.d("Sheet", "received : "+read);
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    public void close(){
        try {
            runFlag = false;
            socket.close();
            inThread.interrupt();
            inThread.join();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

}
