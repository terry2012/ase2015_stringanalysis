package edu.buffalo.cse.blueseal.messengerexample;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

public class MessengerExampleService extends Service {
    private Messenger mMessenger = new Messenger(new IncomingHandler());
    
    private static class IncomingHandler extends Handler {
        public void handleMessage(Message msg) {
            Log.v(MessengerExampleService.class.getCanonicalName(), (String) msg.obj);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

}
