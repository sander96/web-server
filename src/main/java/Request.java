import java.io.*;
import java.net.*;
import java.util.*;

public class Request implements Runnable{
    private Socket socket;
    private String request = "";

    public Request(Socket socket){
        this.socket = socket;
    }

    @Override
    public void run() {
        try{
            try(InputStream inStream = socket.getInputStream();
                OutputStream outStream = socket.getOutputStream()){
                while(true) {
                    byte[] buf = new byte[1024];
                    int num = inStream.read(buf);

                    if (num < 0){
                        // send bad request error? (or not)
                        break;
                    }

                    String tmp = new String(buf, 0, num, "UTF-8");
                    request += tmp;
                    System.out.println(tmp);

                    if (num > 3 && new String(buf, num-3, 3, "UTF-8").equals("\n\r\n")){
                        // must ascertain whether request has a body (meaning (for now) if POST or GET)
                        analyze(inStream, outStream);
                        break;
                    }
                }
            }finally {
                socket.close();
            }
        }catch (IOException ioEx){
            throw new RuntimeException(ioEx);
        }
    }

    private void analyze(InputStream inStream, OutputStream outStream) throws IOException{
        String[] req_lines = request.split("\n");
        String[] method_line = req_lines[0].split(" ");
        Map<String, String> headers = new HashMap<>();

        for(int i = 1; i < req_lines.length - 1; ++i){
            String[] h = req_lines[i].split(": ");

            if (h.length != 2){
                continue;
            }
            headers.put(h[0], h[1]);
        }

        switch (method_line[0]) {
            case "GET": // generate new GETRequest
                GETRequest get = new GETRequest(outStream, method_line[1]);
                get.sendResponse();
                break;
            case "POST": // (read request body) xor (send InStream to constructor) and generate new POSTRequest
                break;
            default: // generate new errorPage (we have no full support)
        }

        // prolly not going to stay here of course (daaa!?)
        System.out.println("--- DATA OF QUERY ---\n");
        System.out.println("Method: " + method_line[0]);
        System.out.println("Path: " + method_line[1]);
        System.out.println("Protocol and version: " + method_line[2]);
        System.out.println("\n--- HEADER DATA ---\n");
        for (Map.Entry<String, String> entry: headers.entrySet()){
            String key = entry.getKey();
            String value = entry.getValue();
            System.out.println(key + ": " + value);
        }
        System.out.println("\n--- END OF REQUEST ---\n\n");
    }
}
