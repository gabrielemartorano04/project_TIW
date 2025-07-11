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
    				int year = rs.getInt("album_year");
    				String artist = rs.getString("album_artist");
                    String img = rs.getString("album_image");
                    		//il controllo su album img è da controllare 
                    if (year != albumYear || !artist.equals(albumArtist) || !albumImg.equals(img)) {
                    	throw new SQLException("Error: album già esistente con attributi differenti");
                    }
    			}
    		}
    		
    	}
    	
    	query = "INSERT INTO Song (title, genre, mp3, album_title, album_artist, album_year, album_image, user) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    	
    	try(PreparedStatement stmt = con.prepareStatement(query)){
    		stmt.setString(1, title);
            stmt.setString(4, albumName);
            stmt.setString(7, albumImg);
            stmt.setString(5, albumArtist);
            stmt.setInt(6, albumYear);
            stmt.setString(2, genre.toString());
            stmt.setString(3, mp3);
            stmt.setString(8, user);
            
            stmt.executeUpdate();
    	}
    }
    
    public Song findSong(String title, String user) throws SQLException{
    	
    	Song song= null;
    	
    	String query = "SELECT * FROM Song WHERE title = ? AND user = ?";
    	
    	try(PreparedStatement stmt = con.prepareStatement(query)){
    		stmt.setString(1, title);
    		stmt.setString(2, user);
    		try (ResultSet rs = stmt.executeQuery()){
    			if(rs.next()) {
    				song=new Song();
                    song.setTitle(rs.getString("title"));
                    song.setAlbumName(rs.getString("album_title"));
                    song.setAlbumImg(rs.getString("album_image"));
                    song.setAlbumArtist(rs.getString("album_artist"));
                    song.setAlbumYear(rs.getInt("album_year"));
                    song.setGenre(Genres.valueOf(rs.getString("genre")));
                    song.setMp3(rs.getString("mp3"));
    			}
    		}
    	}
    	return song;
    }
    
    public boolean songAlreadyExists(String username, String songTitle) throws SQLException {
        String query = "SELECT 1 FROM Songs WHERE user = ? AND title = ?";
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
    	
        String query = "SELECT * FROM Song WHERE user = ? ORDER BY s.album_artist ASC, s.album_year ASC";

        try (PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Song s = new Song();
                    s.setTitle(rs.getString("title"));
                    s.setAlbumName(rs.getString("album_title"));
                    s.setAlbumImg(rs.getString("album_image"));
                    s.setAlbumArtist(rs.getString("album_artist"));
                    s.setAlbumYear(rs.getInt("album_year"));
                    s.setGenre(Genres.valueOf(rs.getString("genre")));
                    s.setMp3(rs.getString("mp3"));
                    songs.add(s);
                }
            }
        }
        return songs;
    }
    
}
