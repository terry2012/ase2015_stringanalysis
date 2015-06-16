package edu.buffalo.cse.phonelab.heartbeat;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

public class BootReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent arg1) {	
		AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		PendingIntent intent = PendingIntent.getBroadcast(context, 0, new Intent(context, AlarmReceiver.class), PendingIntent.FLAG_UPDATE_CURRENT);		
		manager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + HeartbeatService.INITIAL_DELAY_MS, HeartbeatService.INTERVAL, intent);
	}
}
