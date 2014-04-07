package com.gp.wirelessftptransfer;

import org.apache.ftpserver.ftplet.FtpException;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;

public class WirelessFtpService extends Service {
	FtpServerDetails fsd = null;
	public static final int NOTIFICATION_ID = 14110;
	public static final String KEY_INTENT_USERNAME = "KEY_INTENT_USERNAME";
	public static final String KEY_INTENT_STARTED = "KEY_INTENT_STARTED";
	public static final String KEY_INTENT_PASSWORD = "KEY_INTENT_PASSWORD";
	public static final String KEY_INTENT_PORT = "KEY_INTENT_PORT";
	public WirelessFtpService() {
	}

	@Override
	public IBinder onBind(Intent intent) {
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		SharedPreferences prefs = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE);
		FTPServerManager fm = new FTPServerManager(prefs);
		fsd = fm.configureServer();		
		
		Notification.Builder builder = new Notification.Builder(this);
		builder.setTicker("Wireless FTP Server is starting");
		builder.setContentTitle("Wireless FTP Server is running");
		builder.setSmallIcon(R.drawable.ic_launcher);
		Intent tint = new Intent(this, MainActivity.class);
		tint.putExtra(KEY_INTENT_STARTED, true);
		tint.putExtra(KEY_INTENT_USERNAME, fsd.getUsername());
		tint.putExtra(KEY_INTENT_PORT, fsd.getPortnumber());
		tint.putExtra(KEY_INTENT_PASSWORD, fsd.getPassword());
		tint.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		PendingIntent pendingint = PendingIntent.getActivity(this, 0, tint, PendingIntent.FLAG_UPDATE_CURRENT);
		builder.setContentIntent(pendingint);
		Notification notification = builder.getNotification();
		startForeground(NOTIFICATION_ID, notification);
		try {
			fsd.getServer().start();
		} catch (FtpException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return START_STICKY;
	}
	
	@Override
	public void onDestroy() {
		fsd.getServer().stop();
		stopForeground(true);
	}
	
}
