package it.polimi.tiw.project;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.DriverManager;

@WebServlet("/RegisterServlet")
public class RegisterServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

	@Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String nome = request.getParameter("nome");
        String cognome = request.getParameter("cognome");
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String confirm = request.getParameter("confirm");

        // Controlli base
        if (nome == null || cognome == null || username == null || password == null || confirm == null ||
            nome.isBlank() || cognome.isBlank() || username.isBlank() || password.isBlank() || confirm.isBlank()) {
            response.sendRedirect("register.html?error=empty");
            return;
        }

        if (!password.equals(confirm)) {
            response.sendRedirect("register.html?error=mismatch");
            return;
        }

        // Connessione al DB (modifica parametri con i tuoi valori)
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/tuoDB", "user", "pwd")) {

            // Controlla se l’username è già presente
            PreparedStatement checkStmt = con.prepareStatement("SELECT * FROM utenti WHERE username = ?");
            checkStmt.setString(1, username);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                response.sendRedirect("register.html?error=taken");
                return;
            }

            // Inserisci nuovo utente
            PreparedStatement insertStmt = con.prepareStatement(
                    "INSERT INTO utenti (username, password, nome, cognome) VALUES (?, ?, ?, ?)");
            insertStmt.setString(1, username);
            insertStmt.setString(2, password); // In un progetto reale andrebbe criptata!
            insertStmt.setString(3, nome);
            insertStmt.setString(4, cognome);
            insertStmt.executeUpdate();

            response.sendRedirect("LoginServlet");

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("register.html?error=generic");
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