package it.polimi.tiw.project.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Arrays;

@WebServlet("/HomeServlet")
public class HomeServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

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
        out.println("<option>Pop</option><option>Rock</option><option>Jazz</option><option>Rap</option><option>Instrumntal</option><option>Techno</option><option>Metal</option><option>Country</option><option>Punk</option><option>Raggaeton</option><option>Indie</option><option>Hip hop</option><option>Blues</option><option>Other</option>");
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