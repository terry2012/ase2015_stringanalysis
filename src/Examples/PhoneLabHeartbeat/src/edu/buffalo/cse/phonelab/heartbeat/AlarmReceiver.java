package edu.buffalo.cse.phonelab.heartbeat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		try {
			Heartbeat.setStatic(context);
		} catch (Exception e) {
			Log.v("HeartbeatService", e.toString());
			return;
		}
		HeartbeatService.acquireLock(context);
		context.startService(new Intent(context, HeartbeatService.class));
	}
}
