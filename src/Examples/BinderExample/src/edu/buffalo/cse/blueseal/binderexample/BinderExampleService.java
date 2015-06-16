package edu.buffalo.cse.blueseal.binderexample;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class BinderExampleService extends Service {
    private IBinder mBinder = new BinderExampleBinder();
    
    public class BinderExampleBinder extends Binder {
        public BinderExampleService getService() {
            return BinderExampleService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void printStr(String str) {
        Log.v(BinderExampleService.class.getCanonicalName(), str);
    }
}
