package it.polimi.tiw.project.controller;

import it.polimi.tiw.project.dao.SongDAO;
import it.polimi.tiw.project.beans.Genres;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@WebServlet("/upload")
@MultipartConfig
public class UploadServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Connection connection;

    public void init() throws ServletException {
        try {
            ServletContext context = getServletContext();
            String driver = context.getInitParameter("dbDriver");
            String url = context.getInitParameter("dbUrl");
            String user = context.getInitParameter("dbUser");
            String password = context.getInitParameter("dbPassword");
            Class.forName(driver);
            connection = DriverManager.getConnection(url, user, password);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            response.sendRedirect("login");
            return;
        }

        String user = (String) session.getAttribute("currentUser");
        String titolo = request.getParameter("titolo");
        String album = request.getParameter("album");
        String interprete = request.getParameter("interprete");
        String annoStr = request.getParameter("anno");
        String genereStr = request.getParameter("genere");

        Part audioPart = request.getPart("audio");
        Part coverPart = request.getPart("cover");

        if (titolo == null || titolo.isBlank() || album == null || interprete == null || annoStr == null || genereStr == null
                || audioPart == null || coverPart == null) {
            session.setAttribute("uploadError", "Dati mancanti o incompleti");
            response.sendRedirect("home");
            return;
        }

        try {
            int anno = Integer.parseInt(annoStr);
            Genres genere = Genres.valueOf(genereStr.toUpperCase().replace(" ", "_"));

            // Percorsi locali (puoi cambiarli se vuoi)
            String uploadDir = getServletContext().getRealPath("/") + "uploads/";
            new File(uploadDir).mkdirs(); // crea se non esiste

            String audioPath = uploadDir + System.currentTimeMillis() + "_" + audioPart.getSubmittedFileName();
            String coverPath = uploadDir + System.currentTimeMillis() + "_" + coverPart.getSubmittedFileName();

            Files.copy(audioPart.getInputStream(), new File(audioPath).toPath());
            Files.copy(coverPart.getInputStream(), new File(coverPath).toPath());

            // Percorsi relativi (per salvarli nel DB)
            String audioDbPath = "uploads/" + new File(audioPath).getName();
            String coverDbPath = "uploads/" + new File(coverPath).getName();

            SongDAO dao = new SongDAO(connection);
            dao.insertSong(titolo, audioDbPath, genere, album, anno, coverDbPath, interprete, user);

            session.setAttribute("uploadSuccess", true);
        } catch (Exception e) {
            session.setAttribute("uploadError", "Errore durante il caricamento del brano: " + e.getMessage());
        }

        response.sendRedirect("home");
    }

    public void destroy() {
        try {
            if (connection != null) connection.close();
        } catch (SQLException ignored) {}
    }
}