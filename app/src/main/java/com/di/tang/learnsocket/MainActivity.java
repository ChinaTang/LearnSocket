package com.di.tang.learnsocket;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "MainActivity";

    private Socket clientScoket = null;

    private PrintWriter printWrite = null;

    private static final int SOCKECT_CONNECT_SUCCESS = 1;

    private static final int NEW_MESSAGE_COME = 2;

    private Button sendBn;

    private TextView reciveMessage;

    private EditText sendMessage;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SOCKECT_CONNECT_SUCCESS:
                    sendBn.setEnabled(true);
                    break;
                case NEW_MESSAGE_COME:
                    reciveMessage.setText((String)msg.obj);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sendBn = (Button) findViewById(R.id.send);
        sendMessage = (EditText) findViewById(R.id.send_message);
        reciveMessage = (TextView) findViewById(R.id.recive_messge);
        sendBn.setEnabled(false);
        new Thread(){
            @Override
            public void run(){
                connectTCPServer();
            }
        }.start();
        Intent intent = new Intent(MainActivity.this, TCPServerService.class);
        startService(intent);
    }

    private void connectTCPServer() {
        Socket socket = null;
        while (socket == null) {
            try {
                socket = new Socket("localhost", 8688);
                clientScoket = socket;
                printWrite = new PrintWriter(new BufferedWriter(
                        new OutputStreamWriter(socket.getOutputStream())), true);
                if (printWrite != null) {
                    mHandler.sendEmptyMessage(SOCKECT_CONNECT_SUCCESS);
                }
            } catch (IOException e) {
                Log.e(TAG, "connectTCPServer: " + e.toString());
                SystemClock.sleep(1000);
            }
        }

        try{
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(clientScoket.getInputStream()));
            while(!MainActivity.this.isFinishing()){
                String msg = br.readLine();
                if(msg != null){
                    mHandler.obtainMessage(NEW_MESSAGE_COME, msg).sendToTarget();
                }
            }
            if(printWrite != null){
                printWrite.close();
            }
            if(br != null){
                br.close();
            }
            if(clientScoket != null){
                clientScoket.close();
            }
        }catch (IOException e){
            Log.e(TAG, "connectTCPServer: " + e.toString());
        }
    }

    @Override
    public void onClick(View v) {
        final String msg = sendMessage.getText().toString();
        if(!TextUtils.isEmpty(msg) && printWrite != null){
            printWrite.print(msg);
            sendMessage.setText("");
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(clientScoket != null){
            try{
                clientScoket.shutdownInput();
                clientScoket.close();
            }catch (IOException e){
                Log.e(TAG, "onDestroy: " + e.toString());
            }
        }
    }
}
