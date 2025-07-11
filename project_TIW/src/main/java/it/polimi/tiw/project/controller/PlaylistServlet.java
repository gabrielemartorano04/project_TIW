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
            throw new ServletException(e);
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            response.sendRedirect("login");
            return;
        }

        String playlistName = request.getParameter("name");
        int page = 0;
        if (request.getParameter("page") != null) {
            try {
                page = Integer.parseInt(request.getParameter("page"));
            } catch (NumberFormatException ignored) {}
        }

        PlaylistDAO dao = new PlaylistDAO(connection);
        try {
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
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore nel caricamento brani");
        }
    }

    public void destroy() {
        try { 
        	if (connection != null) connection.close(); 
    	} catch (SQLException ignored) {}
    }
}