package app;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import serverexception.AccessRestrictedException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

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

    public String createIndexPage(String cookie, String username, boolean admin) throws IOException {
        String htmlPage = new String(Files.readAllBytes(Paths.get("data/index-template.html")));

        StringBuilder body = new StringBuilder();
        body.append("<a href=files/>Files</a>");

        if (admin) {
            body.append("<br><a href=users.html>Users</a>");
        }

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

    public String createUsersPage(boolean isAdmin, Map<String, Boolean> usernames) throws IOException {
        if (!isAdmin) {
            throw new RuntimeException();
        }

        String htmlPage = new String(Files.readAllBytes(Paths.get("data/index-template.html")));
        StringBuilder body = new StringBuilder();
        body.append("<form method=\"post\"><pre>");

        ArrayList<String> sortedUsernames = new ArrayList<>(usernames.keySet());
        Collections.sort(sortedUsernames);

        for (String username : sortedUsernames) {
            body.append("<div>" + String.format("%-20s", username));

            if (usernames.get(username)) {
                body.append("<input type=\"checkbox\" disabled>");
            } else {
                body.append("<input type=\"checkbox\" name=\"" + username + "\">");

            }

            body.append("</div>");
        }

        body.append("</pre><input type=\"submit\" value=\"Delete selected users\"></form>");

        return htmlPage.replace("#body#", body).replace("#title#", "Users");
    }
}
