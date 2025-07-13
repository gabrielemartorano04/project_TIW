package it.polimi.tiw.project.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/files/*")
public class FileServlet extends HttpServlet {
	
    private static final long serialVersionUID = 1L;
	private String basePath;

    public void init() throws ServletException {
        basePath = getServletContext().getInitParameter("basePath");
        if (!basePath.endsWith("/")) basePath += "/";
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String requestPath = req.getPathInfo();
        if (requestPath == null || requestPath.contains("..")) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        File file = new File(basePath + requestPath);
        if (!file.exists()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String mime = getServletContext().getMimeType(file.getName());
        if (mime == null) mime = "application/octet-stream";
        resp.setContentType(mime);

        Files.copy(file.toPath(), resp.getOutputStream());
    }
}
