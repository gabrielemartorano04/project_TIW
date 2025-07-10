package it.polimi.tiw.project.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import it.polimi.tiw.project.beans.Genres;
import it.polimi.tiw.project.beans.Song;

public class SongDAO {
	private final Connection con;

    public SongDAO(Connection con) {
        this.con = con;
    }
    
    //da modificare i nomi nelle query SQL, non so come sono salvati. 
    //Secondo me qua non andrà song, ma i parametri singoli 
    
    public void insertSong(Song song) throws SQLException {
    	
    	String query="SELECT year, interpreter, album_cover_path " +
                "FROM Song WHERE album_title = ? LIMIT 1";
    	
    	try(PreparedStatement check= con.prepareStatement(query)){
    		check.setString(1, song.getAlbumName());
    		try (ResultSet rs = check.executeQuery()){
    			if(rs.next()) {
    				int year = rs.getInt("year");
    				String artist = rs.getString("interpreter");
                    String albumImg = rs.getString("album_cover_path");
                    		//il controllo su album img è da controllare 
                    if (year != song.getAlbumYear() || !artist.equals(song.getAlbumArtist()) || !albumImg.equals(song.getAlbumImg())) {
                    	throw new SQLException("Error: album già esistente con attributi differenti");
                    }
    			}
    		}
    		
    	}
    	
    	query = "INSERT INTO Song (title, album_title, album_cover_path, interpreter, year, genre, file_path, user_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    	
    	try(PreparedStatement stmt = con.prepareStatement(query)){
    		stmt.setString(1, song.getTitle());
            stmt.setString(2, song.getAlbumName());
            stmt.setString(3, song.getAlbumImg());
            stmt.setString(4, song.getAlbumArtist());
            stmt.setInt(5, song.getAlbumYear());
            stmt.setString(6, song.getGenre().toString());
            stmt.setString(7, song.getMp3());
            
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
    
    
    
}
