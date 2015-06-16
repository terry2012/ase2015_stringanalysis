package edu.buffalo.cse.phonelab.heartbeat;

import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.location.Location;
import android.location.LocationManager;
import android.os.BatteryManager;
import android.os.Build;
import android.provider.Settings.Secure;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.util.Log;

@Root
class Version {
	@Element
	public static String build;
	
	@Element
	public static Integer code;
	
	@Element
	public static String name;

	@Element
	public static String secureID;
	
	@Element
	public static String product;
	
	@Element
	public static String model;
	
	public static boolean staticDone;
	
	public static void setStatic(Context context) throws NameNotFoundException {
		
		if (staticDone == true) {
			return;
		}
		
		build = Build.DISPLAY;
		product = Build.PRODUCT;
		model = Build.MODEL;
		
		PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
		code = info.versionCode;
		name = info.versionName;
		
		StringBuilder stringBuilder = new StringBuilder();
		Formatter formatter = new Formatter(stringBuilder, Locale.US);
		formatter.format("%016X", new BigInteger(Secure.getString(context.getContentResolver(), Secure.ANDROID_ID), 16));
		formatter.close();
		secureID = stringBuilder.toString();
		
		staticDone = true;
	}
}

@Root
class Telephony {
	@Element
	public static String hashedID;
	
	public static boolean staticDone;
	
	@Element
	public String number;
	
	@Element
	public String operator;
	
	public Telephony(Context context) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		
		number = telephonyManager.getLine1Number();
		operator = telephonyManager.getNetworkOperatorName();
	}
	
	public static void setStatic(Context context) throws NoSuchAlgorithmException {
		
		if (staticDone == true) {
			return;
		}
		
		TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		
		MessageDigest digester = MessageDigest.getInstance("SHA-1");
		byte[] digest = digester.digest(telephonyManager.getDeviceId().getBytes());
		hashedID = (new BigInteger(1, digest)).toString(16);
		
		staticDone = true;
	}
}

@Root
class Battery {
	@Element
	public Float level;
	
	@Element
	public Boolean charging;
	
	public Battery(Context context) {
		Intent batteryIntent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		
		Integer amount = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
		Integer scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
		Integer status = batteryIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
		
		level = amount / scale.floatValue();
		charging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL;
	}
}

@Root
class LocationInfo {
	@Element
	String provider;
		
	@Element
	Long time;

	@Element
	Double latitude;
		
	@Element
	Double longitude;
	
	public LocationInfo(Location location) {
		provider = location.getProvider();
		time = location.getTime();
		latitude = location.getLatitude();
		longitude = location.getLongitude();
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}
		if (this == o) {
			return true;
		}
		if (!(o instanceof LocationInfo)) {
			return false;
		}
		LocationInfo l = (LocationInfo) o;
		
		return provider != null && l.provider != null && l.provider.equals(provider) &&
				time != null && l.time != null && l.time.equals(time) &&
				latitude != null && l.latitude != null && l.latitude.equals(latitude) &&
				longitude != null && l.longitude != null && l.longitude.equals(longitude);
	}
	
	@Override
	public String toString() {
		Serializer serializer = new Persister();
		StringWriter writer = new StringWriter();
		try {
			serializer.write(this, writer);
		} catch (Exception e) {
			return null;
		}
		return writer.toString();
	}
	
	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}
}

@Root
class Status {
	
	private final String START_INTENT ="edu.buffalo.cse.phonelab.services.manifest.ManifestService";
	private final String PACKAGE_NAME = "edu.buffalo.cse.phonelab.services";
	
	@Element
	Boolean installed;
	
	@Element
	Boolean running;
	
	@Element
	Boolean restarted;

	private Boolean isRunning(PackageManager packageManager) throws NameNotFoundException {
		PackageInfo packageInfo = packageManager.getPackageInfo(PACKAGE_NAME, 0);
		if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_STOPPED) == ApplicationInfo.FLAG_STOPPED) {
			return false;
		} else {
			return true;
		}
	}
	
	public Status(Context context) {
		PackageManager packageManager = context.getPackageManager();
		try {
			running = isRunning(packageManager);
			installed = true;
			if (running == false) {
				Intent intent = new Intent(START_INTENT);
				intent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
				context.startService(intent);
			}	
			restarted = isRunning(packageManager);
		} catch (NameNotFoundException e) {
			running = installed = restarted = false;
		}
	}

}

@Root
public class Heartbeat {
	
	@Element
	public Version version;
	
	@Element
	public Telephony telephony;
	
	@Element
	public Long uptime;
	
	@Element
	public Battery battery;
	
	@ElementList
	public List<LocationInfo> locations;
	
	private void setLocations(Context context) {
		
		LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		
		/*
		 * 22 Aug 2012 : GWA : getAllProviders() seems to return duplicates, so hash entries first.
		 */
		
		HashSet<LocationInfo> locationSet = new HashSet<LocationInfo>();
		
		List<String> providers = locationManager.getAllProviders();
		
		for (String provider: providers) {;
			Location location = locationManager.getLastKnownLocation(provider);
			if (location == null) {
				continue;
			} else {
				locationSet.add(new LocationInfo(location));
			}
		}
		
		locations = new ArrayList<LocationInfo>(locationSet);
	}
	
	@Element
	public Status status;
	
	public Heartbeat(Context context) throws NoSuchAlgorithmException, UnsupportedEncodingException, NameNotFoundException {
		version = new Version();
		telephony = new Telephony(context);
		battery = new Battery(context);
		status = new Status(context);
		setLocations(context);
		uptime = SystemClock.elapsedRealtime();
	}
	
	@Override
	public String toString() {
		Serializer serializer = new Persister();
		StringWriter writer = new StringWriter();
		try {
			serializer.write(this, writer);
		} catch (Exception e) {
			Log.d("HeartbeatService", e.toString());
			return null;
		}
		return writer.toString();
	}
	
	public static void setStatic(Context context) throws NoSuchAlgorithmException, NameNotFoundException {
		Version.setStatic(context);
		Telephony.setStatic(context);
	}
}
