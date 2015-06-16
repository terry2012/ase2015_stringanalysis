package edu.buffalo.cse.blueseal.intentexample;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class IntentExampleService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }
    
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle extras = intent.getExtras();

        String value = extras.getString(Intent.EXTRA_TEXT);
        Log.v("value", value);
        Log.v("onStartCommand", "started");
        return START_STICKY;
    }
}
