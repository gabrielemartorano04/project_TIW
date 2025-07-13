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
            response.sendRedirect("createPlaylist");
            return;
        }

        try {
            int anno = Integer.parseInt(annoStr);
            Genres genere = Genres.valueOf(genereStr.toUpperCase().replace(" ", "_"));

            // Leggi i file in RAM
            byte[] audioBytes = audioPart.getInputStream().readAllBytes();
            byte[] coverBytes = coverPart.getInputStream().readAllBytes();

            // Recupera base path dal contesto
            String basePath = getServletContext().getInitParameter("basePath");
            String imgDir = basePath + "images/";
            String mp3Dir = basePath + "audio/";
            new File(imgDir).mkdirs();
            new File(mp3Dir).mkdirs();

            // Istanzia DAO
            SongDAO dao = new SongDAO(connection);

            // Esegui controlli in RAM: solo coverBytes viene confrontata
            dao.checkSongConsistency(album, anno, interprete, coverBytes, user, basePath);

            // Se tutto è andato bene, crea i nomi file
            String audioFilename = System.currentTimeMillis() + "_" + audioPart.getSubmittedFileName();
            String coverFilename = System.currentTimeMillis() + "_" + coverPart.getSubmittedFileName();
            String audioPath = mp3Dir + audioFilename;
            String coverPath = imgDir + coverFilename;

            // Salva fisicamente i file
            Files.write(new File(audioPath).toPath(), audioBytes);
            Files.write(new File(coverPath).toPath(), coverBytes);

            // Salva i path nel DB
            String audioDbPath = "files/audio/" + audioFilename;
            String coverDbPath = "files/images/" + coverFilename;

            dao.insertSong(titolo, audioDbPath, genere, album, anno, coverDbPath, interprete, user);

            session.setAttribute("uploadSuccess", true);

        } catch (Exception e) {
            session.setAttribute("uploadError", "Errore durante il caricamento del brano: " + e.getMessage());
        }

        response.sendRedirect("createPlaylist");
    }

    public void destroy() {
        try {
            if (connection != null) connection.close();
        } catch (SQLException ignored) {}
    }
}