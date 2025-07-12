package it.polimi.tiw.project.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;

import it.polimi.tiw.project.beans.Genres;
import it.polimi.tiw.project.beans.Playlist;
import it.polimi.tiw.project.beans.Song;

public class PlaylistDAO {
	
	private final Connection con;

    public PlaylistDAO(Connection con) {
        this.con = con;
    }

	public void createPlaylist(String title, String playlistTitle, LocalDate today) throws SQLException{
		String query="SELECT user FROM `Playlist` WHERE title=?";
		
		try(PreparedStatement check = con.prepareStatement(query)){
			check.setString(1, title);
			try(ResultSet rs= check.executeQuery()){
				while(rs.next()){
					String existingUser = rs.getString("user");
					if(today.equals(existingUser)) {
						throw new SQLException("ERROR: Playlist title already used");
					}
				}
			}
		}
		
		
		query = "INSERT INTO Playlist(title, date, user, song) VALUES (?, ?, ?, ?)";
		try(PreparedStatement stmt = con.prepareStatement(query)){
			stmt.setString(1, title);
			stmt.setString(2, playlistTitle);
			stmt.setDate(3, java.sql.Date.valueOf(today));
		}
		
	};
	
	public void addTracksToPlaylist(String title, ArrayList<String> songs) throws SQLException{
		for(String song : songs) {
			if(isTrackInPlaylist(title, song)) {
				throw new SQLException("ERROR: one of the songs is already in this playlist");
			}
		}
		String query = "INSERT IGNORE INTO PlaylistSongs (playlist, song) VALUES (?, ?)";
        try (PreparedStatement stmt = con.prepareStatement(query)) {
            for (String song : songs) {
                stmt.setString(1, title);
                stmt.setString(2, song);
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
	};

	public ArrayList<Playlist> findPlaylistsByUser(String user) throws SQLException{
		ArrayList<Playlist> playlists = new ArrayList<>();
	    String query = "SELECT title, date FROM Playlist WHERE user = ? ORDER BY date DESC";
	    try (PreparedStatement stmt = con.prepareStatement(query)) {
	        stmt.setString(1, user);
	        try (ResultSet rs = stmt.executeQuery()) {
	            while (rs.next()) {
	                Playlist p = new Playlist();
	                p.setTitle(rs.getString("title"));
	                p.setDate(rs.getDate("date"));
	                p.setUser(user);
	                playlists.add(p);
	            }
	        }
	    }
	    return playlists;
	};
	
	public Playlist findPlaylistByTitle(String title) throws SQLException{
		String query = "SELECT title, date, user FROM Playlist WHERE id = ?";
        try (PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setString(1, title);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Playlist p = new Playlist();
                    p.setTitle(rs.getString("title"));
                    p.setDate(rs.getDate("creation_date"));
                    p.setUser(rs.getString("user_id"));
                    return p;
                }
            }
        }
        return null;
	};
	
	public ArrayList<Song> findTracksInPlaylist(String title) throws SQLException{
		ArrayList<Song> songs = new ArrayList<>();
        String query = "SELECT s.* FROM Song s JOIN PlaylistSongs ps ON s.title = ps.song WHERE ps.playlist = ? " +
                       "ORDER BY s.album_artist ASC, s.album_year ASC";
        try (PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setString(1, title);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Song s = new Song();
                    s.setTitle(rs.getString("title"));
                    s.setGenre(Genres.valueOf(rs.getString("genre")));
                    s.setMp3(rs.getString("mp3"));
                    s.setAlbumName(rs.getString("album_title"));
                    s.setAlbumImg(rs.getString("album_image"));
                    s.setAlbumArtist(rs.getString("album_artist"));
                    s.setAlbumYear(rs.getInt("album_year"));
                    songs.add(s);
                }
            }
        }
        return songs;

	};
	
	public boolean isTrackInPlaylist(String title, String song) throws SQLException{
		String query = "SELECT 1 FROM PlaylistSongs WHERE playlist = ? AND song = ?";
        try (PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setString(1, title);
            stmt.setString(2, song);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
	};
	
}
