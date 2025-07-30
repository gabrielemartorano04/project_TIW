package it.polimi.tiw.project.controller;

import it.polimi.tiw.project.dao.UserDAO;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.WebApplicationTemplateResolver;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

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
            resolver.setSuffix(".html");
            templateEngine = new TemplateEngine();
            templateEngine.setTemplateResolver(resolver);

        } catch (Exception e) {
            throw new ServletException("Unable while initialization: " + e.getMessage());
        }
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        JakartaServletWebApplication webApp = JakartaServletWebApplication.buildApplication(getServletContext());
        WebContext ctx = new WebContext(webApp.buildExchange(request, response), request.getLocale());
        templateEngine.process("login", ctx, response.getWriter());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        if (username == null || password == null || username.isBlank() || password.isBlank()) {
            forwardWithError(request, response, "Username and password are mandatory");
            return;
        }

        try {
            UserDAO userDAO = new UserDAO(connection);
            boolean validCredentials = userDAO.checkCredentials(username, password);

            if (!validCredentials) {
                forwardWithError(request, response, "Invalid credentials");
            } else {
                HttpSession oldSession = request.getSession(false);
                if (oldSession != null) {
                    oldSession.invalidate();
                }

                HttpSession session = request.getSession(true);
                session.setAttribute("currentUser", username);

                response.sendRedirect("createPlaylist");
            }

        } catch (SQLException e) {
            forwardWithError(request, response, "Internal error during login");
        }
    }

    private void forwardWithError(HttpServletRequest request, HttpServletResponse response, String message)
            throws IOException, ServletException {
        JakartaServletWebApplication app = JakartaServletWebApplication.buildApplication(getServletContext());
        WebContext ctx = new WebContext(app.buildExchange(request, response), request.getLocale());
        ctx.setVariable("errorMsg", message);
        templateEngine.process("login", ctx, response.getWriter());
    }

    @Override
    public void destroy() {
        try {
            if (connection != null)
                connection.close();
        } catch (SQLException ignore) {
        }
    }
}