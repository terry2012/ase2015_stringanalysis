package edu.buffalo.cse.phonelab.heartbeat;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Formatter;
import java.util.Locale;

import android.app.AlarmManager;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.provider.Settings.Secure;
import android.util.Log;

public class HeartbeatService extends IntentService {
		
	static private final String TAG = "HeartbeatService";
	
	static final long INITIAL_DELAY_MS = 5000;
	static final long INTERVAL = AlarmManager.INTERVAL_HOUR;
	static final String HEARTBEAT_URL_ROOT = "http://heartbeat.phone-lab.org/heartbeat";
	
	static final Integer HEARTBEAT_OK_RESPONSE_CODE = 200;
	
	@Override
	protected void onHandleIntent(Intent arg0) {
		Context context = getApplicationContext();
		try {
			Heartbeat heartbeat;
			try {
				heartbeat = new Heartbeat(context);
			} catch (Exception e) {
				Log.v(TAG, "Could not create heartbeat message: " + e);
				return;
			}
			
			String xml = heartbeat.toString();
			Log.v(TAG, xml);
			send(xml);
		} finally {
			releaseLock(context);
		}
	}

	private void send(String xml) {
		
		try {
			if (((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo().isConnected() == false) {	
				return;
			}
		} catch (NullPointerException e) {
			return;
		} finally {
			Log.v(TAG, "No network connection.");
		}
		
		HttpURLConnection connection;
		URL url;
		
		try {
			url = getHeartbeatURL();
			Log.v(TAG, "Uploading heartbeat to " + url.toString());
			connection = (HttpURLConnection) url.openConnection();
		} catch (IOException e) {
			Log.d(TAG, "Failed to open connection.");
			return;
		}
		
		
		try {
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "text/xml");
			connection.setDoOutput(true);
			connection.setFixedLengthStreamingMode(xml.length());
			BufferedOutputStream out = new BufferedOutputStream(connection.getOutputStream());
			
			out.write(xml.getBytes());
			out.flush();
			
			if (connection.getResponseCode() == HEARTBEAT_OK_RESPONSE_CODE) {
				Log.v(TAG, "Sending heartbeat succeeded.");
			} else {
				Log.v(TAG, "Sending heartbeat failed: " + connection.getResponseMessage());
			}
			
		} catch (Exception e) {
			Log.d(TAG, "Execption while sending heartbeat: " + e);
		}
		
		connection.disconnect();
	}

	private static WakeLock lock;
	
	synchronized public static void acquireLock(Context context) {
		if (lock == null) {
			PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
			lock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, HeartbeatService.class.getName());
			lock.setReferenceCounted(true);
		}
		lock.acquire();
	}
	
	synchronized public void releaseLock(Context context) {
		if (lock != null && lock.isHeld()) {
			lock.release();
		}
	}
	
	private URL getHeartbeatURL() throws MalformedURLException {
		
		/*
		 * 22 Aug 2012 : GWA : All this for a fixed-length hex string.
		 */
		
		StringBuilder stringBuilder = new StringBuilder();
		Formatter formatter = new Formatter(stringBuilder, Locale.US);
		formatter.format(HEARTBEAT_URL_ROOT + "/" + "%016X", new BigInteger(Secure.getString(getContentResolver(), Secure.ANDROID_ID), 16));
		formatter.close();
		
		return new URL(stringBuilder.toString());
	}
	
	public HeartbeatService() {
		super(TAG);
	}
}
