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
import android.view.Menu;
import android.view.MenuItem;
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
	
	public static final int  REQUEST_CODE_SETTINGS = 1977;


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
		
		Button butSettings = (Button) this.findViewById(R.id.butSettings);
		butSettings.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showSettings();
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
		populateFields();
		TextView textServ = (TextView) this.findViewById(R.id.textServerOnOff);		
				
		Button buton = (Button) this.findViewById(R.id.butonoff);
		if (this.getIntent().getBooleanExtra(WirelessFtpService.KEY_INTENT_STARTED,false)){
			buton.setText(this.getResources().getString(R.string.text_but_off));
			textServ.setText("server online");
		} if (isMyServiceRunning()){
			buton.setText(this.getResources().getString(R.string.text_but_off));
			textServ.setText("server online");
			
			
	
		} else {
			textServ.setText("server stopped");			
			buton.setText(this.getResources().getString(R.string.text_but_on));
		}
	}
	
	private void showSettings(){
		Intent i = new Intent(MainActivity.this, SettingsActivity.class);
		//startActivity(i);
		startActivityForResult(i, REQUEST_CODE_SETTINGS);
	}
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_CODE_SETTINGS){
			populateFields();
			if (isMyServiceRunning()){
				startFTPService();
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		if (item.getItemId() == R.id.action_settings){
			showSettings();
		}
		return super.onMenuItemSelected(featureId, item);
	}
	
	
	

	private void ftpStartStop() throws Exception{
		Button buton = (Button) this.findViewById(R.id.butonoff);
		TextView textv = (TextView) this.findViewById(R.id.textarea_log);
		if (buton.getText().equals(this.getResources().getString(R.string.text_but_on))){
			buton.setText(this.getResources().getString(R.string.text_but_off));
			startFTPService();

		} else {
			buton.setText(this.getResources().getString(R.string.text_but_on));
			Intent tint = new Intent(this,WirelessFtpService.class);
			stopService(tint);

			textv.setText("server stopped");			

		}		
	}
	
	private void startFTPService(){
		Intent tint = new Intent(this,WirelessFtpService.class);
		startService(tint);		
	}
	
	private void populateFields() {
		String wifiaddress = wifiIpAddress();
		if (wifiaddress==null) wifiaddress="WIFI Network not connected"; 
		EditText txtServerAddress = (EditText) this.findViewById(R.id.txtWIFIAddress);
		txtServerAddress.setText(wifiIpAddress());
		
		TextView textv = (TextView) this.findViewById(R.id.textarea_log);

		SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
		String credentialsText = "";
		if (prefs.getBoolean(PREFS_ALLOW_ANY, true)){			
			credentialsText = "Use any credentials for login";
		} else {
			credentialsText ="user:"+prefs.getString(PREFS_USERNAME, "guest")+" \n" +
					"pass:"+prefs.getString(PREFS_PASSWORD, "guest");
		}
		textv.setText("host: " +wifiIpAddress()+":"+Integer.toString(prefs.getInt(PREFS_PORT, 2121))+"\n" +
				credentialsText);		
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
