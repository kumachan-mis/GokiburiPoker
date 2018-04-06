import java.io.*;
import java.net.*;

public class JabberServer{
    public static int PORT = 8080;
    public static void main(String[] args) throws IOException{

        if(args.length != 1){
            System.err.println("Usage : Input PORT number");
            System.exit(1);
        }
        
        PORT = Integer.parseInt(args[0]);
        ServerSocket s = new ServerSocket(PORT);
        System.out.println("Listening : " + s);
        try{
            Socket socket = s.accept();
            try{
                System.out.println("Connection AC : " + socket);
                BufferedReader reader
                = new BufferedReader(
                    new InputStreamReader(
                        socket.getInputStream()
                    )
                );
                PrintWriter writer
                = new PrintWriter(
                    new BufferedWriter(
                        new OutputStreamWriter(
                            socket.getOutputStream()
                        )
                    ), true
                );

                while(true){
                    String str = reader.readLine();
                    if(str != null){
                        if(str.equals("END")){
                            break;
                        }
                        System.out.println("Echoing : " + str);
                        writer.println(str);
                    }
                }
            }finally{
                System.out.println("closing...");
                socket.close();
            }
        }finally{
            s.close();
        }
    }
}