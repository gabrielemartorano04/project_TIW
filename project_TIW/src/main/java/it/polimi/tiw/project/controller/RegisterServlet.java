package it.polimi.tiw.project.controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import it.polimi.tiw.project.dao.UserDAO;

import org.thymeleaf.templateresolver.WebApplicationTemplateResolver;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/RegisterServlet")
public class RegisterServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
    private TemplateEngine templateEngine;
    
    @Override
    public void init() throws ServletException {
    	ServletContext servletContext = getServletContext();
    	JakartaServletWebApplication webApplication = JakartaServletWebApplication.buildApplication(servletContext);
    	WebApplicationTemplateResolver templateResolver = new WebApplicationTemplateResolver(webApplication);
    	templateResolver.setTemplateMode(TemplateMode.HTML);
    	this.templateEngine = new TemplateEngine();
    	this.templateEngine.setTemplateResolver(templateResolver);
    	templateResolver.setSuffix(".html");
    }
    
    @Override
    public void doPost (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	String username = request.getParameter("username");
    	String password = request.getParameter("password");
    	String name = request.getParameter("name");
    	String surname = request.getParameter("surname");

    	if (name == null || surname == null || username == null || password == null ||
            name.isBlank() || surname.isBlank() || username.isBlank() || password.isBlank()) {
            response.sendRedirect("register.html?error=empty");
            return;
        }
    	
    	try {
    		
    		Class.forName("com.mysql.cj.jdbc.Driver");

            Connection con = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/playlistdb?serverTimezone=UTC", "root", "1a2b3c4d");


            UserDAO userDAO = new UserDAO(con);

            if (userDAO.isUsernameTaken(username)) {
                con.close();
                response.sendRedirect("RegisterServlet?error=taken");
                return;
            }

            userDAO.registerUser(username, password, name, surname);

            con.close();

            request.getSession().setAttribute("username", username);
            response.sendRedirect("HomeServlet");

           
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            response.sendRedirect("register.html?error=server");
        }
    }
}