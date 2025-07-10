package it.polimi.tiw.project.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;

import it.polimi.tiw.project.beans.Playlist;
import it.polimi.tiw.project.beans.Song;

public class PlaylistDao {
	
	private final Connection con;

    public PlaylistDao(Connection con) {
        this.con = con;
    }

	public int createPlaylist(String title, int userId, Date creationDate) throws SQLException{
	
		
	};
	
	public void addTracksToPlaylist(int playlistId, ArrayList<Integer> trackIds) throws SQLException{
		
	};
	
	public ArrayList<Playlist> findPlaylistsByUser(int userId) throws SQLException{
		
	};
	
	public Playlist findPlaylistById(int id) throws SQLException{
		
	};
	
	public ArrayList<Song> findTracksInPlaylist(int playlistId) throws SQLException{
		
	};
	
	public boolean isTrackInPlaylist(int playlistId, int trackId) throws SQLException{
		
	};
	
}
