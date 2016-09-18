package com.di.tang.learnsocket;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

/**
 * Created by tangdi on 2016/9/13.
 */
public class TCPServerService extends Service {

    private static final String TAG = "TCPServerService";

    private boolean mIsServiceDestoryed = false;

    private String[] mDefinedMessages = new String[]{
            "hello",
            "what is your name",
            "How are you",
            "i am fine TQ"
    };

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            ServerSocket serverSocket = null;
            try{
                serverSocket = new ServerSocket(8688);
            }catch(IOException e){
                Log.e(TAG, "run: " + e.toString());
                return;
            }
            while(!mIsServiceDestoryed){
                try{
                    final Socket socket = serverSocket.accept();
                    new Thread(){
                        @Override
                        public void run(){
                            try{
                                responseClient(socket);
                            }catch (IOException e){
                                Log.e(TAG, "run: " + e.toString());
                            }
                        };
                    }.start();
                }catch(IOException e){
                    Log.e(TAG, "run: " + e.toString());
                }
            }
        }
    };

    @Override
    public void onCreate(){
        super.onCreate();
        new Thread(runnable).start();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void responseClient(Socket client) throws IOException{
        BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        PrintWriter out = new PrintWriter(new BufferedWriter(
                new OutputStreamWriter(client.getOutputStream())), true);
        out.println("welcome to room");
        while(!mIsServiceDestoryed){
            String str = in.readLine();
            if(str == null){
                break;
            }
            Log.d(TAG, "responseClient: " + str);
            int i = new Random().nextInt(mDefinedMessages.length);
            String msg = mDefinedMessages[i];
            out.println(msg);
        }
        if(out != null){
            out.close();
        }
        if(in != null){
            in.close();
        }
        if(client != null){
            client.close();
        }
    }
}
