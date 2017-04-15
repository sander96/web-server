package core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import serverexception.AccessRestrictedException;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Date;

public class DynamicPage {
    private StringBuilder page = new StringBuilder();
    private static final Logger logger = LogManager.getLogger(DynamicPage.class);

    public String createPage(String folderPath) throws FileNotFoundException, AccessRestrictedException {
        File fileHandle = new File(folderPath);
        FileHandler.checkServerDirectory(fileHandle);

        String htmlString = "<!doctype html>\n" +
                "<html>\n" +
                "<head>\n" +
                "<meta charset=\"utf-8\">\n" +
                "</head>\n<body>\n" +
                "<h1>" + folderPath + "</h1><hr>\n" +
                "<pre>\n";

        page.append(htmlString);
        page.append("<a href=\"../\">../</a>\n");

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
            page.append(str);
        }

        String end = "</pre>\n<hr>      <form action=\"\" method=\"post\" enctype=\"multipart/form-data\">\n" +
                "         <input type=\"submit\" value=\"Upload file\">\n" +
                "\t\t <input type=\"file\" name=\"fileName\">\n" +
                "      </form></body>\n</html>";
        page.append(end);

        return page.toString();
    }
}
