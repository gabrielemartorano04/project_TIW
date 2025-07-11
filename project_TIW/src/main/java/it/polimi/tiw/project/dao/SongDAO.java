package it.polimi.tiw.project.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import it.polimi.tiw.project.beans.Genres;
import it.polimi.tiw.project.beans.Song;

public class SongDAO {
	private final Connection con;

    public SongDAO(Connection con) {
        this.con = con;
    }
    
    //da modificare i nomi nelle query SQL, non so come sono salvati. 
    //Secondo me qua non andrà song, ma i parametri singoli 
    
    public void insertSong(String title, String mp3, Genres genre, String albumName, int albumYear, String albumImg, String albumArtist, String user) throws SQLException {
    	
    	if(songAlreadyExists(user, title)) {
    		throw new SQLException("ERROR: a song with this title already exists");
    	}
    	
    	String query="SELECT year, interpreter, album_cover_path " +
                "FROM Song WHERE album_title = ? AND user_id = ? LIMIT 1";
    	
    	try(PreparedStatement check= con.prepareStatement(query)){
    		check.setString(1, albumName);
    		check.setString(2, user);
    		try (ResultSet rs = check.executeQuery()){
    			while(rs.next()) {
    				int year = rs.getInt("year");
    				String artist = rs.getString("interpreter");
                    String img = rs.getString("album_cover_path");
                    		//il controllo su album img è da controllare 
                    if (year != albumYear || !artist.equals(albumArtist) || !albumImg.equals(img)) {
                    	throw new SQLException("Error: album già esistente con attributi differenti");
                    }
    			}
    		}
    		
    	}
    	
    	query = "INSERT INTO Song (title, album_title, album_cover_path, interpreter, year, genre, file_path, user_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    	
    	try(PreparedStatement stmt = con.prepareStatement(query)){
    		stmt.setString(1, title);
            stmt.setString(2, albumName);
            stmt.setString(3, albumImg);
            stmt.setString(4, albumArtist);
            stmt.setInt(5, albumYear);
            stmt.setString(6, genre.toString());
            stmt.setString(7, mp3);
            stmt.setString(8, user);
            
            stmt.executeUpdate();
    	}
    }
    
    public Song findSong(String title) throws SQLException{
    	
    	Song song= null;
    	
    	String query = "SELECT * FROM Song WHERE title = ?";
    	
    	try(PreparedStatement stmt = con.prepareStatement(query)){
    		stmt.setString(1, title);
    		try (ResultSet rs = stmt.executeQuery()){
    			if(rs.next()) {
    				song=new Song();
                    song.setTitle(rs.getString("title"));
                    song.setAlbumName(rs.getString("album_title"));
                    song.setAlbumImg(rs.getString("album_cover_path"));
                    song.setAlbumArtist(rs.getString("interpreter"));
                    song.setAlbumYear(rs.getInt("year"));
                    song.setGenre(Genres.valueOf(rs.getString("genre")));
                    song.setMp3(rs.getString("file_path"));
    			}
    		}
    	}
    	return song;
    }
    
    public boolean songAlreadyExists(String username, String songTitle) throws SQLException {
        String query = "SELECT 1 FROM UserSongs WHERE username = ? AND song = ?";
        try (PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, songTitle);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }
    
    public ArrayList<Song> findSongByUser(String username) throws SQLException{
    	
    	ArrayList<Song> songs = new ArrayList<>();
    	
        String query = "SELECT s.* FROM Song s JOIN UserSongs us ON s.title = us.song WHERE us.username = ? ORDER BY s.album_artist ASC, s.album_year ASC";

        try (PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Song s = new Song();
                    s.setTitle(rs.getString("title"));
                    s.setAlbumName(rs.getString("album_title"));
                    s.setAlbumImg(rs.getString("album_cover_path"));
                    s.setAlbumArtist(rs.getString("interpreter"));
                    s.setAlbumYear(rs.getInt("year"));
                    s.setGenre(Genres.valueOf(rs.getString("genre")));
                    s.setMp3(rs.getString("file_path"));
                    songs.add(s);
                }
            }
        }
        return songs;
    }
    
}
