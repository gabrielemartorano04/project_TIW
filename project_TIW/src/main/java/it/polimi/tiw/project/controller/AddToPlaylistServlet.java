package it.polimi.tiw.project.controller;

import it.polimi.tiw.project.dao.PlaylistDAO;
import it.polimi.tiw.project.dao.SongDAO;
import it.polimi.tiw.project.beans.Song;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templateresolver.WebApplicationTemplateResolver;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

@WebServlet("/addToPlaylist")
public class AddToPlaylistServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private TemplateEngine templateEngine;
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

            JakartaServletWebApplication webApp = JakartaServletWebApplication.buildApplication(context);
            WebApplicationTemplateResolver resolver = new WebApplicationTemplateResolver(webApp);
            resolver.setTemplateMode(TemplateMode.HTML);
            resolver.setPrefix("/");
            resolver.setSuffix(".html");
            resolver.setCharacterEncoding("UTF-8");

            templateEngine = new TemplateEngine();
            templateEngine.setTemplateResolver(resolver);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            response.sendRedirect("login");
            return;
        }

        String username = (String) session.getAttribute("currentUser");
        String playlistTitle = request.getParameter("playlist");

        if (playlistTitle == null || playlistTitle.isBlank()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Playlist non specificata");
            return;
        }

        try {
            SongDAO songDAO = new SongDAO(connection);
            PlaylistDAO playlistDAO = new PlaylistDAO(connection);

            if (playlistDAO.playlistExistsForUser(username, playlistTitle) == false) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Accesso negato alla playlist");
                return;
            }

            List<Song> allUserSongs = songDAO.findSongByUser(username);
            List<Song> playlistSongs = playlistDAO.findTracksInPlaylist(playlistTitle);

            Set<String> playlistTitles = new HashSet<>();
            for (Song s : playlistSongs) {
                playlistTitles.add(s.getTitle());
            }

            List<Song> availableSongs = new ArrayList<>();
            for (Song s : allUserSongs) {
                if (!playlistTitles.contains(s.getTitle())) {
                    availableSongs.add(s);
                }
            }

            WebContext ctx = new WebContext(
                    JakartaServletWebApplication.buildApplication(getServletContext()).buildExchange(request, response),
                    request.getLocale());
            ctx.setVariable("songs", availableSongs);
            ctx.setVariable("playlist", playlistTitle);

            templateEngine.process("addToPlaylist", ctx, response.getWriter());

        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore durante il recupero dei brani");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            response.sendRedirect("login");
            return;
        }

        String username = (String) session.getAttribute("currentUser");
        String playlistTitle = request.getParameter("playlist");
        String[] selectedSongs = request.getParameterValues("songs");

        if (playlistTitle == null || playlistTitle.isBlank()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Titolo playlist mancante");
            return;
        }

        if (selectedSongs == null || selectedSongs.length == 0) {
            // Nessuna canzone selezionata, non errore ma solo redirect
            response.sendRedirect("addToPlaylist?playlist=" + playlistTitle);
            return;
        }

        try {
            PlaylistDAO playlistDAO = new PlaylistDAO(connection);

            if (playlistDAO.playlistExistsForUser(username, playlistTitle) == false) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Accesso negato");
                return;
            }

            ArrayList<String> songsToAdd = new ArrayList<>(Arrays.asList(selectedSongs));
            playlistDAO.addTracksToPlaylist(playlistTitle, songsToAdd);

        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore durante l'aggiunta dei brani");
            return;
        }

        response.sendRedirect("createPlaylist");
    }

    public void destroy() {
        try {
            if (connection != null) connection.close();
        } catch (SQLException ignored) {}
    }
}