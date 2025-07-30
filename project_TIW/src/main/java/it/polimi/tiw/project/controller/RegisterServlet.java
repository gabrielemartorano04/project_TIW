package it.polimi.tiw.project.controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.UnavailableException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.WebApplicationTemplateResolver;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import it.polimi.tiw.project.dao.UserDAO;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
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

			this.templateEngine = new TemplateEngine();
			this.templateEngine.setTemplateResolver(templateResolver);

		} catch (ClassNotFoundException e) {
			throw new UnavailableException("Can't load database driver");
		} catch (SQLException e) {
			throw new UnavailableException("Couldn't get DB connection");
		}
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setCharacterEncoding("UTF-8");
		response.setContentType("text/html");

		String name = request.getParameter("name");
		String surname = request.getParameter("surname");
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		String confirm = request.getParameter("confirm");

		if (name == null || surname == null || username == null || password == null || confirm == null ||
				name.isBlank() || surname.isBlank() || username.isBlank() || password.isBlank() || confirm.isBlank()) {
			forwardWithError(request, response, "Fill in all the fields");
			return;
		}

		if (!password.equals(confirm)) {
			forwardWithError(request, response, "Passwords don't match");
			return;
		}

		UserDAO userDAO = new UserDAO(connection);
		try {
			if (userDAO.isUsernameTaken(username)) {
				forwardWithError(request, response, "username already existing");
				return;
			}

			userDAO.registerUser(username, password, name, surname);

			HttpSession session = request.getSession(true);
			session.setAttribute("currentUser", username);

			response.sendRedirect(getServletContext().getContextPath() + "/createPlaylist");

		} catch (SQLException e) {
			forwardWithError(request, response, "Database error while registration");
		}
	}
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
	        throws ServletException, IOException {
		response.setCharacterEncoding("UTF-8");
		response.setContentType("text/html");

		HttpSession session = request.getSession(false);
		if (session != null && session.getAttribute("currentUser") != null) {
			response.sendRedirect("createPlaylist");
			return;
		}

	    JakartaServletWebApplication webApp = JakartaServletWebApplication.buildApplication(getServletContext());
	    WebContext ctx = new WebContext(webApp.buildExchange(request, response), request.getLocale());
	    templateEngine.process("register", ctx, response.getWriter());
	}

	private void forwardWithError(HttpServletRequest request, HttpServletResponse response, String errorMsg) throws IOException {
		response.setCharacterEncoding("UTF-8");
		response.setContentType("text/html");

		JakartaServletWebApplication webApp = JakartaServletWebApplication.buildApplication(getServletContext());
		WebContext ctx = new WebContext(webApp.buildExchange(request, response), request.getLocale());
		ctx.setVariable("errorMsg", errorMsg);
		templateEngine.process("register", ctx, response.getWriter());
	}

	public void destroy() {
		try {
			if (connection != null)
				connection.close();
		} catch (SQLException e) {
		}
	}
}