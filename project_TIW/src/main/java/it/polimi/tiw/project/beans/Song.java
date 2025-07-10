package it.polimi.tiw.project.beans;

public class Song {
	
	private String title; 
	private String mp3;
	private Genres genre;
	private String albumName;
	private int albumYear;
	private String albumImg;
	private String albumArtist;
	
	//setters 
	
	public void setTitle(String title) {
		this.title=title;
	}
	
	public void setMp3(String mp3) {
		this.mp3=mp3;
	}
	
	public void setGenere(Genres genre) {
		this.genre=genre;
	}
	
	public void setAlbumName(String albumName){
		this.albumName=albumName;
	}
	
	public void setAlbumImg(String albumImg) {
		this.albumImg=albumImg;
	}
	
	public void setAlbumArtist(String artist) {
		this.albumArtist=artist;
	}
	
	public void setAlbumYear(int year) {
		this.albumYear=year;
	}
	
	//getters
	
	public String getTitle() {
		return title;
	}
	
	public String getMp3() {
		return mp3;
	}
	
	public Genres getGenere() {
		return genre;
	}
	
	public String getAlbumName() {
		return albumName;
	}
	
	public String getAlbumImg() {
		return albumImg;
	}
	
	public String getAlbumArtist() {
		return albumArtist;
	}
	
	public int getAlbumYear() {
		return albumYear;
	}
	
}
