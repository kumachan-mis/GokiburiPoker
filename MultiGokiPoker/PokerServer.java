/*
各受信番号・送信番号は
PokerClient.javaの各送信番号・受信番号に対応しています.
-は条件による送信文字列の分岐を意味します.
*/

import java.net.*;
import java.io.*;
import java.util.*;

public class PokerServer{

    private static int PORT = 8080;
    public static int PLAYER = 5;
    private static ServerSocket s = null;
    private static Socket socket = null;

    public static final String[] insects =
    {"コウモリ", "ハエ", "ネズミ", "サソリ", "ゴキブリ", "カエル", "クモ", "カメムシ"};
    public static final int INSECTNUM = insects.length; //カードの種類の数
    public static final int EACHCARD = 8;  //各害虫カードの枚数

    private static ArrayList<Integer> cardList = new ArrayList<Integer>(); //Kimura: カードのリスト

    public static void main(String[] args) throws IOException{

        if(args.length != 2){
            System.err.println("通信するポート番号とプレイヤーの人数を指定してください");
            System.exit(1);
        }

        PORT = Integer.parseInt(args[0]);
        PLAYER = Integer.parseInt(args[1]);

        try{
            s = new ServerSocket(PORT);
        }catch(IOException e){
            System.err.println(e);
        }finally{
            System.out.println("準備完了 : " + s);
            System.out.println("プレイヤーの接続待機中...");
        }

        initializeCardList();

        try{
            for(int n = 0; n < PLAYER;){
                socket = s.accept();
                System.err.println(socket);

                if(socket != null){
                    PokerServerThread th = new PokerServerThread(socket, n);  //各クライアントと通信を行うスレッドの生成
                    System.out.println("新しいプレイヤーが参加しました. " + (n + 1) + "/" + PLAYER);
                    th.start();
                    n++;
                }
            }
            System.out.println("プレイヤーの募集を終了します.");

            try{
                for(int n = 0; n < PLAYER; n++){
                    PokerServerThread.playerThreads[n].join();  //先にメインだけ終わってしまっては困る
                }
            }catch(InterruptedException e){
                System.err.println(e);
            }finally{
                System.out.println("ゲームを終了します.");
            }

        }catch(IOException e){
            System.err.println(e);
        }finally{
            socket.close();
            s.close();
        }

    }

    private static void initializeCardList(){
        for (int i = 0; i < EACHCARD; i++) {
			for (int j = 0; j < PokerServer.INSECTNUM; j++) {
				cardList.add(j);//Kimura: リストに64個の数を入力
			}
		}
        Collections.shuffle(cardList);//Kimura: リストをシャッフル
    }

    public static int getCardList(int index){
        return cardList.get(index);
    }
}

class PokerServerThread extends Thread{

    public static PokerServerThread[] playerThreads = null; //全クライアント用のスレッドの配列
    private static ThreadsSynchro synchro = ThreadsSynchro.getInstance();

    private static final String yes = "YES";
    private static final String no = "NO";
    private static final String end = "END";

    private Socket socket = null;
    private BufferedReader reader = null;
    private PrintWriter writer = null;

    private int playerId = -1;
    private String nickName;
    private  boolean canSend = true;  //カードを押し付ける対象にできるかどうか
    private boolean isAvailable = true;  //通信が途切れていないかどうか. trueは繋がっている.

    public PokerServerThread(Socket socket, int id) throws IOException{
        if(playerThreads == null){
            playerThreads = new PokerServerThread[PokerServer.PLAYER];
        }
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

        playerId = id;
        playerThreads[playerId] = this;
        nickName = "Player[" + playerId + "]";  //デフォルトのニックネーム

        if(playerId == 0){
            synchro.setMainPlayerId((new Random()).nextInt(PokerServer.PLAYER));
        }
    }  //各種初期化


    public void run(){
        int turn = 1;
        synchro.synchro();  //全員が接続できるまで待機, 同期
        processAC("全プレイヤーの接続を確認しました");  //送信番号: S1, 全員の接続を通知

        receiveNickname();
        sendPlayerInfo();
        sendHandCards();
        
        while(true){
            writer.println(turn);  //送信番号: S2, ターン数を通知
            reseivePushCard();

            while(true){
                int judged = synchro.getJudged();
                synchro.synchro();
                if(judged != 2){
                    break;
                }  //カードの言い当てが行われたらこのターン終了
                writer.println("CardMoves");  //送信番号: S3-1, カードのたらい回しを通知

                int[] ret = new int[2];
                ret = synchro.getRet();
                synchro.synchro();
                reseiveAction(ret[0], ret[1]);
            }

            writer.println("NextTurn");  //送信番号: S3-2, カードの言い当てが行われたことを通知

            checkIsLoser();  //害虫LIMIT枚負け
            int loserId = synchro.getLoserId();
            synchro.synchro();
            if(loserId != -1){
                writer.println("GameSet");  //送信番号: S4-1, ゲーム終了を通知
                break;
            }
            writer.println("Continue");  //送信番号: S4-2, ゲーム継続を通知

            checkIsLoser();  //ノーカード負け
            loserId = synchro.getLoserId();
            synchro.synchro();
            if(loserId != -1){
                writer.println("GameSet");  //送信番号: S5-1, ゲーム終了を通知
                break;
            }
            writer.println("Continue");  //送信番号: S5-2, ゲーム継続を通知
            turn++;
        }
        showResult();  //結果の表示
    }

    private void receiveNickname(){
        nickName = readSingleMessage();  //受信番号: R1, ニックネームを受信
        processAC(nickName + " の登録が完了しました");  //送信番号: S6, ニックネームの登録完了を送信
        synchro.synchro();
    }

    private void sendPlayerInfo(){
        writer.println(PokerServer.PLAYER);  //送信番号: S7, プレイヤーの人数を送信
        String[] str = new String[PokerServer.PLAYER];

        writer.println(playerId);  //送信番号: S31, プレイヤーidを送信
        for(int i = 0; i < PokerServer.PLAYER; ++i){
            str[i] = playerThreads[i].nickName;
        }
        writeMultiStrings(str, PokerServer.PLAYER);  //送信番号: S8, 全員のニックネームを送信
        processAC("プレイヤーの登録を完了しました");  //送信番号: S9, 全員のニックネームの登録・送信完了を通知
    }

    private void sendHandCards(){
        int index = playerId;

        while(index < PokerServer.INSECTNUM * PokerServer.EACHCARD){
            int card = PokerServer.getCardList(index);
            writer.println(card);
            index += PokerServer.PLAYER;
        }
        writer.println(end);  //送信番号: S10, 手持ちのカードを送信
        processAC("カードを配布しました");  //送信番号: S11, カード配布の完了を通知
    }

    private void reseivePushCard(){
        int[] ret = new int[2];
        int mainPlayerId = synchro.getMainPlayerId();
        int card = -1;
        int say = -1;
        int target = -1;
        String str = null;
        String[] strings = null;

        canSend = (playerId != mainPlayerId);  //初期段階でカードを押し付ける対象にならないのは自分だけ
        synchro.synchro();

        if(playerId == mainPlayerId){
            writer.println(yes);
        }else{
            writer.println(no);
        }  //送信番号: S12, 各クライアントの役割を送信

        if(playerId == mainPlayerId){  //メインプレイヤーのとき

            strings = new String[PokerServer.PLAYER];

            for(int i = 0; i < PokerServer.PLAYER; ++i){
                strings[i] = (playerThreads[i].isAvailable && playerThreads[i].canSend)? yes : no;
                //カードを押し付けられるのはまだカードを押し付けていないプレイヤー, かつ通信が途切れていない人プレイヤー
            }

            writeMultiStrings(strings, PokerServer.PLAYER);  //送信番号: S13, カードを押し付ける対象かどうかを各プレイヤーについて送信
            str = readSingleMessage();  //受信番号: R2, 押し付けるカードを受信
            card = Integer.parseInt(str);
            str = readSingleMessage();  //受信番号: R3, 宣言を受信
            say = Integer.parseInt(str);
            str = readSingleMessage();  //受信番号: R4, 押し付ける相手を受信
            target = Integer.parseInt(str);
                

            ret[0] = card;
            ret[1] = say;
            synchro.setRet(ret);

            synchro.setSenderId(mainPlayerId);  //直前に押し付けたプレイヤーを自分に
            synchro.setMainPlayerId(target);  //次のメインプレイヤーを押し付けた相手に
            synchro.setJudged(2);  //一番最初は必ずカードをたらい回し

        }else{  //メインプレイヤーでないとき
            processAC(playerThreads[mainPlayerId].nickName + "が行動しています. しばらくお待ちください.");  //送信番号: S14, クライアントに待機を通知
        }

        synchro.synchro();

        ret = synchro.getRet();
        say = ret[1];
        target = synchro.getMainPlayerId();
        processAC(playerThreads[mainPlayerId].nickName + "が" + PokerServer.insects[say] + "と宣言して" + playerThreads[target].nickName + "にカードを押し付けました");  //送信番号: S15, メインプレイヤーの行動結果を通知
    }

    private void reseiveAction(int card, int say){
        int mainPlayerId = synchro.getMainPlayerId();
        int senderId = synchro.getSenderId();
        String str = null;
        String[] strings = null;

        if(playerId == mainPlayerId){
            writer.println(yes);
        }else if(playerId == senderId){
            writer.println("SENDER");
        }else{
            writer.println(no);
        }  //送信番号: S16, 各クライアントの役割を送信
        
        if(playerId == mainPlayerId){  //メインプレイヤーの時
            canSend = false; //押し付ける対象から自分を除外
            boolean ecs = existsCanSend(); //押し付ける対象がまだいるか
            writer.println(ecs? yes : no);  //送信番号: S17, 押し付ける対象がまだいるかを送信

            int pass = -1; //押し付ける場合は1, 自分で判定する場合は0

            if(ecs){  //押し付ける対象がまだいる場合
                str = readSingleMessage();  //受信番号: R5, 押し付けるか自分で当てるかを受信
                pass = str.equals(yes)? 1 : 0;

            }else{  //押し付ける対象がもういない場合
                pass = 0;  //たらい回しはできない
            }

            if(pass == 1){  //他の人に押し付ける場合
                writer.println(card);  //送信番号: S18, 押し付けられたカードがなんであったかを送信

                strings = new String[PokerServer.PLAYER];
                for(int i = 0; i < PokerServer.PLAYER; ++i){
                    strings[i] = (playerThreads[i].isAvailable && playerThreads[i].canSend)? yes : no;
                    //カードを押し付けられるのはまだカードを押し付けていないプレイヤー, かつ通信が途切れていない人プレイヤー
                }

                writeMultiStrings(strings, PokerServer.PLAYER);  //送信番号: S19, カードを押し付ける対象かどうかを各プレイヤーについて送信

                int sayAgain = -1;
                int targetAgain = -1;
                str = readSingleMessage();  //受信番号: R6, 宣言を受信
                sayAgain = Integer.parseInt(str);
                str = readSingleMessage();  //受信番号: R7, 押し付ける相手を受信
                targetAgain = Integer.parseInt(str);

                int[] ret = new int[2];
                ret[0] = card;
                ret[1] = sayAgain;
                synchro.setRet(ret);

                synchro.setSenderId(mainPlayerId);  //直前に押し付けたプレイヤーを自分に
                synchro.setMainPlayerId(targetAgain); //メインプレイヤーを押し付けた相手に (*)
                synchro.setJudged(2);  //状態を「たらい回し」にする

            }else if(pass == 0){  //自分で当てる場合
                writer.println(senderId);  //送信番号: S20, 押し付けてきた相手を送信
                writer.println(say);  //送信番号: S21, 相手の宣言を送信
                String guess = readSingleMessage();  //受信番号: R8, 予想を受信

                if((say == card && guess.equals(yes)) || (say != card && guess.equals(no))){  //カードが宣言と一緒かどうか当てたら
                    writer.println(yes);  //送信番号: S22-1, 正解を通知
                    writer.println(card);  //送信番号: S23-1, 正解を送信
                    synchro.setJudged(1);  //状態を「正解」にする.
                    processAC("カードを当てました. 本当のカードは " + PokerServer.insects[card] + " です.");  //送信番号: S24-1, 正解時の表示用メッセージを送信
                    synchro.setMainPlayerId(senderId);

                }else{
                    writer.println(no);  //送信番号: S22-2, 不正解を通知
                    writer.println(card);  //送信番号: S23-2, 本当の正解を送信
                    synchro.setJudged(0);  //状態を「不正解」にする
                    processAC("カードを外しました. 本当のカードは " + PokerServer.insects[card] + " です.");  //送信番号: S24-2, 不正解時の表示用メッセージを送信

                }
            }

        }else if(playerId == senderId){  //直前に押し付けたプレイヤーの時
            synchro.setJudged(-1);  //状態を「不定」にする
            processAC(playerThreads[mainPlayerId].nickName + " が行動しています. しばらくお待ちください.");  //送信番号: S25, 待機の指示を通知

            int judged = -1;

            while(true){
                judged = synchro.getJudged();
                if(judged != -1) break;
            }  //メインプレイヤーが状態を「不定」から変更するまで待機

            if(judged == 1){  //状態が「正解」だったら
                writer.println(yes);  //送信番号: S26-1, 当てられてしまったことを通知
                writer.println(card);  //送信番号: S27,  当てられたカードを送信
                processAC(playerThreads[mainPlayerId].nickName + "にカードを当てられてしまいました.");  //送信番号: S28-1, 正解時の表示用のメッセージを送信

            }else if(judged == 0){  //状態が「不正解」だったら
                writer.println(no);  //送信番号: S26-2, 外れたことを通知
                writer.println(card);  //送信番号: S34,  外してくれたカードを送信
                writer.println(mainPlayerId);  //送信番号: S35, 押し付けた相手を送信
                processAC(playerThreads[mainPlayerId].nickName + "はカードを外しました.");  //送信番号: S28-2, 不正解時の表示用のメッセージを送信

            }else{
                writer.println("PASSED");  //送信番号: S26-3, たらい回しになったことを通知
                int target = synchro.getMainPlayerId();  //(*)で更新された次のメインプレイヤーを取得
                int[] ret = new int[2];
                ret = synchro.getRet();
                processAC(playerThreads[mainPlayerId].nickName + " はカードを改めて " + PokerServer.insects[ret[1]] + " と宣言して " + playerThreads[target].nickName + " に押し付けました");  //送信番号: S28-3, たらい回し時の表示用のメッセージを送信
            }

        }else{  //何事もないプレイヤーのとき
            processAC(playerThreads[mainPlayerId].nickName + " が行動しています. しばらくお待ちください.");   //送信番号: S29, 待機の指示を通知
        }

        synchro.synchro();  //同期点(**)

        if(playerId != mainPlayerId && playerId != senderId){   //何事もないプレイヤーのとき
            int judged = synchro.getJudged();

            writer.println(card);  //送信番号: S32, 今メインになっているカードを通知

            if(judged == 1){
                writer.println(senderId);  //送信番号: S33-1 場のカードが増えてしまった、直前にカードを押し付けた人のidを通知
                processAC(playerThreads[mainPlayerId].nickName + " が " + playerThreads[senderId].nickName + " の押し付けたカード " + PokerServer.insects[card] + " を当てました");
                //送信番号: S30-1, カードの判定が正解であることを通知

            }else if(judged == 0){
                writer.println(mainPlayerId);  //送信番号: S33-2 場のカードが増えてしまった、メインプレイヤーのidを通知
                processAC(playerThreads[mainPlayerId].nickName + " が " + playerThreads[senderId].nickName + " の押し付けたカード " + PokerServer.insects[card] + " を外しました");
                //送信番号: S30-2, カードの判定が正解であることを通知

            }else{
                writer.println(-1);  //送信番号: S33-3, 場にカードがたまらなかったことを通知
                int target = synchro.getMainPlayerId();  //(*)で更新された次のメインプレイヤーを取得
                int[] ret = new int[2];
                ret = synchro.getRet();
                processAC(playerThreads[mainPlayerId].nickName + " が"  + playerThreads[senderId].nickName + " の押し付けたカードを " + PokerServer.insects[ret[1]] + " と宣言して " + playerThreads[target].nickName + " に押し付けました");
                //送信番号: S30-3, たらい回しであることを通知

            }//同期点(**)でメインプレイヤーが状態を変えているから「不定」はありえない(はず)
        }

        if(synchro.getJudged() != 2){
            synchro.setJudged(-1);  //「たらい回し」でなければ状態を「不定」に
        }
        //System.err.println("actionEnd: " + nickName);
        synchro.synchro();
    }

    private void checkIsLoser(){
        String str = readSingleMessage();  //受信番号R9: 敗者かどうかを受信. 敗者が同時に複数出ることはない(はず)

        if(str.equals(yes)){
            synchro.setLoserId(playerId);  
        }
        synchro.synchro();
    }

    private void showResult(){
        processAC(playerThreads[synchro.getLoserId()].nickName + "が敗北しました");
    }

    private void processAC(String acMessage){
        System.out.println(nickName + ": " + acMessage);
        writer.println("[サーバ]: " + acMessage);
    }  //クライアントへの通知とサーバーのログ

    
    private String readSingleMessage(){
        String str = null;
        try{
            while(true){
                str = reader.readLine();
                if(str != null){
                    break;
                }
            }
        }catch(IOException e){
            System.err.println(e);
            connectionError();
            return null;
        }
        return str;
    }  //単一データ受信

    private void writeMultiStrings(String[] str, int maxIndex){
        for(int i = 0; i < maxIndex; ++i){
            writer.println(str[i]);
        }
        writer.println(end);
    }  //複数データ送信
    
    private void connectionError(){
        System.err.println("プレイヤー " + nickName + " との接続が切れました");
        isAvailable = false;
        synchro.addError();
        synchro.synchro();

        try{
            socket.close();
        }catch(IOException e){
            System.err.println(e);
        }
    }  //通信が途切れたとき

    private boolean existsCanSend(){  //Kimura: 送り先があるかどうかのチェック
		for (int i = 0; i < PokerServer.PLAYER; ++i) {
			if (playerThreads[i].isAvailable && playerThreads[i].canSend) {
				return true;
			}
		}
		return false;
    }

    public boolean getIsAvailable(){
        return isAvailable;
    }
}

class ThreadsSynchro{

    private int mainPlayerId;  //メインプレイヤーのid
    private int senderId;      //直前にカードを押し付けたプレイヤーのid
    private int loserId;       //敗者のid

    private int error;  //通信が途切れた人数
    private int judged;  //-1: 不定, 0: 不正解, 1:正解, 2:たらい回し
    private int[] ret = new int[2]; //本当のカードと宣言を格納
    private boolean in = false;
    private boolean out = true;
    private int waitnum;  //待機している人数

    private static ThreadsSynchro instance = new ThreadsSynchro();
    public static ThreadsSynchro getInstance() {
        return instance;
    }

    private ThreadsSynchro(){
        mainPlayerId = -1;
        senderId = -1;
        loserId = -1;

        error = 0;
        judged = -1;
        waitnum = 0;
    }

    public synchronized void setMainPlayerId(int id){
        mainPlayerId = id;
    }

    public synchronized int getMainPlayerId(){
        return mainPlayerId;
    }

    public synchronized void setSenderId(int id){
        senderId = id;
    }

    public synchronized  int getSenderId(){
        return senderId;
    }

    public synchronized void setLoserId(int id){
        loserId = id;
    }

    public synchronized int getLoserId(){
        return loserId;
    }

    public synchronized void addError(){
        error++;
    }

    public synchronized int getError(){
        return error;
    }

    public synchronized void setJudged(int judged){
        this.judged = judged;
    }

    public synchronized int getJudged(){
        return judged;
    }

    public synchronized int[] getRet(){
        return ret;
    }

    public synchronized void setRet(int[] ret){
        this.ret = ret;
    }

    private synchronized void in(){
        try{
            if(waitnum == PokerServer.PLAYER - error - 1){
                waitnum++;
                out = false;
                in= true;
                notifyAll();
                //System.err.println("notifiy: " + waitnum);
            }

            while(in == false){
                waitnum++;
                //System.err.println("wait: " + waitnum);
                wait();
            }
        }catch(InterruptedException e){
            System.err.println(e);
        }
    }

    private synchronized void out(){
        try{
            //System.out.println("player : " + PokerServer.PLAYER);
            if(waitnum == 1){
                waitnum--;
                in = false;
                out = true;
                notifyAll();
                //System.err.println("notifiy: " + waitnum);
            }

            while(out == false){
                waitnum--;
                //System.err.println("wait: " + waitnum);
                wait();
            }
        }catch(InterruptedException e){
            System.err.println(e);
        }
    }

    public void synchro(){
        in();
        out();
        //System.out.println("synchronized");
    }  //同期処理
}