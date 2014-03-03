package com.gp.wirelessftptransfer;

import org.apache.ftpserver.FtpServer;

public class FtpServerDetails {
	String username;
	String password;
	String homedir;
	String portnumber;
	FtpServer server;
	
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getHomedir() {
		return homedir;
	}
	public void setHomedir(String homedir) {
		this.homedir = homedir;
	}
	public String getPortnumber() {
		return portnumber;
	}
	public void setPortnumber(String portnumber) {
		this.portnumber = portnumber;
	}
	public FtpServer getServer() {
		return server;
	}
	public void setServer(FtpServer server) {
		this.server = server;
	}
	public FtpServerDetails(String username, String password, String homedir,
			String portnumber, FtpServer server) {
		super();
		this.username = username;
		this.password = password;
		this.homedir = homedir;
		this.portnumber = portnumber;
		this.server = server;
	}
	
	
	
	
}
