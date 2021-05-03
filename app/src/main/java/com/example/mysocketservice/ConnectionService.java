package com.example.mysocketservice;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import androidx.annotation.Nullable;

public class ConnectionService extends Service {
    final static String tag = "ConnectionService";
    final int STATUS_CONNECTED = 1;
    final int STATUS_DISCONNECTED = 0;

    final int TIME_OUT = 5000;

    private int status = STATUS_DISCONNECTED;
    private Socket socket = null;
    private SocketAddress socketAddress = null;
    private DataInputStream dataInputStream = null;
    private DataOutputStream dataOutputStream = null;
    private int port = 13302;
    Handler handler;

    IConnectionService.Stub binder = new IConnectionService.Stub() {
        @Override
        public int getStatus() throws RemoteException {
            return status;
        }

        @Override
        public void setSocket(String ip) throws RemoteException {
            mySetSocket(ip);

        }

        @Override
        public void connect() throws RemoteException {
            myConnect();

        }

        @Override
        public void disconnect() throws RemoteException {
            myDisconnect();
        }

        @Override
        public void send(byte[] data) throws RemoteException {
            mySend(data);
        }

        @Override
        public void receive() throws RemoteException {
            myReceive();
        }
    };


    public ConnectionService(){

    }

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler();
        Log.i(tag, "onCreate()");
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(tag, "onStartCommand()");

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(tag, "onDestroy()");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(tag, "onBind()");
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(tag, "onUnbind()");
        return super.onUnbind(intent);
    }

    void mySetSocket(String ip) {
        socketAddress = new InetSocketAddress(ip, port);
        Log.i(tag, "mySetSocket()");
    }

    void myConnect() {
        Log.i(tag, "myConnect()");
        socket = new Socket();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    socket.connect(socketAddress, TIME_OUT);
                    dataOutputStream = new DataOutputStream(socket.getOutputStream());
                    dataInputStream = new DataInputStream(socket.getInputStream());
                    Log.i(tag, "complete connection");
                } catch (Exception e) {
                    e.printStackTrace();
                    _runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "error on connection ", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                status = STATUS_CONNECTED;
            }
        }).start();
    }

    void myDisconnect() {
        try {
            if(dataInputStream != null) dataInputStream.close();
            if(dataOutputStream != null) dataOutputStream.close();
            if(socket != null) socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        status = STATUS_DISCONNECTED;
    }

    void mySend(byte[] data) {
        final byte[] byteData = data;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    dataOutputStream.write(byteData);

                    dataOutputStream.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }



    void myReceive() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final byte[] receivedData = new byte[256];
                    dataInputStream.readFully(receivedData);
                    Log.i(tag, "Received Data : " + receivedData);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Received Message : " + receivedData, Toast.LENGTH_SHORT ).show();
                        }
                    });
                }catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }).start();
    }


    private void _runOnUiThread(Runnable runnable) {
        handler.post(runnable);
    }
}
