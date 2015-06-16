package edu.buffalo.cse.blueseal.asynctaskexample;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getCanonicalName();
    private static final int TRAFFIC_CAP = 1024 * 1024 * 1024;
    private static final int PORT = 10000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    
    public void onClick(View v) {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            new Task().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    private class Task extends AsyncTask<ServerSocket, String, Integer> {

        protected void onPreExecute() {
            Log.v(TAG, "onPreExecute()");
        }
        
        @Override
        protected Integer doInBackground(ServerSocket... sockets) {
            String msg = null;
            ServerSocket serverSocket = sockets[0];
            Socket socket;
            int cnt = 0;
            
            try {
                while (true) {
                    socket = serverSocket.accept();
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    msg = in.readLine();
                    publishProgress(msg);
                    cnt += msg.length();
                    if (cnt >= TRAFFIC_CAP)
                        break;
                    socket.close();
                }
            } catch (IOException e) {
            }
            return Integer.valueOf(cnt);
        }

        protected void onProgressUpdate(String...strings) {
            Toast.makeText(getApplicationContext(), strings[0], Toast.LENGTH_SHORT).show();

            return;
        }
        
        protected void onPostExecute(Integer cnt) {
            Log.v(TAG, "onPostExecute(): " + cnt.toString());
        }
        
        protected void onCancelled(Integer cnt) {
            Log.v(TAG, "onCancelled(): " + cnt.toString());
        }
    }

}
