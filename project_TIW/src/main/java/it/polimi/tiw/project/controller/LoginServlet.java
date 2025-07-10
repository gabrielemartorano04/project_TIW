package it.polimi.tiw.project.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.List;

@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        // Controllo base
        if (username == null || password == null || username.isBlank() || password.isBlank()) {
            response.sendRedirect("login.html?error=1");
            return;
        }

        boolean loginSuccess = false;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/playlistdb?serverTimezone=UTC",
                    "root",  // ← cambia con il tuo utente
                    "1a2b3c4d" // ← cambia con la tua password
            );

            PreparedStatement stmt = con.prepareStatement("SELECT * FROM User WHERE username = ? AND password = ?");
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                loginSuccess = true;
            }

            con.close();

        } catch (Exception e) {
            e.printStackTrace(); // log interno
            response.sendRedirect("login.html?error=db");
            return;
        }

        // ✅ Login hardcoded fittizio (ancora valido)
        if (username.equals("admin") && password.equals("1234")) {
            loginSuccess = true;
        }

        if (loginSuccess) {
            HttpSession session = request.getSession();
            session.setAttribute("username", username);
            response.sendRedirect("HomeServlet");
        } else {
            response.sendRedirect("login.html?error=1");
        }
    }
	
	@Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("username") == null) {
            response.sendRedirect("login.html");
            return;
        }

        String username = (String) session.getAttribute("username");

        // MOCK: playlist utente fittizie (da sostituire con DAO più avanti)
        List<String> playlists = Arrays.asList("Allenamento", "Relax", "Auto");

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        out.println("<!DOCTYPE html>");
        out.println("<html><head><title>Home</title></head><body>");

        out.println("<h1>Benvenuto, " + username + "</h1>");

        out.println("<h2>Le tue Playlist</h2>");
        for (String playlist : playlists) {
            out.println("<p><a href='PlaylistServlet?name=" + playlist + "'>" + playlist + "</a></p>");
        }

        out.println("<h2>Carica un nuovo brano</h2>");
        out.println("<form action='UploadTrackServlet' method='post' enctype='multipart/form-data'>");
        out.println("Titolo: <input type='text' name='titolo'><br>");
        out.println("Album: <input type='text' name='album'><br>");
        out.println("Interprete: <input type='text' name='interprete'><br>");
        out.println("Anno: <input type='number' name='anno'><br>");
        out.println("Genere: <select name='genere'>");
        out.println("<option>Pop</option><option>Rock</option><option>Jazz</option>");
        out.println("</select><br>");
        out.println("Copertina: <input type='file' name='cover'><br>");
        out.println("File MP3: <input type='file' name='audio'><br>");
        out.println("<input type='submit' value='Carica brano'>");
        out.println("</form>");

        out.println("<h2>Crea una nuova Playlist</h2>");
        out.println("<form action='CreatePlaylistServlet' method='post'>");
        out.println("Titolo: <input type='text' name='titolo'><br>");
        out.println("<!-- Qui metteremo checkbox per i brani -->");
        out.println("<input type='submit' value='Crea Playlist'>");
        out.println("</form>");

        out.println("</body></html>");
    }
}