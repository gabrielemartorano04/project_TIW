package it.polimi.tiw.project.controller;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

@WebServlet("/files/*")
public class FileServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private String basePath;

    @Override
    public void init() throws ServletException {
        ServletContext context = getServletContext();
        basePath = context.getInitParameter("basePath");
        if (basePath == null || basePath.isBlank()) {
            throw new ServletException("Parameter basePath not specified in web.xml");
        }
        if (!basePath.endsWith(File.separator)) {
            basePath += File.separator;
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthenticated user");
            return;
        }

        String requestedFile = req.getPathInfo();
        if (requestedFile == null || requestedFile.contains("..") || requestedFile.contains("/") && requestedFile.startsWith("/../")) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invelid request");
            return;
        }

        File file = new File(basePath, requestedFile);
        if (!file.exists() || file.isDirectory()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "File not found");
            return;
        }

        String mimeType = getServletContext().getMimeType(file.getName());
        if (mimeType == null) {
            mimeType = "application/octet-stream";
        }

        resp.setContentType(mimeType);
        resp.setContentLengthLong(file.length());

        try (FileInputStream fis = new FileInputStream(file); OutputStream os = resp.getOutputStream()) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
        }
    }
}