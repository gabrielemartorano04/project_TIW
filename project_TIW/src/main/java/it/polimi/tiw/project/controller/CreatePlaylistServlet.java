package it.polimi.tiw.project.controller;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.UnavailableException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.WebApplicationTemplateResolver;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import it.polimi.tiw.project.beans.Playlist;
import it.polimi.tiw.project.dao.PlaylistDAO;

@WebServlet("/createPlaylist")
public class CreatePlaylistServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private Connection connection;
    private TemplateEngine templateEngine;

    @Override
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
        } catch (ClassNotFoundException | SQLException e) {
            throw new UnavailableException("Errore durante l'inizializzazione: " + e.getMessage());
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            response.sendRedirect("login");
            return;
        }

        String username = (String) session.getAttribute("currentUser");

        List<Playlist> playlists;
        try {
            PlaylistDAO dao = new PlaylistDAO(connection);
            playlists = dao.findPlaylistsByUser(username);
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore nel caricamento playlist");
            return;
        }

        WebContext ctx = new WebContext(
                JakartaServletWebApplication.buildApplication(getServletContext()).buildExchange(request, response),
                request.getLocale());

        ctx.setVariable("username", username);
        ctx.setVariable("playlists", playlists);

        // Messaggi opzionali
        HttpSession sessionAttr = request.getSession();
        Boolean uploadSuccess = (Boolean) sessionAttr.getAttribute("uploadSuccess");
        String uploadError = (String) sessionAttr.getAttribute("uploadError");
        if (uploadSuccess != null && uploadSuccess) {
            ctx.setVariable("uploadSuccess", true);
            sessionAttr.removeAttribute("uploadSuccess");
        }
        if (uploadError != null) {
            ctx.setVariable("uploadError", uploadError);
            sessionAttr.removeAttribute("uploadError");
        }

        templateEngine.process("home", ctx, response.getWriter());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            response.sendRedirect("login");
            return;
        }

        String playlistTitle = request.getParameter("titolo");
        String username = (String) session.getAttribute("currentUser");

        if (playlistTitle == null || playlistTitle.isBlank()) {
            session.setAttribute("uploadError", "Il titolo della playlist non può essere vuoto.");
            response.sendRedirect("createPlaylist");
            return;
        }

        PlaylistDAO dao = new PlaylistDAO(connection);
        try {
            LocalDate today = LocalDate.now();
            System.out.println("chiamo createPlaylist");
            dao.createPlaylist(username, playlistTitle, today);
            response.sendRedirect("addToPlaylist?playlist=" + playlistTitle);
        } catch (SQLException e) {
            session.setAttribute("uploadError", "Errore nella creazione della playlist: " + e.getMessage());
            response.sendRedirect("createPlaylist");
        }
    }

    @Override
    public void destroy() {
        try {
            if (connection != null) connection.close();
        } catch (SQLException ignored) {}
    }
}