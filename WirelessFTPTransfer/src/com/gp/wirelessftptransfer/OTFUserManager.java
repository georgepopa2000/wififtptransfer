package com.gp.wirelessftptransfer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import org.apache.ftpserver.ftplet.Authentication;
import org.apache.ftpserver.ftplet.AuthenticationFailedException;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.usermanager.UsernamePasswordAuthentication;
import org.apache.ftpserver.util.EncryptUtils;

public class OTFUserManager implements UserManager{
	ArrayList<User> userList = new ArrayList<User>(); 
	@Override
	public User authenticate(Authentication authentication)
			throws AuthenticationFailedException {
		if (authentication instanceof UsernamePasswordAuthentication){
			UsernamePasswordAuthentication upauth = (UsernamePasswordAuthentication) authentication;
			String pass = upauth.getPassword();
			String username = upauth.getUsername();
			try {
				if (doesExist("allowanyusername")) {//hack to allow any login details
					User u = getUserByName("allowanyusername");
				    return u;
				}
			} catch (FtpException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
		    if (username == null) {
		        throw new AuthenticationFailedException("Authentication failed");
		    }

            if (pass == null) {
                pass = "";
            }
            String passencrypt = EncryptUtils.encryptMD5(pass);
			try {
				if (!doesExist(username)) {
					throw new AuthenticationFailedException("Authentication failed");
				}
				
				User u = getUserByName(username);
				if (u.getPassword().equals(passencrypt)){
					return u;
				} else {
					throw new AuthenticationFailedException("Authentication failed");
				}
			} catch (FtpException e) {
				return null;
			}
		} else {
            throw new IllegalArgumentException(
                    "Authentication not supported by this user manager");
        }
	}

	@Override
	public void delete(String username) throws FtpException {
		if (!doesExist(username)) return;
		Iterator<User> it = userList.iterator();
		while (it.hasNext()){
			User u = it.next();			
			if (u.getName().equals(username)) it.remove();
		}		
	}

	@Override
	public boolean doesExist(String username) throws FtpException {
		Iterator<User> it = userList.iterator();
		while (it.hasNext()){
			User u = it.next();
			if (u.getName().equals(username)) return true;
		}
		return false;
	}

	@Override
	public String getAdminName() throws FtpException {
		return "Admin";
	}

	@Override
	public String[] getAllUserNames() throws FtpException {
		ArrayList<String> als = new ArrayList<String>();
		Iterator<User> it = userList.iterator();
		while (it.hasNext()){
			User u = it.next();			
			als.add(u.getName());
		}
		Collections.sort(als);
		return als.toArray(new String[0]);
	}

	@Override
	public User getUserByName(String username) throws FtpException {
		if (!doesExist(username)) return null;
		Iterator<User> it = userList.iterator();
		while (it.hasNext()){
			User u = it.next();			
			if (u.getName().equals(username)) return u;
		}				
		return null;
	}

	@Override
	public boolean isAdmin(String username) throws FtpException {
		if (doesExist(username)) return true;
		return false;
	}

	@Override
	public void save(User user) throws FtpException {
		if (doesExist(user.getName())) throw new FtpException();
		userList.add(user);
	}

}
