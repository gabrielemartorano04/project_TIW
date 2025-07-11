package it.polimi.tiw.project.controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.UnavailableException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.WebApplicationTemplateResolver;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import it.polimi.tiw.project.dao.UserDAO;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection;
	private TemplateEngine templateEngine;

	public void init() throws ServletException {
		try {
			ServletContext context = getServletContext();
			String driver = context.getInitParameter("dbDriver");
			String url = context.getInitParameter("dbUrl");
			String user = context.getInitParameter("dbUser");
			String password = context.getInitParameter("dbPassword");
			Class.forName(driver);
			connection = DriverManager.getConnection(url, user, password);

			JakartaServletWebApplication webApp = JakartaServletWebApplication.buildApplication(getServletContext());
			WebApplicationTemplateResolver templateResolver = new WebApplicationTemplateResolver(webApp);
			templateResolver.setTemplateMode(TemplateMode.HTML);
			templateResolver.setPrefix("/");
			templateResolver.setSuffix(".html");
			templateResolver.setCharacterEncoding("UTF-8");

			templateEngine = new TemplateEngine();
			templateEngine.setTemplateResolver(templateResolver);

		} catch (ClassNotFoundException | SQLException e) {
			throw new UnavailableException("Database connection error: " + e.getMessage());
		}
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		JakartaServletWebApplication webApp = JakartaServletWebApplication.buildApplication(getServletContext());
		WebContext ctx = new WebContext(webApp.buildExchange(request, response), request.getLocale());
		templateEngine.process("login", ctx, response.getWriter());
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String username = request.getParameter("username");
		String password = request.getParameter("password");

		if (username == null || password == null || username.isBlank() || password.isBlank()) {
			forwardWithError(request, response, "Compila tutti i campi.");
			return;
		}

		UserDAO userDAO = new UserDAO(connection);
		try {
			boolean valid = userDAO.checkCredentials(username, password);
			if (!valid) {
				forwardWithError(request, response, "Credenziali non valide.");
				return;
			}
			HttpSession session = request.getSession();
			session.setAttribute("currentUser", username);
			response.sendRedirect(getServletContext().getContextPath() + "/home"); // o HomeWorker
		} catch (SQLException e) {
			forwardWithError(request, response, "Errore durante il login.");
		}
	}

	private void forwardWithError(HttpServletRequest request, HttpServletResponse response, String errorMsg)
			throws IOException {
		JakartaServletWebApplication webApp = JakartaServletWebApplication.buildApplication(getServletContext());
		WebContext ctx = new WebContext(webApp.buildExchange(request, response), request.getLocale());
		ctx.setVariable("errorMsg", errorMsg);
		templateEngine.process("login", ctx, response.getWriter());
	}

	public void destroy() {
		try {
			if (connection != null) connection.close();
		} catch (SQLException ignored) {}
	}
}