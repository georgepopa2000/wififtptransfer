package com.gp.wirelessftptransfer;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteOrder;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
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
	
	public static final String DEFAULT_HOME_DIR = Environment.getExternalStorageDirectory().getAbsolutePath();
	
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
		

	}
	
	@Override
	protected void onResume() {
		super.onResume();
		populateFields();
		TextView textServ = (TextView) this.findViewById(R.id.textServerOnOff);		
				
		Button buton = (Button) this.findViewById(R.id.butonoff);
		if (this.getIntent().getBooleanExtra(WirelessFtpService.KEY_INTENT_STARTED,false)){
			buton.setText(this.getResources().getString(R.string.text_but_off));
			textServ.setText("FTP Server is running");
		} if (isMyServiceRunning()){
			buton.setText(this.getResources().getString(R.string.text_but_off));
			textServ.setText("FTP Server is running");
		} else {
			textServ.setText("FTP Server is stopped");			
			buton.setText(this.getResources().getString(R.string.text_but_on));
		}
	}
	
	private void showSettings(){
		Intent i = new Intent(MainActivity.this, SettingsActivity.class);
		startActivityForResult(i, REQUEST_CODE_SETTINGS);
	}
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_CODE_SETTINGS){//return from settings activity
			populateFields(); //show new preferences
			if (isMyServiceRunning()){
				startFTPService(); //if the server service is started restart the service
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
		TextView textv = (TextView) this.findViewById(R.id.textServerOnOff);
		if (buton.getText().equals(this.getResources().getString(R.string.text_but_on))){
			if (wifiIpAddress()==null){
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage("Device not connected to a Wireless Network")
				.setNeutralButton("OK", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				})
				.setCancelable(true);
				AlertDialog ad = builder.create();
				ad.show();
				return;
			}
			buton.setText(this.getResources().getString(R.string.text_but_off));
			startFTPService();
			textv.setText("FTP Server is running");

		} else {
			buton.setText(this.getResources().getString(R.string.text_but_on));
			stopFTPService();
			textv.setText("FTP Server is stopped");			

		}		
	}
	
	
	private void stopFTPService(){
		Intent tint = new Intent(this,WirelessFtpService.class);
		stopService(tint);		
	}
	
	private void startFTPService(){
		
		if (isMyServiceRunning()){
			stopFTPService();
		}
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
		
		String wifihost = wifiIpAddress();;
		if (wifihost==null) wifihost="WIFI Network not connected";
		else wifihost = wifihost+":"+Integer.toString(prefs.getInt(PREFS_PORT, 2121)); 
		
		textv.setText("host: "+wifihost +"\n" +
				credentialsText+"\n"+
				"Home dir:"+prefs.getString(PREFS_HOMEDIR, MainActivity.DEFAULT_HOME_DIR));		
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
