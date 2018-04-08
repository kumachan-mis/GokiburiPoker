import java.net.*;
import java.io.*;
import java.util.*;

public class PokerServer{

    private static int PORT = 8080;
    public static int PLAYER = 5;
    private static ServerSocket s = null;
    private static Socket socket = null;
    public static PokerServerThread[] threads = null;

    public static void main(String[] args) throws IOException{

        if(args.length != 2){
            System.err.println("通信するポート番号とプレイヤーの人数を指定してください");
            System.exit(1);
        }

        PORT = Integer.parseInt(args[0]);
        PLAYER = Integer.parseInt(args[1]);

        try{
            s = new ServerSocket(PORT);
            System.out.println("準備完了 : " + s + "  プレイヤーの接続待機中...");
            PokerServerThread.setMainPlayerId((new Random()).nextInt(PLAYER));
            threads = new PokerServerThread[PLAYER];

            try{
                for(int n = 0; n < PLAYER;){
                    socket = s.accept();
                    System.err.println(socket);

                    if(socket != null){
                        threads[n] = new PokerServerThread(socket);
                        System.out.println("新しいプレイヤーが参加しました. " + (n + 1) + "/" + PLAYER);
                        threads[n].start();
                        n++;
                    }
                }

            }catch(IOException e){
                System.err.println(e);
            }

            try{
                for(int n = 0; n < PLAYER; n++){
                    threads[n].join();
                }
            }catch(InterruptedException e){
                System.err.println(e);
            }finally{
                System.out.println("ゲームを終了します.");
            }

        }finally{
            socket.close();
            s.close();
        }
    }
}

class PokerServerThread extends Thread{

    private static int ready = 0;
    private static int mainPlayerId = 0;
    private  static int loserId = -1;

    private Socket socket = null;
    private BufferedReader reader = null;
    private PrintWriter writer = null;;

    private int playerId = -1;
    private String nickName = null;
    private boolean isAvailable = true;

    public PokerServerThread(Socket socket) throws IOException{

        this.socket = socket;

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

        playerId = ready;
        nickName = "Player[" + ready + "]";
        isAvailable = true;
        ready++;

        if(ready == PokerServer.PLAYER){
            ready = 0;
        }
    }

    public static void setMainPlayerId(int mainPlayerId){
        PokerServerThread.mainPlayerId = mainPlayerId;
    }

    public void run(){
        try{
            initialize();
        }catch(IOException e){
            System.err.println(e);
        }
    }

    private synchronized void initialize() throws IOException{
        while(true){
            try{
                nickName = reader.readLine();
                if(nickName != null){
                    break;
                }
            }catch(IOException e){
                System.err.println(e);
                System.err.println("プレイヤー " + nickName + " との接続が切れました");
                isAvailable = false;
                socket.close();
                PokerServer.PLAYER--;
                return;
            }
        }

        String acMessage = nickName + " の登録が完了しました";
        System.out.println(acMessage);
        writer.println("[サーバ]: " + acMessage);
        ready++;
        
        synchro();
    }

    private void synchro(){
        try{
            while(ready < PokerServer.PLAYER){
                wait();
            }
        }catch(InterruptedException e){
            System.err.println(e);
        }
        notifyAll();
        ready = 0;
    }
}