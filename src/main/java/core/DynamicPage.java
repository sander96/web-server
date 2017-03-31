package core;

import serverexception.AccessRestrictedException;

import java.io.File;
import java.io.FileNotFoundException;

public class DynamicPage {
    private StringBuilder page = new StringBuilder();

    public String createPage(String folderPath) throws FileNotFoundException, AccessRestrictedException {
        if (!folderPath.endsWith("/")) {
            throw new RuntimeException("This was not a folder");
        }

        FileHandler.checkServerDirectory(new File(folderPath));

        String[] files = new File(folderPath).list();

        String htmlString = "<!doctype html>\n" +
                "<html>\n" +
                "<head>\n" +
                "<meta charset=\"utf-8\">\n" +
                "</head>\n<body>\n" +
                "<h1>" + folderPath + "</h1><hr>\n" +
                "<pre>\n";

        page.append(htmlString);
        page.append("<a href=\"../\">../</a>\n");

        for (int i = 0; i < files.length; ++i) {
            String slash = "";

            if (new File(folderPath + files[i]).isDirectory()) {
                slash = "/";
            }

            String str = "<a href=" + "\"" + files[i] + slash + "\"" + ">" + files[i] + "</a>\n";
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
