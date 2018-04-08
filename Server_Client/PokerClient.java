import java.net.*;
import java.io.*;
import java.util.*;

public class PokerClient{

    private static int PORT = 8080;
    private static InetAddress addr = null;
    private static Socket socket = null;
    private static BufferedReader reader = null;
    private static PrintWriter writer = null;
    private static Scanner sc = null;

    public static void main(String[] args) throws IOException{
        if(args.length != 2){
            System.err.println("通信するポート番号とローカルホスト名を指定してください");
            System.exit(1);
        }

        PORT = Integer.parseInt(args[0]);
        addr = InetAddress.getByName(args[1]);
        System.out.println("アドレス : " + addr);

        makeInstance();
        sendNickname();

        sc.close();
        socket.close();
    }

    private static void makeInstance(){
        sc = new Scanner(System.in);

        try{
            socket = new Socket(addr, PORT);

            reader = 
            new BufferedReader(
                new InputStreamReader(
                    socket.getInputStream()
                )
            );
            
            writer = 
            new PrintWriter(
                new BufferedWriter(
                    new OutputStreamWriter(
                        socket.getOutputStream()
                    )
                ), true
            );
        }catch(IOException e){
            System.err.println(e);
        }
    }

    private static void sendNickname(){
        String nickName = null;
        boolean correctName = false;

        while(correctName == false){
            System.out.print("ニックネームを入力してください: ");
            nickName = sc.next();
            System.out.print("ニックネームが " + nickName + " でよければ y を入力してください: ");
            String reply = sc.next();
            correctName = (reply.equals("y") || reply.equals("Y"));
        }

        writer.println(nickName);

        try{
            System.out.println(reader.readLine());
        }catch(IOException e){
            System.err.println(e);
            System.err.println("サーバ " + addr + " との接続が切れました");
            return;
        }
    }
}