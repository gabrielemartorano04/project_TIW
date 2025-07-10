package it.polimi.tiw.project.beans;

import java.sql.Date;

public class Playlist {
	private String title;
	private Date date;
	private String user;
	
	//setter 
	
	public void setTitle(String title) {
		this.title=title;
	}
	
	public void setDate(Date date) {
		this.date=date;
	}
	
	public void setUser(String user) {
		this.user=user;
	}
	
	//getters
	
	public String getTitle() {
		return title;
	}
	
	public Date getDate() {
		return date;
	}
	
	public String getUser() {
		return user;
	}
	
}
