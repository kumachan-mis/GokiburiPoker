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
    private static int senderId = -1;
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
            System.out.println("準備完了 : " + s);
            System.out.println("プレイヤーの接続待機中...");
        }

        initializeCardList();
        mainPlayerId = (new Random()).nextInt(PLAYER);
        loserId = -1;

        try{
            for(int n = 0; n < PLAYER;){
                socket = s.accept();
                System.err.println(socket);

                if(socket != null){
                    PokerServerThread th = new PokerServerThread(socket, n);
                    System.out.println("新しいプレイヤーが参加しました. " + (n + 1) + "/" + PLAYER);
                    th.start();
                    n++;
                }
            }
            System.out.println("プレイヤーの募集を終了します.");

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

    public static void setSenderId(int id){
        senderId = id;
    }

    public static int getSenderId(){
        return senderId;
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
    private static ThreadsSynchro synchro = ThreadsSynchro.getInstance();
    
    private static  Integer[] ret = new Integer[2]; //各カード移動での返り値

    private static final String yes = "YES";
    private static final String no = "NO";
    private static final String end = "END";

    private Socket socket = null;
    private BufferedReader reader = null;
    private PrintWriter writer = null;

    private int playerId = -1;
    private String nickName;
    private  boolean canSend = true;
    private boolean isAvailable = true;

    public PokerServerThread(Socket socket, int index) throws IOException{
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

        playerId = index;
        playerThreads[playerId] = this;
        nickName = "Player[" + playerId + "]";
    }


    public void run(){
        int turn = 1;

        synchro.synchro();
        processAC("全プレイヤーの接続を確認しました");
        receiveNickname();
        sendPlayerInfo();
        sendHandCards();
        
        while(PokerServer.getLoserId() == -1){
            writer.println(turn);
            reseivePushCard(ret);

            while(synchro.getJunged() == 2){
                writer.println("CardMoves");
                reseiveAction(ret[0], ret[1], ret);
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

    private void receiveNickname(){
        nickName = readSingleMessage();
        processAC(nickName + " の登録が完了しました");
        synchro.synchro();
    }

    private void sendPlayerInfo(){
        writer.println(PokerServer.PLAYER);
        String[] str = new String[PokerServer.PLAYER];

        for(int i = 0; i < PokerServer.PLAYER; ++i){
            str[i] = playerThreads[i].nickName;
        }
        writeMultiStrings(str, PokerServer.PLAYER);
        processAC("プレイヤーの登録を完了しました");
    }

    private void sendHandCards(){
        int index = playerId;

        while(index < PokerServer.INSECTNUM * PokerServer.EACHCARD){
            int card = PokerServer.getCardList(index);
            writer.println(card);
            index += PokerServer.PLAYER;
        }
        writer.println(end);
        processAC("カードを配布しました");
    }

    private void reseivePushCard(Integer[] ret){
        int mainPlayerId = PokerServer.getMainPlayerId();
        int card = -1;
        int say = -1;
        int target = -1;
        String str = null;
        String[] strings = null;

        canSend = (playerId != mainPlayerId);
        synchro.synchro();
        
        if(playerId == mainPlayerId){

            writer.println(yes);

            strings = new String[PokerServer.PLAYER];
            for(int i = 0; i < PokerServer.PLAYER; ++i){
                strings[i] = (playerThreads[i].isAvailable && playerThreads[i].canSend)? yes : no;
            }

            writeMultiStrings(strings, PokerServer.PLAYER);
            str = readSingleMessage();
            card = Integer.parseInt(str);
            str = readSingleMessage();
            say = Integer.parseInt(str);
            str = readSingleMessage();
            target = Integer.parseInt(str);
                

            ret[0] = card;
            ret[1] = say;
            PokerServer.setSenderId(mainPlayerId);
            PokerServer.setMainPlayerId(target);
            synchro.setJunged(2);

        }else{
            writer.println(no);
            processAC(playerThreads[mainPlayerId].nickName + "が行動しています. しばらくお待ちください.");
        }

        synchro.synchro();

        target = PokerServer.getMainPlayerId();
        say = ret[1];
        processAC(playerThreads[mainPlayerId].nickName + "が" + PokerServer.insects[say] + "と宣言して" + playerThreads[target].nickName + "にカードを押し付けました");
    }

    private void reseiveAction(int card, int say, Integer[] ret){
        int mainPlayerId = PokerServer.getMainPlayerId();
        int senderId = PokerServer.getSenderId();
        String str = null;
        String[] strings = null;

        if(playerId == mainPlayerId){

            writer.println(yes);
            canSend = false;
            boolean ecs = existsCanSend();
            writer.println(ecs? yes : no);

            int pass = -1;

            if(ecs){
                str = readSingleMessage();
                pass = str.equals(yes)? 1 : 0;
            }else{
                pass = 0;
            }

            if(pass == 1){
                writer.println(card);
                synchro.setJunged(2);

                strings = new String[PokerServer.PLAYER];
                for(int i = 0; i < PokerServer.PLAYER; ++i){
                    strings[i] = (playerThreads[i].isAvailable && playerThreads[i].canSend)? yes : no;
                }

                writeMultiStrings(strings, PokerServer.PLAYER);

                int sayAgain = -1, targetAgain = -1;
                str = readSingleMessage();
                sayAgain = Integer.parseInt(str);
                str = readSingleMessage();
                targetAgain = Integer.parseInt(str);

                ret[0] = card;
                ret[1] = sayAgain;
                PokerServer.setSenderId(mainPlayerId);
                PokerServer.setMainPlayerId(targetAgain);

            }else if(pass == 0){
                writer.println(senderId);
                writer.println(say);
                str = readSingleMessage();
                String ans = readSingleMessage();

                if((say == card && ans.equals(yes)) || (say != card && ans.equals(no))){
                    writer.println(yes);
                    synchro.setJunged(1);
                    processAC("カードを当てました");
                    PokerServer.setMainPlayerId(senderId);

                }else{
                    writer.println(no);
                    writer.println(card);
                    synchro.setJunged(0);
                    processAC("カードを外しました");

                }
            }

            ret[0] = -1;
            ret[1] = -1;

        }else if(playerId == senderId){
            
            writer.println("SENDER");
            processAC(playerThreads[mainPlayerId].nickName + "が行動しています. しばらくお待ちください.");

            while(true){
                if(synchro.getJunged() != -1) break;
            }

            if(synchro.getJunged() == 1){
                writer.println(yes);
                writer.println(card);
                processAC(playerThreads[mainPlayerId].nickName + "にカードを当てられてしまいました.");

            }else if(synchro.getJunged() == 0){
                writer.println(no);
                processAC(playerThreads[mainPlayerId].nickName + "はカードを外しました.");

            }

        }else{
            writer.println(no);
            processAC(playerThreads[mainPlayerId].nickName + "が行動しています. しばらくお待ちください.");
        }

        synchro.synchro();

        if(playerId != mainPlayerId && playerId != senderId){

            if(synchro.getJunged() == 1){
                processAC(playerThreads[mainPlayerId].nickName + "が" + playerThreads[senderId].nickName + "の押し付けたカード " + PokerServer.insects[card] + " を当てました");

            }else if(synchro.getJunged() == 0){
                processAC(playerThreads[mainPlayerId].nickName + "が" + playerThreads[senderId].nickName + "の押し付けたカードを外しました");
            }

        }

        if(playerId == senderId && synchro.getJunged() != 2){
            synchro.setJunged(-1);
        }
    }

    private void checkIsLoser(){
        String str = readSingleMessage();

        if(str.equals(yes)){
            PokerServer.setMLoserId(playerId);
        }
        synchro.synchro();
    }

    private void showResult(){
        processAC(playerThreads[PokerServer.getLoserId()].nickName + "が敗北しました");
    }

    private void processAC(String acMessage){
        System.out.println(nickName + ": " + acMessage);
        writer.println("[サーバ]: " + acMessage);
    }

    
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
        synchro.addError();
        synchro.synchro();

        try{
            socket.close();
        }catch(IOException e){
            System.err.println(e);
        }
    }

    private boolean existsCanSend(){//Kimura: 送り先があるかどうかのチェック
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

    private int error;
    private int judged;
    boolean in = false, out = true;
    private int waitnum;

    private static ThreadsSynchro instance = new ThreadsSynchro();
    public static ThreadsSynchro getInstance() {
        return instance;
    }

    private ThreadsSynchro(){
        error = 0;
        judged = -1;
        waitnum = 0;
    }

    public synchronized void addError(){
        error++;
    }

    public synchronized int getError(){
        return error;
    }

    public synchronized void setJunged(int judged){
        this.judged = judged;
    }

    public synchronized int getJunged(){
        return judged;
    }

    private synchronized void in(){
        try{
            System.out.println("player : " + PokerServer.PLAYER);
            if(waitnum == PokerServer.PLAYER - error - 1){
                waitnum++;
                out = false;
                in= true;
                notifyAll();
                System.err.println("notifiy: " + waitnum);
            }

            while(in == false){
                waitnum++;
                System.err.println("wait: " + waitnum);
                wait();
            }
        }catch(InterruptedException e){
            System.err.println(e);
        }
    }

    private synchronized void out(){
        try{
            System.out.println("player : " + PokerServer.PLAYER);
            if(waitnum == 1){
                waitnum--;
                in = false;
                out = true;
                notifyAll();
                System.err.println("notifiy: " + waitnum);
            }

            while(out == false){
                waitnum--;
                System.err.println("wait: " + waitnum);
                wait();
            }
        }catch(InterruptedException e){
            System.err.println(e);
        }
    }

    public void synchro(){
        in();
        out();
    }
}