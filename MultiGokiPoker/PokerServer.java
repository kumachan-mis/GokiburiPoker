import java.net.*;
import java.io.*;
import java.util.*;

public class PokerServer{

    private static int PORT = 8080;
    public static int PLAYER = 5;
    private static ServerSocket s = null;
    private static Socket socket = null;

    public static final int INSECTNUM = 8;
    public static final int EACHCARD = 8;
    public static String[] insects =
    { "コウモリ", "ハエ", "ネズミ", "サソリ", "ゴキブリ", "カエル", "クモ", "カメムシ" };
    private static ArrayList<Integer> cardList = new ArrayList<Integer>(); //Kimura: カードのリスト

    private static int mainPlayerId = -1;
    private static int loserId = -1;

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
            System.out.println("準備完了 : " + s + "  プレイヤーの接続待機中...");
        }

        initializeCardList();
        mainPlayerId = (new Random()).nextInt(PLAYER);
        loserId = -1;

        try{
            for(int n = 0; n < PLAYER;){
                socket = s.accept();
                System.err.println(socket);

                if(socket != null){
                    PokerServerThread th = new PokerServerThread(socket);
                    System.out.println("新しいプレイヤーが参加しました. " + (n + 1) + "/" + PLAYER);
                    th.start();
                    n++;
                }
            }

            try{
                for(int n = 0; n < PLAYER; n++){
                    PokerServerThread.playerThreads[n].join();
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

    public static void setMainPlayerId(int id){
        mainPlayerId = id;
    }

    public static int getMainPlayerId(){
        return mainPlayerId;
    }

    public static void setMLoserId(int id){
        loserId = id;
    }

    public static int getLoserId(){
        return loserId;
    }
}

class PokerServerThread extends Thread{

    public static PokerServerThread[] playerThreads = null;
    private static int ready = 0;
    private static int judged = -1;
    /*
    0はmainPlayerがsenderのカードを外したこと
    1はmainPlayerがsenderのカードを当てたこと
    2はmainPlayerが新しくsenderになること
    -1は1ターンが終了していること
    */

    private static  Integer[] ret = new Integer[3]; //各カード移動での返り値

    private static final String yes = "YES";
    private static final String no = "NO";
    private static final String end = "END";

    private Socket socket = null;
    private BufferedReader reader = null;
    private PrintWriter writer = null;

    private int playerId = -1;
    private String nickName = "Player[" + ready + "]";
    private  boolean canSend = true;
    private boolean isAvailable = true;

    public PokerServerThread(Socket socket) throws IOException{
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

        playerId = ready;
        playerThreads[ready] = this;

        ready++;
        if(ready == PokerServer.PLAYER){
            ready = 0;
        }
    }


    public void run(){
        int turn = 1;
        receiveNickname();
        sendPlayerInfo();
        sendHandCards();
        
        while(PokerServer.getLoserId() == -1){
            writer.println(turn);
            reseivePushCard(ret);

            while(judged == 2){
                writer.println("CardMoves");
                reseiveAction(ret[0], ret[1], ret[2], ret);
            }

            writer.println("NextTurn");

            checkIsLoser();//害虫4枚負け
            if(PokerServer.getLoserId() != -1){
                writer.println("GameSet");
                break;
            }
            writer.println("Continue");

            checkIsLoser();//ノーカード負け
            if(PokerServer.getLoserId() != -1){
                writer.println("GameSet");
                break;
            }
            writer.println("Continue");
            turn++;
        }
        showResult();
    }

    private synchronized void receiveNickname(){
        readSingleMessage(nickName);
        processAC(nickName + " の登録が完了しました");
        synchro();
    }

    private synchronized void sendPlayerInfo(){
        writer.println(PokerServer.PLAYER);
        String[] str = new String[PokerServer.PLAYER];

        for(int i = 0; i < PokerServer.PLAYER; ++i){
            str[i] = playerThreads[i].nickName;
        }
        writeMultiStrings(str, PokerServer.PLAYER);

        processAC("プレイヤーの登録を完了しました");
        synchro();
    }

    private synchronized void sendHandCards(){
        int index = playerId;

        while(index < PokerServer.INSECTNUM * PokerServer.EACHCARD){
            int card = PokerServer.getCardList(index);
            writer.println(card);
            index += PokerServer.PLAYER;
        }
        writer.println(end);

        processAC("カードを配布しました");
        synchro();
    }

    private synchronized void reseivePushCard(Integer[] ret){
        int mainPlayerId = PokerServer.getMainPlayerId();
        int card = -1;
        int say = -1;
        int target = -1;
        String str = null;
        String[] strings = null;

        canSend = (playerId != mainPlayerId);
        synchro();
        
        if(playerId == mainPlayerId){

            writer.println(yes);

            strings = new String[PokerServer.PLAYER];
            for(int i = 0; i < PokerServer.PLAYER; ++i){
                strings[i] = (playerThreads[i].isAvailable && playerThreads[i].canSend)? yes : no;
            }

            writeMultiStrings(strings, PokerServer.PLAYER);
            readSingleMessage(str);
            card = Integer.parseInt(str);
            readSingleMessage(str);
            say = Integer.parseInt(str);
            readSingleMessage(str);
            target = Integer.parseInt(str);
                

            ret[0] = card;
            ret[1] = say;
            ret[2] = PokerServer.getMainPlayerId();
            PokerServer.setMainPlayerId(target);
            judged = 2;

        }else{
            writer.println(no);
            processAC(playerThreads[mainPlayerId].nickName + "が行動しています. しばらくお待ちください.");
        }

        synchro();
        processAC(playerThreads[mainPlayerId].nickName + "が" + PokerServer.insects[say] + "と宣言して" + playerThreads[target].nickName + "にカードを押し付けました");
        synchro();
    }

    private synchronized void reseiveAction(int card, int say, int sender, Integer[] ret){
        int mainPlayerId = PokerServer.getMainPlayerId();
        String str = null;
        String[] strings = null;

        if(playerId == mainPlayerId){

            writer.println(yes);
            canSend = false;
            boolean ecs = existsCanSend();
            writer.println(ecs? yes : no);

            int pass = -1;

            if(ecs){
                readSingleMessage(str);
                pass = str.equals(yes)? 1 : 0;
            }else{
                pass = 0;
            }

            if(pass == 1){
                writer.println(card);
                judged = 2;

                strings = new String[PokerServer.PLAYER];
                for(int i = 0; i < PokerServer.PLAYER; ++i){
                    strings[i] = (playerThreads[i].isAvailable && playerThreads[i].canSend)? yes : no;
                }

                writeMultiStrings(strings, PokerServer.PLAYER);

                int sayAgain = -1, targetAgain = -1;
                readSingleMessage(str);
                sayAgain = Integer.parseInt(str);
                readSingleMessage(str);
                targetAgain = Integer.parseInt(str);

                ret[1] = card;
                ret[2] = sayAgain;
                ret[3] = mainPlayerId;
                PokerServer.setMainPlayerId(targetAgain);

            }else if(pass == 0){
                writer.println(sender);
                writer.println(say);
                readSingleMessage(str);
                String ans = null;
                readSingleMessage(ans);

                if((say == card && ans.equals(yes)) || (say != card && ans.equals(no))){
                    writer.println(yes);
                    judged = 1;
                    processAC("カードを当てました");
                    PokerServer.setMainPlayerId(sender);

                }else{
                    writer.println(no);
                    writer.println(card);
                    judged = 0;
                    processAC("カードを外しました");

                }
            }

            ret[1] = -1;
            ret[2] = -1;
            ret[3] = -1;

        }else if(playerId == sender){
            
            writer.println("SENDER");
            processAC(playerThreads[mainPlayerId].nickName + "が行動しています. しばらくお待ちください.");

            while(true){
                if(judged != -1) break;
            }

            if(judged == 1){
                writer.println(yes);
                writer.println(card);
                processAC(playerThreads[mainPlayerId].nickName + "にカードを当てられてしまいました.");

            }else if(judged == 0){
                writer.println(no);
                processAC(playerThreads[mainPlayerId].nickName + "はカードを外しました.");

            }

        }else{
            writer.println(no);
            processAC(playerThreads[mainPlayerId].nickName + "が行動しています. しばらくお待ちください.");
        }

        synchro();

        if(playerId != mainPlayerId && playerId != sender){

            if(judged == 1){
                processAC(playerThreads[mainPlayerId].nickName + "が" + playerThreads[sender].nickName + "の押し付けたカード " + PokerServer.insects[card] + " を当てました");

            }else if(judged == 0){
                processAC(playerThreads[mainPlayerId].nickName + "が" + playerThreads[sender].nickName + "の押し付けたカードを外しました");
            }

        }

        if(playerId == sender && judged != 2){
            judged = -1;
        }

        synchro();
    }

    private synchronized void checkIsLoser(){
        String str = null;
        readSingleMessage(str);

        if(str.equals(yes)){
            PokerServer.setMLoserId(playerId);
        }
        synchro();
    }

    private synchronized void showResult(){
        processAC(playerThreads[PokerServer.getLoserId()].nickName + "が敗北しました");
        synchro();
    }

    private void processAC(String acMessage){
        System.out.println(nickName + ": " + acMessage);
        writer.println("[サーバ]: " + acMessage);
    }

    private void synchro(){
        ready++;
        try{
            while(ready > 0 && ready < PokerServer.PLAYER){
                wait();
            }
        }catch(InterruptedException e){
            System.err.println(e);
        }
        notifyAll();
        ready = 0;
    }

    private void readSingleMessage(String str){
        str = null;
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
            return;
        }
    }

    private void writeMultiStrings(String[] str, int maxIndex){
        for(int i = 0; i < maxIndex; ++i){
            writer.println(str[i]);
        }
        writer.println(end);
    }
    
    private void connectionError(){
        System.err.println("プレイヤー " + nickName + " との接続が切れました");
        isAvailable = false;
        try{
            socket.close();
        }catch(IOException e){
            System.err.println(e);
        }
    }

    private synchronized boolean existsCanSend(){//Kimura: 送り先があるかどうかのチェック
		for (int i = 0; i < PokerServer.PLAYER; ++i) {
			if (playerThreads[i].isAvailable && playerThreads[i].canSend) {
				return true;
			}
		}
		return false;
	}
}