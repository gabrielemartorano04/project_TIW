package it.polimi.tiw.project.beans;

import java.util.ArrayList;

public class User {
	private String username;
	private String name;
	private String surname;
	private String password;
	private ArrayList<Song> userSongs = new ArrayList<>();
	
	//setter
	
	public void setUsername(String username) {
		this.username=username;
	}
	
	public void setName(String name) {
		this.name=name;
	}
	
	public void setSurname(String surname) {
		this.surname=surname;
	}
	
	public void setPassword(String password){
		this.password=password;
	}
	
	public void addSong(Song song) {
		userSongs.add(song);
	}
	
	//getters
	
	public String getUsername() {
		return username;
	}
	
	public String getName() {
		return name;
	}
	
	public String getSurname() {
		return surname;
	}
	
	public String getPassword(){
		return password;
	}
	
	public ArrayList<Song> getUserSongs(){
		return userSongs;
	}
	
}
