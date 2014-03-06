package com.gp.wirelessftptransfer;

import java.io.File;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.util.Properties;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity {
	FtpServerDetails fsd;
	PowerManager.WakeLock wl;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		//if (getCurrentFocus()!=null) getCurrentFocus().clearFocus();

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
		
		EditText txtAddress = (EditText) this.findViewById(R.id.txtWIFIAddress);
		txtAddress.setInputType(InputType.TYPE_NULL);
		
		File configFile = new File("/mnt/sdcard/WirelessFTPTransfer/config.properties");
		if (!configFile.exists()){
			File configFolder = new File("/mnt/sdcard/WirelessFTPTransfer/");
			configFolder.mkdirs();
			PrintWriter writer;
			try {
				writer = new PrintWriter("/mnt/sdcard/WirelessFTPTransfer/config.properties", "UTF-8");
				writer.println();
				writer.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		
		try {
			populateFields();
		} catch (Exception e) {
			e.printStackTrace();
		}
		

		PowerManager pm = (PowerManager)getSystemService(
                Context.POWER_SERVICE);
		wl = pm.newWakeLock(
            PowerManager.SCREEN_DIM_WAKE_LOCK
            | PowerManager.ON_AFTER_RELEASE,
            "frptrans");



		TextView textv = (TextView) this.findViewById(R.id.textarea_log);
		textv.setText("server stopped");		
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	private void ftpStartStop() throws Exception{
		Button buton = (Button) this.findViewById(R.id.butonoff);
		TextView textv = (TextView) this.findViewById(R.id.textarea_log);
		if (buton.getText().equals(this.getResources().getString(R.string.text_but_on))){
			buton.setText(this.getResources().getString(R.string.text_but_off));
			FTPServerManager fsm = new FTPServerManager();
			fsd = fsm.configureServer();			
			fsd.getServer().start();
			wl.acquire();
			
			textv.setText("server online \n" +
							"host: " +wifiIpAddress()+":"+fsd.getPortnumber()+"\n" +
							"user:"+fsd.getUsername()+" \n" +
							"pass:"+fsd.getPassword());
		} else {
			buton.setText(this.getResources().getString(R.string.text_but_on));
			fsd.getServer().stop();
			wl.release();
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
		
		
	
		
		
		EditText txtUsername = (EditText) this.findViewById(R.id.txtUsername);
		prop.setProperty("username", txtUsername.getText().toString());
		
		EditText txtPassword = (EditText) this.findViewById(R.id.txtPassword);
		prop.setProperty("password", txtPassword.getText().toString());
		
		EditText txtHomedir = (EditText) this.findViewById(R.id.txtHomedir);
		prop.setProperty("homedir", txtHomedir.getText().toString());
		
		EditText txtPort = (EditText) this.findViewById(R.id.txtPort);
		prop.setProperty("portnumber", txtPort.getText().toString());
		
		try {
			prop.store(new FileOutputStream(new File("/mnt/sdcard/WirelessFTPTransfer/config.properties")), "Config file for WirelessFTPTransfer app");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private void populateFields() throws FileNotFoundException, IOException{
		EditText txtServerAddress = (EditText) this.findViewById(R.id.txtWIFIAddress);
		txtServerAddress.setText(wifiIpAddress());
		Properties prop = new Properties();
		prop.load(new FileInputStream(new File("/mnt/sdcard/WirelessFTPTransfer/config.properties")));
		
		String username = prop.getProperty("username", "guest");
		String password = prop.getProperty("password", "guest");
		String homedir  = prop.getProperty("homedir", "/");
		String portnumber = prop.getProperty("portnumber", "2121");
		
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

}
