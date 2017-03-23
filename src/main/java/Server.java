import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static void main(String[] args) throws IOException{
        try(ServerSocket serverSocket = new ServerSocket(8080)){
            while(true){
                try{
                    Socket socket = serverSocket.accept();
                    Thread thread = new Thread(new Request(socket));
                    thread.start();
                }catch (IOException ioEx){
                    // keep server from crashing when serverSocket.accept() throws error
                    // must find out if this is necessary!!!
                    //  if necessary log to log file
                }
            }
        }
    }
}
