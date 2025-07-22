package it.polimi.tiw.project.beans;

import java.sql.Date;
import java.util.ArrayList;

public class Playlist {
    private String title;
    private Date date;
    private String user;
    private ArrayList<Song> songs = new ArrayList<>();

    // Setters
    public void setTitle(String title) {
        this.title = title;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void addSong(Song song) {
        songs.add(song);
    }

    // Getters
    public String getTitle() {
        return title;
    }

    public Date getDate() {
        return date;
    }

    public String getUser() {
        return user;
    }

    public ArrayList<Song> getSongs() {
        return songs;
    }
}