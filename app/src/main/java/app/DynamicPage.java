package app;

import core.FileHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import serverexception.AccessRestrictedException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

public class DynamicPage {
    private static final Logger logger = LogManager.getLogger(DynamicPage.class);

    public String createFilePage(String folderPath) throws IOException, AccessRestrictedException {
        File fileHandle = new File(folderPath);
        FileHandler.checkServerDirectory(fileHandle);

        String htmlPage = new String(Files.readAllBytes(Paths.get("template.html")));

        StringBuilder body = new StringBuilder();
        body.append("<h1>" + folderPath + "</h1><hr><pre><a href=\"../\">../</a>\n");

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

            String str = "<a href=" + "\"" + filename + slash + "\"" + ">" + filename + slash + "</a>" +
                    dateString + date + fileSize + "\n";
            body.append(str);
        }

        body.append("</pre><hr><form method=\"post\" enctype=\"multipart/form-data\">" +
                "<input type=\"submit\" value=\"Upload file\"><input type=\"file\" name=\"filename\"></form>");

        return htmlPage.replace("#body#", body).replace("#title#", folderPath);
    }

    public String createIndexPage(String cookie, String username) throws IOException {
        String htmlPage = new String(Files.readAllBytes(Paths.get("template.html")));

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
        String htmlPage = new String(Files.readAllBytes(Paths.get("login-template.html")));
        String message = "";

        if (wrongLogin) {
            message = "<p>Wrong username or password</p>";
        }

        return htmlPage.replace("#title#", "Login")
                .replace("#button#", "Sign in").replace("#paragraph#", message);
    }

    public String createRegisterPage(boolean wrongUsername) throws IOException {
        String htmlPage = new String(Files.readAllBytes(Paths.get("login-template.html")));
        String message = "";

        if (wrongUsername) {
            message = "<p>Username already exists.</p>";
        }

        return htmlPage.replace("#title#", "Register")
                .replace("#button#", "Register").replace("#paragraph#", message);
    }
}
