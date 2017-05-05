package app;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import serverexception.AccessRestrictedException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;

public class DynamicPage {
    private static final Logger logger = LogManager.getLogger(DynamicPage.class);

    public String createFilePage(String folderPath, boolean isUser, boolean checkboxes) throws IOException, AccessRestrictedException {
        File fileHandle = new File(folderPath);
        FileHandler.checkServerDirectory(fileHandle);

        String htmlPage = new String(Files.readAllBytes(Paths.get("data/template.html")));

        StringBuilder body = new StringBuilder();
        body.append("<h1>" + folderPath.substring(4) + "</h1>");

        if (checkboxes) {
            body.append("<form method=\"post\">");
        }

        body.append("<hr><pre><a href=\"../\">../</a>\n");

        for (String filename : fileHandle.list()) {
            String slash = "";

            File file = new File(folderPath + filename);

            if (new File(folderPath + filename).isDirectory()) {
                slash = "/";
            }

            Date date = new Date(file.lastModified());
            String dateString = String.format("%50s", filename + slash).replace(filename + slash, "");

            String fileSize = String.format("%50s", String.valueOf(file.length()));

            if (slash.equals("/")) {
                fileSize = fileSize.replace("0", "-");
            }

            String str = "<div><a href=" + "\"" + filename + slash + "\"" + ">" + filename + slash + "</a>" +
                    dateString + date + fileSize;
            body.append(str);

            if (checkboxes) {
                body.append("<input type=\"checkbox\" name=\"" + filename + "\">");
            }

            body.append("</div>");
        }

        body.append("</pre><hr>");

        if (checkboxes) {
            body.append("<input type=\"submit\" value=\"Delete files\"></form>");
        }

        if (isUser && !checkboxes) {
            body.append("<div><form method=\"post\" enctype=\"multipart/form-data\">" +
                    "<input type=\"submit\" value=\"Upload file\"><input type=\"file\" name=\"filename\"></form></div>");
        }

        if (isUser && !checkboxes) {
            body.append("<div><form method=\"get\"><input type=\"submit\" name=\"checkbox-delete\" value=\"Delete files\"></form></div>");
        }

        return htmlPage.replace("#body#", body).replace("#title#", folderPath.substring(4));
    }

    public String createIndexPage(String cookie, String username) throws IOException {
        String htmlPage = new String(Files.readAllBytes(Paths.get("data/index-template.html")));

        StringBuilder body = new StringBuilder();
        body.append("<a href=files/>Files</a>");

        if (cookie == null) {
            body.append("<br><a href=login.html>Log in</a>");
            body.append("<br><a href=register.html>Register</a>");
        }

        if (cookie != null) {
            body.append("<br><a href=logout>Log out</a>");
            body.append("<br><p>Logged in as <b>" + username + "</b></p>");
        }

        return htmlPage.replace("#body#", body).replace("#title#", "Index");
    }

    public String createLoginPage(boolean wrongLogin) throws IOException {
        String htmlPage = new String(Files.readAllBytes(Paths.get("data/login-template.html")));
        String message = "";

        if (wrongLogin) {
            message = "<p>Wrong username or password</p>";
        }

        return htmlPage.replace("#title#", "Login")
                .replace("#button#", "Sign in").replace("#paragraph#", message);
    }

    public String createRegisterPage(boolean wrongUsername) throws IOException {
        String htmlPage = new String(Files.readAllBytes(Paths.get("data/login-template.html")));
        String message = "";

        if (wrongUsername) {
            message = "<p>Username already exists.</p>";
        }

        return htmlPage.replace("#title#", "Register")
                .replace("#button#", "Register").replace("#paragraph#", message);
    }
}
