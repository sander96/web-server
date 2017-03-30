package core;

import serverexception.AccessRestrictedException;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

public class DynamicPage {
    private StringBuilder page = new StringBuilder();

    public DynamicPage() {
    }

    public String createPage(List<String> files, String path){
        String test = "<!doctype html>\n" +
                "<html>\n" +
                "<head>\n" +
                "<meta charset=\"utf-8\">\n" +
                "</head>\n<body>\n" +
                "<h1>" + path + "</h1><hr>\n" +
                "<pre>\n";

        page.append(test);
        page.append("<a href=\"../\">../</a>\n");

        for(int i = 0; i < files.size(); ++i){           // TODO kaldkriips folderi puhul, faili puhul mitte, bugine
            String str = new String("<a href=" + "\"" + files.get(i) + "/" + "\"" + ">" + files.get(i) + "</a>\n");
            page.append(str);
        }

        String end = "</pre>\n<hr></body>\n</html>";
        page.append(end);

        return page.toString();
    }
}
