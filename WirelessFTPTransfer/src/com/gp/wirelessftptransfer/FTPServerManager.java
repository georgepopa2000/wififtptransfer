package com.gp.wirelessftptransfer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

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

import android.util.Log;

public class FTPServerManager {
	public FtpServerDetails configureServer(){
		Properties prop = new Properties();
		try {
			prop.load(new FileInputStream(new File("/mnt/sdcard/WirelessFTPTransfer/config.properties")));
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		String username = prop.getProperty("username", "guest");
		String password = prop.getProperty("password", "guest");
		String homedir  = prop.getProperty("homedir", "/");
		int portnumber = 2121;		
		
		
		
		try {
			portnumber = Integer.parseInt(prop.getProperty("portnumber", "2121"));
		} catch (NumberFormatException e1) {
			e1.printStackTrace();
		}
		Log.i("ftptrans", "username="+username);
		Log.i("ftptrans", "password="+password);
		Log.i("ftptrans", "homedir="+homedir);
		Log.i("ftptrans", "portnumber="+portnumber);
		
        UserFactory userFact = new UserFactory();
        userFact.setName(username);
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
        
        FtpServerDetails fsd = new FtpServerDetails(username, password, homedir, portnumber+"", serverFactory.createServer());
		return fsd;
	}

	
	
	
	
}
