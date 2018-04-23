import java.util.Scanner;
import java.io.IOException;
import java.net.InetAddress;
import processing.core.PApplet;
import java.net.UnknownHostException;

public class PokerClient{

    static int PORT = 8080;
    static InetAddress addr = null;
    private static ClientCommunication cc = null;

    public static void main(String[] args){
        if(args.length != 2){
            System.err.println("通信する　ポート番号 と ローカルホスト名　を指定してください");
            System.exit(1);
        }

        PORT = Integer.parseInt(args[0]);
        try{
            addr = InetAddress.getByName(args[1]);
        }catch(UnknownHostException e){
            System.err.println(e);            
        }

        System.out.println("アドレス : " + addr);
        
        initialize();
        beforeStart();
        sendNickname();
        receivePlayerInfo();
        receiveHandCards();

        PApplet.main("GUIClient");
    }

    private static void initialize(){
        GUIClient.makeInstance(addr, PORT);
        cc = GUIClient.cc;
    }  //各種初期化

    private static void beforeStart(){
        System.out.println("全プレイヤーの準備ができるまでしばらくお待ちください");   //全員が接続できるまで待機
        String str = cc.readSingleMessage();  //受信番号: R1, 全員の接続を検出
        System.out.println(str);  //メッセージをコマンドラインに表示
    }

    private static void sendNickname(){
        Scanner sc = new Scanner(System.in);
        boolean correctName = false;
        String str = null;
        
        while(correctName == false){
            System.out.print("ニックネームを入力してください: ");
            str = sc.next();  //ニックネームを取得
            System.out.print("ニックネームが " + str + " でよければ y を入力してください: ");
            String reply = sc.next();  //登録ニックネームの確認を取得
            correctName = (reply.toLowerCase().equals("yes") || reply.toLowerCase().equals("y"));
        }

        cc.write(str);  //送信番号: S1, ニックネームを送信
        str = cc.readSingleMessage();  //受信番号: R6, ニックネームの登録完了を受信
        System.out.println(str);  //メッセージをコマンドラインに表示
        sc.close();
    }

    private static void receivePlayerInfo(){
        String str = cc.readSingleMessage();  //受信番号: R31, プレイヤーidを受信
        GUIClient.playerId = Integer.parseInt(str);
        GUIClient.nickNames = cc.readMultiMessages(GUIClient.PLAYER);  //受信番号: R8, 全員のニックネームを受信

        for(int p = 0; p < GUIClient.PLAYER; ++p){
            for(int i = 0; i < GUIClient.INSECTNUM; ++i){
                GUIClient.fieldCards[p][i] = 0;
            }
        }

        str = cc.readSingleMessage();  //受信番号: R9, 全員のニックネームの登録・送信完了を受信
        System.out.println(str);  //メッセージをコマンドラインに表示
    }

    private static void receiveHandCards(){
        GUIClient.handCards = cc.readHandCards();
        String str = cc.readSingleMessage();  //受信番号: R11, ゲームの開始を受信
        System.out.println(str);  //メッセージをコマンドラインに表示
        GUIClient.sumOfHandCards = cc.readSumOfHandCards();
    }
}