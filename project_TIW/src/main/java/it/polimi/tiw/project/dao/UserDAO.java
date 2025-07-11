package it.polimi.tiw.project.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {
	private final Connection con;

    public UserDAO(Connection con) {
        this.con = con;
    }

    public boolean checkCredentials(String username, String password) throws SQLException {
        String query = "SELECT * FROM User WHERE username = ? AND password = ?";
        try (PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean isUsernameTaken(String username) throws SQLException {
        String query = "SELECT * FROM `User` WHERE username = ?";
        try (PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void registerUser(String username, String password, String name, String surname) throws SQLException {
        
        String query = "INSERT INTO User (username, password, name, surname) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, name);
            stmt.setString(4, surname);
            stmt.executeUpdate();
        }
    }
}
