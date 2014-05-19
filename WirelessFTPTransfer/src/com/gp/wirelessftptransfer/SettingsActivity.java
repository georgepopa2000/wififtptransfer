package com.gp.wirelessftptransfer;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity {

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
		
		EditTextPreference txtHomedir = (EditTextPreference) findPreference("homedir");
		String homedir = prefs.getString(MainActivity.PREFS_HOMEDIR, "/");
		txtHomedir.setText(homedir);	
		txtHomedir.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {				
				prefeditor.putString(MainActivity.PREFS_HOMEDIR, (String) newValue);
				prefeditor.apply();
				return true;
			}
		});				

	}
	
}
