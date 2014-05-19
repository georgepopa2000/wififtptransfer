package com.gp.wirelessftptransfer;

import net.rdrei.android.dirchooser.DirectoryChooserActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity {
	
	public final int REQUEST_DIRECTORY = 1234;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		SharedPreferences prefs = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE);
		final Editor prefeditor = prefs.edit();
				
		CheckBoxPreference chkAnonymous = (CheckBoxPreference) findPreference("anonymouslogin");
		boolean checked = prefs.getBoolean(MainActivity.PREFS_ALLOW_ANY, true);
		chkAnonymous.setChecked(checked);		
		
		chkAnonymous.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				boolean bl = ((Boolean) newValue).booleanValue();
				prefeditor.putBoolean(MainActivity.PREFS_ALLOW_ANY, bl);
				prefeditor.apply();
				return true;
			}
		});
		
		
		EditTextPreference txtUsername = (EditTextPreference) findPreference("username");
		String username = prefs.getString(MainActivity.PREFS_USERNAME, "guest");
		txtUsername.setText(username);
		txtUsername.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {				
				prefeditor.putString(MainActivity.PREFS_USERNAME, (String) newValue);
				prefeditor.apply();
				return true;
			}
		});		
		
		EditTextPreference txtPassword = (EditTextPreference) findPreference("password");
		String password = prefs.getString(MainActivity.PREFS_PASSWORD, "guest");
		txtPassword.setText(password);
		txtPassword.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {				
				prefeditor.putString(MainActivity.PREFS_PASSWORD, (String) newValue);
				prefeditor.apply();
				return true;
			}
		});		
		
		EditTextPreference txtPort = (EditTextPreference) findPreference("port");
		int port = prefs.getInt(MainActivity.PREFS_PORT, 2121);
		txtPort.setText(""+port);	
		txtPort.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				prefeditor.putInt(MainActivity.PREFS_PORT, Integer.parseInt((String)newValue));
				prefeditor.apply();
				return true;
			}
		});					
		
		
		final String homedir = prefs.getString(MainActivity.PREFS_HOMEDIR, "/");
		
		
		Preference txtHomedir = findPreference("homedir");
		txtHomedir.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				final Intent chooserIntent = new Intent(SettingsActivity.this, DirectoryChooserActivity.class);
				chooserIntent.putExtra(DirectoryChooserActivity.EXTRA_INITIAL_DIRECTORY, homedir);

				// REQUEST_DIRECTORY is a constant integer to identify the request, e.g. 0
				startActivityForResult(chooserIntent, REQUEST_DIRECTORY);				
				return true;
			}
		});

	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		SharedPreferences prefs = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE);
		final Editor prefeditor = prefs.edit();		
		if (requestCode == REQUEST_DIRECTORY){
			if (resultCode ==DirectoryChooserActivity.RESULT_CODE_DIR_SELECTED){				
				prefeditor.putString(MainActivity.PREFS_HOMEDIR, data.getStringExtra(DirectoryChooserActivity.RESULT_SELECTED_DIR));
				prefeditor.apply();				
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
}
