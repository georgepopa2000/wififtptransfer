package com.gp.wirelessftptransfer;

import java.util.ArrayList;
import java.util.List;

import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.UserFactory;
import org.apache.ftpserver.usermanager.impl.ConcurrentLoginPermission;
import org.apache.ftpserver.usermanager.impl.WritePermission;
import org.apache.ftpserver.util.EncryptUtils;

import android.content.SharedPreferences;

public class FTPServerManager {
	
	SharedPreferences prefs = null;
	
	
	
	public FTPServerManager(SharedPreferences prefs) {
		super();
		this.prefs = prefs;
	}



	public FtpServerDetails configureServer(){
		
		
		String username = prefs.getString(MainActivity.PREFS_USERNAME, "guest");
		String password = prefs.getString(MainActivity.PREFS_PASSWORD, "guest");
		String homedir  = prefs.getString(MainActivity.PREFS_HOMEDIR, "/");
		boolean allowany = prefs.getBoolean(MainActivity.PREFS_ALLOW_ANY, true);
		int portnumber = prefs.getInt(MainActivity.PREFS_PORT, 2121);
		
		
		
		
        UserFactory userFact = new UserFactory();
        userFact.setName(username);
        if (allowany) userFact.setName("allowanyusername");//hack to search for any user
        userFact.setPassword(EncryptUtils.encryptMD5(password));
        userFact.setHomeDirectory(homedir);
        List<Authority> la = new ArrayList<Authority>();
        la.add(new WritePermission());
        la.add(new ConcurrentLoginPermission(0, 0));
        userFact.setAuthorities(la);
        User user = userFact.createUser();	
        
        
        OTFUserManagerFactory userManagerFactory = new OTFUserManagerFactory();
        UserManager um = userManagerFactory.createUserManager();     
        try {
			um.save(user);
		} catch (FtpException e) {
			e.printStackTrace();
		}        
        
        
    	FtpServerFactory serverFactory = new FtpServerFactory();
    	ListenerFactory factory = new ListenerFactory();
    	// set the port of the listener
    	factory.setPort(portnumber);    	
    	// replace the default listener
    	serverFactory.addListener("default", factory.createListener());
    	// start the server
        

        
        serverFactory.setUserManager(um);     
        
        FtpServerDetails fsd = new FtpServerDetails(username, password, homedir, portnumber+"", allowany,serverFactory.createServer());
		return fsd;
	}

	
	
	
	
}
