package it.polimi.tiw.project.controller;

import it.polimi.tiw.project.beans.Song;
import it.polimi.tiw.project.dao.SongDAO;

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

@WebServlet("/player")
public class PlayerServlet extends HttpServlet {

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
        String title = request.getParameter("title");

        if (title == null || title.isBlank()) {
            showErrorPage(request, response, "Missing or invalid title");
            return;
        }

        try {
            SongDAO dao = new SongDAO(connection);
            Song song = dao.findSong(title, username);

            if (song == null) {
                showErrorPage(request, response, "Song not found or not belonging to the user");
                return;
            }

            WebContext ctx = new WebContext(
                JakartaServletWebApplication.buildApplication(getServletContext()).buildExchange(request, response),
                request.getLocale());

            ctx.setVariable("song", song);
            templateEngine.process("player", ctx, response.getWriter());

        } catch (SQLException e) {
            e.printStackTrace();
            showErrorPage(request, response, "Error while song uploading");
        }
    }

    private void showErrorPage(HttpServletRequest request, HttpServletResponse response, String message)
            throws IOException {
        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().write("<h1>Errore</h1><p>" + message + "</p>");
    }

    public void destroy() {
        try {
            if (connection != null)
                connection.close();
        } catch (SQLException ignored) {
        }
    }
}