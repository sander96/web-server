package core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import serverexception.AccessRestrictedException;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Date;
import java.util.Scanner;

public class DynamicPage {
    private StringBuilder page = new StringBuilder();
    private static final Logger logger = LogManager.getLogger(DynamicPage.class);

    public String createFilePage(String folderPath) throws FileNotFoundException, AccessRestrictedException {
        File fileHandle = new File(folderPath);
        FileHandler.checkServerDirectory(fileHandle);

        StringBuilder htmlPage = new StringBuilder();

        try (Scanner scanner = new Scanner(new File("template.html"))) {
            while (scanner.hasNextLine()) {
                htmlPage.append(scanner.nextLine());
            }
        }

        StringBuilder body = new StringBuilder();
        body.append("<h1>" + folderPath + "</h1><hr><pre><a href=\"../\">../</a>\n");


        for (String filename : fileHandle.list()) { // TODO refactor
            String slash = "";

            File file = new File(folderPath + filename);

            if (new File(folderPath + filename).isDirectory()) {
                slash = "/";
            }

            Date date = new Date(file.lastModified());
            String dateString = String.format("%50s", filename + slash).replace(filename + slash, "");

            String spaces = dateString;
            String size = String.format("%50s", String.valueOf(file.length()));

            if (slash.equals("/")) {
                size = size.replace("0", "-");
            }

            String str = "<a href=" + "\"" + filename + slash + "\"" + ">" + filename + slash + "</a>" +
                    spaces + date + size + "\n";
            body.append(str);
        }

        body.append("</pre><hr><form method=\"post\" enctype=\"multipart/form-data\">" +
                "<input type=\"submit\" value=\"Upload file\"><input type=\"file\" name=\"filename\"></form>");

        return htmlPage.toString().replace("#body#", body).replace("#title#", folderPath);
    }

    public String createIndexPage() throws FileNotFoundException {
        StringBuilder htmlPage = new StringBuilder();

        try (Scanner scanner = new Scanner(new File("template.html"))) {
            while (scanner.hasNextLine()) {
                htmlPage.append(scanner.nextLine());
            }
        }

        StringBuilder body = new StringBuilder();
        body.append("<a href=files/>files</a><br><a href=login.html>login</a>");

        return htmlPage.toString().replace("#body#", body).replace("#title#", "Index");
    }
}
