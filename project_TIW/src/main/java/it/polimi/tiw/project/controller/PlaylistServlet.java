package it.polimi.tiw.project.controller;

import it.polimi.tiw.project.dao.PlaylistDAO;
import it.polimi.tiw.project.beans.Song;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.WebApplicationTemplateResolver;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

@WebServlet("/playlist")
public class PlaylistServlet extends HttpServlet {

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
            throw new ServletException("Unable while initialization: " + e.getMessage());
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            response.sendRedirect("login");
            return;
        }

        String username = (String) session.getAttribute("currentUser");
        String playlistName = request.getParameter("name");

        if (playlistName == null || playlistName.isBlank()) {
            showError(response, "Missing or invalid playlist title");
            return;
        }

        int page = 0;
        if (request.getParameter("page") != null) {
            try {
                page = Integer.parseInt(request.getParameter("page"));
                if (page < 0) page = 0;
            } catch (NumberFormatException e) {
                showError(response, "Invalid page number");
                return;
            }
        }

        PlaylistDAO dao = new PlaylistDAO(connection);
        try {
            if (!dao.playlistExistsForUser(username, playlistName)) {
                showError(response, "Playlist not found or not belonging to the user");
                return;
            }

            List<Song> allTracks = dao.findTracksInPlaylist(playlistName);
            int total = allTracks.size();
            int start = page * 5;
            int end = Math.min(start + 5, total);
            List<Song> pageTracks = allTracks.subList(start, end);

            WebContext ctx = new WebContext(
                JakartaServletWebApplication.buildApplication(getServletContext()).buildExchange(request, response),
                request.getLocale());

            ctx.setVariable("playlistName", playlistName);
            ctx.setVariable("tracks", pageTracks);
            ctx.setVariable("hasPrev", page > 0);
            ctx.setVariable("hasNext", end < total);
            ctx.setVariable("page", page);

            templateEngine.process("playlist", ctx, response.getWriter());

        } catch (SQLException e) {
            e.printStackTrace();
            showError(response, "Error while playlist uploading");
        }
    }

    private void showError(HttpServletResponse response, String message) throws IOException {
        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().write("<h1>Errore</h1><p>" + message + "</p>");
    }

    public void destroy() {
        try {
            if (connection != null) connection.close();
        } catch (SQLException ignored) {
        }
    }
}