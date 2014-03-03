package com.gp.wirelessftptransfer;

import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.usermanager.UserManagerFactory;

public class OTFUserManagerFactory implements UserManagerFactory{

	@Override
	public UserManager createUserManager() {
		return new OTFUserManager();		
	}

}
