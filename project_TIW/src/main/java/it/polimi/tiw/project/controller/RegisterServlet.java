package it.polimi.tiw.project.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import it.polimi.tiw.project.dao.UserDAO;

import java.sql.DriverManager;

@WebServlet("/RegisterServlet")
public class RegisterServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    boolean loginSuccess = false;

	@Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String name = request.getParameter("nome");
        String surname = request.getParameter("cognome");
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String confirm = request.getParameter("confirm");

        // Controlli base
        if (name == null || surname == null || username == null || password == null || confirm == null ||
            name.isBlank() || surname.isBlank() || username.isBlank() || password.isBlank() || confirm.isBlank()) {
            response.sendRedirect("register.html?error=empty");
            return;
        }

        if (!password.equals(confirm)) {
            response.sendRedirect("register.html?error=mismatch");
            return;
        }

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/playlistdb?serverTimezone=UTC",
                    "root",
                    "1a2b3c4d");

            UserDAO userDAO = new UserDAO(con);
            loginSuccess = userDAO.checkCredentials(username, password);

            con.close();

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("login.html?error=db");
            return;
        }
        
    }
	
	@Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println("<html><body>");
        out.println("<h2>Registrati</h2>");
        out.println("<form action='RegisterServlet' method='post'>");
        out.println("Nome: <input type='text' name='nome'><br>");
        out.println("Cognome: <input type='text' name='cognome'><br>");
        out.println("Username: <input type='text' name='username'><br>");
        out.println("Password: <input type='password' name='password'><br>");
        out.println("Conferma: <input type='password' name='confirm'><br>");
        out.println("<input type='submit' value='Registrati'>");
        out.println("</form>");
        out.println("</body></html>");
    }
}