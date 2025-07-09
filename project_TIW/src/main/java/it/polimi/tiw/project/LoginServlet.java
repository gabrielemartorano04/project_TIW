package it.polimi.tiw.project;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.io.PrintWriter;

import org.thymeleaf.TemplateEngine;

@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

	@Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Prendi i dati dal form HTML
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        // Validazione base (qui puoi usare DAO con database)
        if (username == null || password == null || username.isBlank() || password.isBlank()) {
            response.sendRedirect("login.html?error=1");
            return;
        }

        // ESEMPIO: login fittizio (da sostituire con accesso DB)
        if (username.equals("admin") && password.equals("1234")) {
            HttpSession session = request.getSession();
            session.setAttribute("username", username);
            response.sendRedirect("HomeServlet"); // questa servlet la creerai tu dopo
        } else {
            response.sendRedirect("login.html?error=1");
        }
    }
	
	@Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String errorParam = request.getParameter("error");
        boolean showError = errorParam != null && errorParam.equals("1");

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        TemplateEngine tmp = new TemplateEngine();
        
    }
}