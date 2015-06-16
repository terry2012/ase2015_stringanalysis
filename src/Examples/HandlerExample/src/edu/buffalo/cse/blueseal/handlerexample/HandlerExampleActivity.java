package edu.buffalo.cse.blueseal.handlerexample;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;

public class HandlerExampleActivity extends Activity {
    private static final String TAG = HandlerExampleActivity.class.getCanonicalName();
    
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            Bundle data = msg.getData();
            String key = data.getString("key");
            Log.v(TAG, key);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_handler_example);
    }

    public void onClick(View v) {
        new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(1000);
                    
                    Bundle data = new Bundle();
                    data.putString("key", "value");
                    Message msg = new Message();
                    msg.setData(data);
                    mHandler.sendMessage(msg);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.handler_example, menu);
        return true;
    }

}
