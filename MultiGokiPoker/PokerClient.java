import java.net.*;
import java.io.*;
import java.util.*;

public class PokerClient{

    private static int PORT = 8080;
    private static InetAddress addr = null;
    private static Socket socket = null;
    private static BufferedReader reader = null;
    private static PrintWriter writer = null;

    private static final String yes = "YES";
    private static final String no = "NO";
    private static final String end = "END";

    private static Scanner sc = null;
    private static int PLAYER = -1;
    public static final String[] insects =
    {"コウモリ", "ハエ", "ネズミ", "サソリ", "ゴキブリ", "カエル", "クモ", "カメムシ"};
    public static final int INSECTNUM = 8;
    public static final int LIMIT = 4;
    private static String[] nickNames = null;

    private static int[] handCards;
    private static int[] fieldCards;

    public static void main(String[] args) throws IOException{
        if(args.length != 2){
            System.err.println("通信するポート番号とローカルホスト名を指定してください");
            System.exit(1);
        }

        PORT = Integer.parseInt(args[0]);
        addr = InetAddress.getByName(args[1]);
        System.out.println("アドレス : " + addr);

        makeInstance();
        beforeStart();
        sendNickname();
        receivePlayerInfo();
        receiveHandCards();

        int turn;
        String turnString = null;

        while(true){

            turnString = readSingleMessage();
            turn = Integer.parseInt(turnString);
            System.out.println("<Turn  " + turn + " >");

            sendPushCard();
            while(true){
                if(isBreak("NextTurn")){
                    break;
                }
                sendAction();
            }

            haveTooManyInsects();
            if(isBreak("GameSet")){
                break;
            }

            haveNoCard();
            if(isBreak("GameSet")){
                break;
            }
        }

        showResult();

        sc.close();
        socket.close();
    }

    private static void makeInstance(){
        sc = new Scanner(System.in);
        handCards = new int[INSECTNUM];
        fieldCards = new int[INSECTNUM];

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

        for(int i = 0; i < INSECTNUM; ++i){
            handCards[i] = 0;
            fieldCards[i] = 0;
        }
    }

    private static void beforeStart(){
        System.out.println("全プレイヤーの準備ができるまでしばらくお待ちください");
        String str = readSingleMessage();
        System.out.println(str);
    }

    private static void sendNickname(){
        boolean correctName = false;
        String str = null;
        while(correctName == false){
            System.out.print("ニックネームを入力してください: ");
            str = sc.next();
            System.out.print("ニックネームが " + str + " でよければ y を入力してください: ");
            String reply = sc.next();
            correctName = correct(reply);
        }

        writer.println(str);
        str = readSingleMessage();
        System.out.println(str);
    }

    private static void receivePlayerInfo(){
        String str = readSingleMessage();
            
        PLAYER = Integer.parseInt(str);
        nickNames = new String[PLAYER];
        nickNames = readMultiMessages(PLAYER);

        str = readSingleMessage();
        System.out.println(str);
    }

    private static void receiveHandCards(){
        String str = null;
        try{
            while(true){
                while(true){
                    str = reader.readLine();
                    if(str != null){
                        break;
                    }
                }
                if(str.equals(end)){
                    break;
                }
                handCards[Integer.parseInt(str)]++;
                str = null;
            }
        }catch(IOException e){
            System.err.println(e);
            System.err.println("サーバ " + addr + " との接続が切れました");
            return;
        }

        str = readSingleMessage();
        System.out.println(str);
    }

    private static void sendPushCard(){
        String str = readSingleMessage();

        if(str.equals(yes)){
            String[] choosables = new String[PLAYER];
            choosables = readMultiMessages(PLAYER);

            System.out.println("押し付ける害虫カード と 宣言する害虫名 と 押し付ける相手 を選びます.");

            int card = chooseInsectCard();
            int say = chooseSayWhat();
            int target = chooseToWhom(choosables);

            handCards[card]--;
            System.out.println("害虫カード: " + insects[card] + " 宣言す害虫名: " + insects[say] + " 押し付ける相手: " + nickNames[target] + " で送信しました");

            writer.println(card);
            writer.println(say);
            writer.println(target);

        }else if(str.equals(no)){

            String str1 = readSingleMessage();
            System.out.println(str1);
        }
        
        str= readSingleMessage();
        System.out.println(str);
    }
    
    private static int chooseInsectCard(){
        int card = -1;
        boolean correctChoice = false;
        String str = null;

        while(correctChoice == false){

            System.out.println("どの害虫カードを押し付けますか？ 今の手札は");
            showHandCards();
            str = sc.next();

            try{
                card = Integer.parseInt(str);
                if(card < 0 || card >= INSECTNUM || handCards[card] <= 0){
                    throw new UnexpectedInputException();
                }

                System.out.print("押し付ける害虫カードが " + insects[card] + " でよければ y を入力してください: ");
                String reply = sc.next();
                correctChoice = correct(reply);

            }catch(NumberFormatException e){
                System.err.println("入力は数字ではありません. やり直してください");
            }catch(UnexpectedInputException e){
                System.err.println("そのカードは持っていないか選択肢にありません. やり直してください");
            }
        }
        return card;
    }

    private static int chooseSayWhat(){
        int say = -1;
        boolean correctChoice = false;
        String str = null;

        while(correctChoice == false){

            System.out.println("今のカードを何と宣言して相手に押し付けますか？");
            showInsects();
            str = sc.next();
            try{
                say = Integer.parseInt(str);
                if(say < 0 || say >= INSECTNUM){
                    throw new UnexpectedInputException();
                }

                System.out.print("宣言する害虫名が " + insects[say] + " でよければ y を入力してください: ");
                String reply = sc.next();
                correctChoice = correct(reply);

            }catch(NumberFormatException e){
                System.err.println("入力は数字ではありません. やり直してください");
            }catch(UnexpectedInputException e){
                System.err.println("それは選択肢にありません. やり直してください");
            }
        }
        return say;
    }

    private static int chooseToWhom(String[] choosables){
        int target = -1;
        boolean correctChoice = false;
        String str = null;

        while(correctChoice == false){

            System.out.println("押し付ける相手は誰にしますか？");
            showChoosablePlayer(choosables);
            str = sc.next();
            try{
                target = Integer.parseInt(str);
                if(target < 0 || target >= INSECTNUM || choosables[target].equals(no)){
                    throw new UnexpectedInputException();
                }

                System.out.print("押し付ける相手が " + nickNames[target] + " でよければ y を入力してください: ");
                String reply = sc.next();
                correctChoice = correct(reply);

            }catch(NumberFormatException e){
                System.err.println("入力は数字ではありません. やり直してください");
            }catch(UnexpectedInputException e){
                System.err.println("そのプレイヤーは選択肢にありません. やり直してください");
            }
        }
        return target;
    }

    private static void sendAction(){
        String str1 = readSingleMessage();

        if(str1.equals(yes)){
            
            String str2 = readSingleMessage();
            int pass = -1;

            System.out.println("カードを押し付けられてしまいました...");

            if(str2.equals(yes)){
                boolean correctChoice = false;

                while(correctChoice == false){
                    System.out.println("(0) カードを当てますか？ それとも (1)カードをみて他のプレイヤーに押し付けますか？");
                    String str = sc.next();
                    try{
                        pass = Integer.parseInt(str);
                        if(pass != 0 && pass != 1){
                            throw new UnexpectedInputException();
                        }
                        if(pass == 0){
                            System.out.println("カードを当てるのでよければ y を入力してください: ");
                        }else if(pass == 1){
                            System.out.println("カードをみて他のプレイヤーに押し付けるのでよければ y を入力してください: ");
                        }

                        String reply = sc.next();
                        correctChoice = correct(reply);
                    }catch(NumberFormatException e){
                        System.err.println("入力は数字ではありません. やり直してください");
                    }catch(UnexpectedInputException e){
                        System.err.println("それは選択肢にありません. やり直してください");
                    }
                }
            }else if(str2.equals(no)){
                pass = 0;
                System.out.println("カードを押し付けられるプレイヤーがいません. カードを当てます.");
            }


            writer.println(pass == 1? yes : no);


            if(pass == 1){
                String str = readSingleMessage();
                int card = Integer.parseInt(str);
                System.out.println("カードは " + insects[card] + " でした");

                String[] choosables = new String[PLAYER];
                choosables = readMultiMessages(PLAYER);

                int sayAgain = chooseSayWhat();
                int targetAgain = chooseToWhom(choosables);
                writer.println(sayAgain);
                writer.println(targetAgain);

            }else if(pass == 0){
                String str3 = readSingleMessage();
                String str4 = readSingleMessage();

                boolean correctChoice = false;
                int guess = -1;

                while(correctChoice == false){
                    System.out.println(nickNames[Integer.parseInt(str3)] + " はこのカードを　" + insects[Integer.parseInt(str4)] + " と宣言しています.");
                    System.out.println("このカードは本当に　" + insects[Integer.parseInt(str4)] + " だと思いますか？");
                    System.out.println("(0) そう思う (1) そう思わない");
                    String str = sc.next();

                    try{
                        guess = Integer.parseInt(str);
                        if(guess != 0 && guess != 1){
                            throw new UnexpectedInputException();
                        }
                        if(guess == 0){
                            System.out.println("そう思う でよければ y を入力してください: ");
                        }else if(guess == 1){
                            System.out.println("そう思わない でよければ y を入力してください: ");
                        }

                        String reply = sc.next();
                        correctChoice = correct(reply);
                    }catch(NumberFormatException e){
                        System.err.println("入力は数字ではありません. やり直してください");
                    }catch(UnexpectedInputException e){
                        System.err.println("そのプレイヤーは選択肢にありません. やり直してください");
                    }
                }

                writer.println(guess == 0? yes : no);
                str3 = readSingleMessage();

                if(str3.equals(no)){
                    str4 = readSingleMessage();
                    fieldCards[Integer.parseInt(str4)]++;
                }

                str3 = readSingleMessage();
                System.out.println(str3);

            }
        }else if(str1.equals("SENDER")){
            String str2 = readSingleMessage();
            System.out.println(str2);

            str2 = readSingleMessage();

            String str3 = null;
            if(str2.equals(yes)){
                str3 = readSingleMessage();
                fieldCards[Integer.parseInt(str3)]++;
            }

            str2 = readSingleMessage();
            System.out.println(str2);

        }else if(str1.equals(no)){
            String str2 = readSingleMessage();
            System.out.println(str2);

            str2 = readSingleMessage();
            System.out.println(str2);
        }
    }

    private static void haveTooManyInsects(){
        boolean match = false;
        
        for(int i = 0; i < INSECTNUM; ++i){
            if(fieldCards[i] >= LIMIT){ //Kimura: そのプレイヤーの現在追加された種類のカードが4枚以上になったら
                match = true;
                break;
            }
        }

        System.out.println("今まで押し付けられてしまった害虫カードは");
        showFieldCards();
        System.out.println("です. 確認できたら y を押してください.");

        boolean check = false;
        while(check == false){
            String reply = sc.next();
            check = correct(reply);
        }

        writer.println(match? yes : no);
    }

    private static void haveNoCard(){
        boolean match = true;

        for(int i = 0; i < INSECTNUM; ++i){
            if(handCards[i] > 0){
                match = false; //Kimura: 手札がある時
                break;
            }
        }
        System.out.println("今の手札は");
        showHandCards();
        System.out.println("です. 確認できたら y を押してください.");

        boolean check = false;
        while(check == false){
            String reply = sc.next();
            check = correct(reply);
        }

        writer.println(match? yes : no);
    }

    private static void showResult(){
        String result = readSingleMessage();
        System.out.println(result);
    }

    private static void showHandCards() {//Kimura : 手持ちのカードを可視化
		for(int i = 0; i < INSECTNUM; i++){
			System.out.println("(" + i + ")" + insects[i] + ":" + handCards[i] + "枚");
		}
    }

    private static void showFieldCards() {//Kimura : 場のカードを可視化
		for(int i = 0; i < INSECTNUM; i++){
			System.out.println("(" + i + ")" + insects[i] + ":" + fieldCards[i] + "枚");
		}
    }

    private static void showInsects() {
		for(int i = 0; i < INSECTNUM; i++){
			System.out.println("(" + i + ")" + insects[i] + " ");
		}
    }

    private static void showChoosablePlayer(String[] choosables){
        for (int i = 0; i < PLAYER; i++) {
            if(choosables[i].equals(yes)){
                System.out.print("(" + i + ")" + nickNames[i] + " ");
            }
		}
		System.out.println();
    }

    public static boolean correct(String reply){
        String lowercase = reply.toLowerCase();
        return lowercase.equals("yes") || lowercase.equals("y");
    }

    private static String readSingleMessage(){
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
            System.err.println("サーバ " + addr + " との接続が切れました");
            return null;
        }
        return str;
    }

    private static String[] readMultiMessages(int size){
        String[] strings = new String[size];
        String str = null;
        int i = 0;
        try{
            while(true){
                while(true){
                    str = reader.readLine();
                    if(str != null){
                        break;
                    }
                }

                if(str.equals(end)){
                    break;
                }
                strings[i++] = str;
                str = null;
            }
        }catch(IOException e){
            System.err.println(e);
            System.err.println("サーバ " + addr + " との接続が切れました");
            return null;
        }

        return strings;
    }
    private static boolean isBreak(String breakMessage){
        String str = null;
        try{
            while(true){
                str = reader.readLine();
                if(str != null){
                    break;
                }
            }

            if(str.equals(breakMessage)){
                return true;
            }else{
                return false;
            }

        }catch(IOException e){
            System.err.println(e);
            System.err.println("サーバ " + addr + " との接続が切れました");
            return false;
        }
    }
}