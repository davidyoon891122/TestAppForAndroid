package com.example.mysocketservice;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.nio.ByteBuffer;


public class MainActivity extends AppCompatActivity {

    final int STATUS_DISCONNECTED = 0;
    final int STATUS_CONNECTED = 1;
    final static String tag = "MainActivity";

    String ip = "10.131.150.171";

    SocketManager socketManager = null;
    Button connectButton = null;
    Button sendDataButton = null;
    Button checkConnectionButton = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connectButton = findViewById(R.id.connectButton);
        sendDataButton = findViewById(R.id.sendDataButton);
        checkConnectionButton = findViewById(R.id.checkConnectionButton);

        //onClickEvent for connect Button
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    connectToServer();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        //onClickEvent for sending data to server
        sendDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    byte[] dataBytes = new byte[7];
                    ByteBuffer byteBuffer = ByteBuffer.wrap(dataBytes);
                    byteBuffer.putShort((short)"Hello".length());
                    byteBuffer.put("Hello".getBytes("UTF-8"));

                    Log.i(tag, "dataBytes :" + dataBytes);
                    Log.i(tag, "dataBytes Length :" + dataBytes.length);

                    sendData(dataBytes);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }
        });

        checkConnectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    disconnect();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });



    }


    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(tag, "onResume()");

        socketManager = SocketManager.getInstance();
    }


    public void connectToServer() throws RemoteException {
        if (socketManager.getStatus() == STATUS_CONNECTED) {
            Toast.makeText(this, "The connection is alive", Toast.LENGTH_SHORT).show();
        }else {
            socketManager.setSocket(ip);
            socketManager.connect();
        }
    }


    public void sendData(byte[] data) throws RemoteException {
        if (socketManager.getStatus() == STATUS_CONNECTED) {
            socketManager.send(data);
        } else {
            Toast.makeText(this, "Not connected to server", Toast.LENGTH_SHORT).show();
        }
    }


    public void receivedData() throws RemoteException {
        if(socketManager.getStatus() == STATUS_CONNECTED) {
            socketManager.receive();
        } else {
            Toast.makeText(this, "Not connected to server", Toast.LENGTH_SHORT).show();
        }
    }

    public void disconnect() throws RemoteException {
        if(socketManager.getStatus() == STATUS_CONNECTED) {
            socketManager.disconnect();
            Toast.makeText(this, "disconnect Server", Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(this, "There is not a connection", Toast.LENGTH_SHORT).show();
        }
    }


}
