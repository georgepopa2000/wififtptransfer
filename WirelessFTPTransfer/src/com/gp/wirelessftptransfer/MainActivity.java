package com.gp.wirelessftptransfer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.util.Properties;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity {

	public static final String PREFS_NAME ="prefs wireless ftp server";
	public static final String PREFS_USERNAME = "username";
	public static final String PREFS_PASSWORD = "password";
	public static final String PREFS_HOMEDIR = "homedir";
	public static final String PREFS_PORT = "port";
	public static final String PREFS_ALLOW_ANY = "anylogin";


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		Button buton = (Button) this.findViewById(R.id.butonoff);
		
		buton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				try {
					ftpStartStop();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		Button butSave = (Button) this.findViewById(R.id.butSaveConfig);
		butSave.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				saveConfig();
			}
		});
		
		Button butSettings = (Button) this.findViewById(R.id.butSettings);
		butSettings.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent i = new Intent(MainActivity.this, SettingsActivity.class);
				startActivity(i);
			}
		});		
		
		EditText txtAddress = (EditText) this.findViewById(R.id.txtWIFIAddress);
		txtAddress.setInputType(InputType.TYPE_NULL);
		
		populateFields();
		

		/*
		PowerManager pm = (PowerManager)getSystemService(
                Context.POWER_SERVICE);
		wl = pm.newWakeLock(
            PowerManager.SCREEN_DIM_WAKE_LOCK
            | PowerManager.ON_AFTER_RELEASE,
            "frptrans");
//*/


		
		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		TextView textv = (TextView) this.findViewById(R.id.textarea_log);
		Button buton = (Button) this.findViewById(R.id.butonoff);
		if (this.getIntent().getBooleanExtra(WirelessFtpService.KEY_INTENT_STARTED,false)){
			buton.setText(this.getResources().getString(R.string.text_but_off));
			textv.setText("server online \n" +
					"host: " +wifiIpAddress()+":"+getIntent().getStringExtra(WirelessFtpService.KEY_INTENT_PORT)+"\n" +
					"user:"+getIntent().getStringExtra(WirelessFtpService.KEY_INTENT_USERNAME)+" \n" +
					"pass:"+getIntent().getStringExtra(WirelessFtpService.KEY_INTENT_PASSWORD));
		} if (isMyServiceRunning()){
			buton.setText(this.getResources().getString(R.string.text_but_off));
			SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
			
			textv.setText("server online \n" +
					"host: " +wifiIpAddress()+":"+Integer.toString(prefs.getInt(PREFS_PORT, 2121))+"\n" +
					"user:"+prefs.getString(PREFS_USERNAME, "guest")+" \n" +
					"pass:"+prefs.getString(PREFS_PASSWORD, "guest"));			
		} else {
		
			textv.setText("server stopped");
			buton.setText(this.getResources().getString(R.string.text_but_on));
		}
	}

	
	
	

	private void ftpStartStop() throws Exception{
		Button buton = (Button) this.findViewById(R.id.butonoff);
		TextView textv = (TextView) this.findViewById(R.id.textarea_log);
		if (buton.getText().equals(this.getResources().getString(R.string.text_but_on))){
			buton.setText(this.getResources().getString(R.string.text_but_off));
			
			Intent tint = new Intent(this,WirelessFtpService.class);
			startService(tint);
			SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
			
			textv.setText("server online \n" +
					"host: " +wifiIpAddress()+":"+Integer.toString(prefs.getInt(PREFS_PORT, 2121))+"\n" +
					"user:"+prefs.getString(PREFS_USERNAME, "guest")+" \n" +
					"pass:"+prefs.getString(PREFS_PASSWORD, "guest"));
		} else {
			buton.setText(this.getResources().getString(R.string.text_but_on));
			Intent tint = new Intent(this,WirelessFtpService.class);
			stopService(tint);

			textv.setText("server stopped");			

		}		
	}
	
	private void saveConfig(){
		Properties prop = new Properties();
		try {
			prop.load(new FileInputStream(new File("/mnt/sdcard/WirelessFTPTransfer/config.properties")));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
		Editor edit = prefs.edit();
		
		
	
		
		
		EditText txtUsername = (EditText) this.findViewById(R.id.txtUsername);
		edit.putString(PREFS_USERNAME, txtUsername.getText().toString());
		
		EditText txtPassword = (EditText) this.findViewById(R.id.txtPassword);
		edit.putString(PREFS_PASSWORD, txtPassword.getText().toString());
		
		EditText txtHomedir = (EditText) this.findViewById(R.id.txtHomedir);
		edit.putString(PREFS_HOMEDIR, txtHomedir.getText().toString());
		
		EditText txtPort = (EditText) this.findViewById(R.id.txtPort);
		edit.putInt(PREFS_PORT,Integer.valueOf(txtPort.getText().toString()));
		
		edit.commit();		
	}
	
	private void populateFields() {
		EditText txtServerAddress = (EditText) this.findViewById(R.id.txtWIFIAddress);
		txtServerAddress.setText(wifiIpAddress());
		
		SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
		
		
		String username = prefs.getString(PREFS_USERNAME, "guest");
		String password = prefs.getString(PREFS_PASSWORD, "guest");
		String homedir  = prefs.getString(PREFS_HOMEDIR, "/");
		String portnumber = Integer.toString(prefs.getInt(PREFS_PORT, 2121));
		
		Log.i("ftptrans", username);
		Log.i("ftptrans", password); 
		
		EditText txtUsername = (EditText) this.findViewById(R.id.txtUsername);
		txtUsername.setText(username);
		
		EditText txtPassword = (EditText) this.findViewById(R.id.txtPassword);
		txtPassword.setText(password);
		
		EditText txtHomedir = (EditText) this.findViewById(R.id.txtHomedir);
		txtHomedir.setText(homedir);
		
		EditText txtPort = (EditText) this.findViewById(R.id.txtPort);
		txtPort.setText(portnumber);
		
	}
	
	protected String wifiIpAddress() {
	    WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
	    int ipAddress = wifiManager.getConnectionInfo().getIpAddress();

	    // Convert little-endian to big-endianif needed
	    if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
	        ipAddress = Integer.reverseBytes(ipAddress);
	    }

	    byte[] ipByteArray = BigInteger.valueOf(ipAddress).toByteArray();

	    String ipAddressString;
	    try {
	        ipAddressString = InetAddress.getByAddress(ipByteArray).getHostAddress();
	    } catch (UnknownHostException ex) {
	        Log.e("WIFIIP", "Unable to get host address.");
	        ipAddressString = null;
	    }

	    return ipAddressString;
	}
	
	private boolean isMyServiceRunning() {
	    ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	        if (WirelessFtpService.class.getName().equals(service.service.getClassName())) {
	            return true;
	        }
	    }
	    return false;
	}	




}
