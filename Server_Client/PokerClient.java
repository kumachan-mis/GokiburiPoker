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
        socket = new Socket(addr, PORT);

        sc = new Scanner(System.in);

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

        initialize();

        sc.close();
        socket.close();
    }

    private static void initialize(){
        String nickName = null;
        boolean correctName = false;

        while(correctName == false){
            System.out.print("ニックネームを入力してください: ");
            nickName = sc.next();
            System.out.print("ニックネームが " + nickName + " でよければ 0 を入力してください: ");
            correctName = (sc.next().equals("0"));
        }

        writer.println(nickName);
        try{
            System.out.println(reader.readLine());
        }catch(IOException e){
            System.err.println(e);
            System.err.println("サーバ " + addr + " との接続が切れました");
        }
    }
}