import java.io.*;
import java.net.*;

public class JabberClient{
    public static final int N = 10;
    public static int PORT = 8080;
    public static void main(String[] args) throws IOException{
        
        if(args.length != 2){
            System.err.println("Usage : Input HostName and PORT number");
            System.exit(1);
        }

        InetAddress addr = InetAddress.getByName(args[0]);
        PORT = Integer.parseInt(args[1]);
        System.out.println("Address : " + addr);
        Socket socket = new Socket(addr, PORT);
        
        try{
            System.out.println("socket : " + socket);
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
            //System.out.println("debug");
            for(int i = 0; i < N; ++i){
                writer.println("howdy : " + i);
                String str = reader.readLine();
                System.out.println(str);
            }
            System.out.println("END");
        }finally{
            socket.close();
        }
    }
}