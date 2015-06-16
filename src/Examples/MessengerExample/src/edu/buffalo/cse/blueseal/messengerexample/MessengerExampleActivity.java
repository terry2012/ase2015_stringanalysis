package edu.buffalo.cse.blueseal.messengerexample;

import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.view.Menu;
import android.view.View;

public class MessengerExampleActivity extends Activity {
    private Messenger mMessenger = null;
    private boolean mBound = false;
    
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mMessenger = new Messenger(service);
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mMessenger = null;
            mBound = false;
        }
        
    }; 

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messenger_example);
        
        
    }

    protected void onStart() {
        super.onStart();
        bindService(new Intent(this, MessengerExampleService.class), mConnection, Context.BIND_AUTO_CREATE);
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
            try {
                mMessenger.send(Message.obtain(null, 0, "Test String"));
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.messenger_example, menu);
        return true;
    }

}
