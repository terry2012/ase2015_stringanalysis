package edu.buffalo.cse.blueseal.binderexample;

import edu.buffalo.cse.blueseal.binderexample.BinderExampleService.BinderExampleBinder;
import android.os.Bundle;
import android.os.IBinder;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.view.Menu;
import android.view.View;

public class BinderExampleActivity extends Activity {
    private BinderExampleService mService;
    private boolean mBound = false;

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BinderExampleBinder binder = (BinderExampleBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBound = false;
        }
        
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_binder_example);
    }
    
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, BinderExampleService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }
    
    protected void onStop() {
        super.onStop();
        
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }
    
    public void onClick(View v) {
        if (mBound) {
            mService.printStr("Test String");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.binder_example, menu);
        return true;
    }

}
