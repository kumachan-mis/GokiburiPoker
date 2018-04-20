/*
各受信番号・送信番号は
PokerServer.javaの各送信番号・受信番号に対応しています.
-は条件による送信文字列の分岐を意味します.
*/

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
    private static int PLAYER = -1;  //プレイヤーの人数
    public static final String[] insects =
    {"コウモリ", "ハエ", "ネズミ", "サソリ", "ゴキブリ", "カエル", "クモ", "カメムシ"};
    public static final int INSECTNUM = insects.length; //カードの種類の数

    public static final int LIMIT = 4;  //場にたまっても良い各害虫カードの枚数
    private static String[] nickNames = null;
    private static int phase = 0;
    private static int playerId;
    private static int[] handCards;  //手持ちのカードの枚数
    private static int[][] fieldCards;  //場のカードの枚数

    public static void main(String[] args) throws IOException{
        if(args.length != 2){
            System.err.println("通信する　ポート番号 と ローカルホスト名　を指定してください");
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

        String turnString = null;

        while(true){

            turnString = readSingleMessage();  //受信番号: R2, ターン数を受信
            System.out.println();
            System.out.println("<Turn " + turnString + ">");

            sendPushCard();
            while(true){
                boolean b = isBreak("NextTurn");  //受信番号: R3, カードの言い当てが行われたかどうかを受信
                if(b){
                    break;
                }  //カードの言い当てが行われたらこのターン終了
                sendAction();
            }

            haveTooManyInsects();  //害虫LIMIT枚負け判定
            boolean b = isBreak("GameSet");  //受信番号: R4, ゲーム終了かどうかを受信
            if(b){
                break;
            }

            haveNoCard();  //ノーカード負け判定
            b = isBreak("GameSet");
            if(b){  //受信番号: R4, ゲーム終了通知されたかどうか
                break;
            }
        }

        showResult();  //結果の表示

        sc.close();
        socket.close();
    }

    private static void makeInstance(){
        sc = new Scanner(System.in);
        handCards = new int[INSECTNUM];

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
        }
    }  //各種初期化

    private static void beforeStart(){
        System.out.println("全プレイヤーの準備ができるまでしばらくお待ちください");   //全員が接続できるまで待機
        String str = readSingleMessage();  //受信番号: R1, 全員の接続を検出
        System.out.println(str);  //メッセージを表示
    }

    private static void sendNickname(){
        boolean correctName = false;
        String str = null;
        while(correctName == false){
            System.out.print("ニックネームを入力してください: ");
            str = sc.next();  //ニックネームを取得
            System.out.print("ニックネームが " + str + " でよければ y を入力してください: ");
            String reply = sc.next();  //登録ニックネームの確認を取得
            correctName = correct(reply);
        }

        writer.println(str);  //送信番号: S1, ニックネームを送信
        str = readSingleMessage();  //受信番号: R6, ニックネームの登録完了を受信
        System.out.println(str);  //メッセージを表示
    }

    private static void receivePlayerInfo(){
        String str = readSingleMessage();  //受信番号: R7, プレイヤーの人数を受信
        PLAYER = Integer.parseInt(str);

        nickNames = new String[PLAYER];
        fieldCards = new int[PLAYER][INSECTNUM];

        str = readSingleMessage();  //受審番号: R31, プレイヤーidを受審
        playerId = Integer.parseInt(str);
        nickNames = readMultiMessages(PLAYER);  //受信番号: R8, 全員のニックネームを受信

        for(int p = 0; p < PLAYER; ++p){
            for(int i = 0; i < INSECTNUM; ++i){
                fieldCards[p][i] = 0;
            }
        }

        str = readSingleMessage();  //受信番号: R9, 全員のニックネームの登録・送信完了を受信
        System.out.println(str);  //メッセージを表示
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
            }  //受信番号: R10, 手持ちのカードを受信
        }catch(IOException e){
            System.err.println(e);
            System.err.println("サーバ " + addr + " との接続が切れました");
            return;
        }

        str = readSingleMessage();  //受信番号: R11, カード配布の完了の通知を受信
        System.out.println(str);  //メッセージの表示
        System.out.println("最初の手札は");
        showHandCards();
        System.out.println("です.");
    }

    private static void sendPushCard(){  //カードを手持ちから出すとき
        String str = readSingleMessage();//受信番号: R12, 各クライアントの役割を受信

        if(str.equals(yes)){  //メインプレイヤーのとき
            String[] choosables = new String[PLAYER];
            choosables = readMultiMessages(PLAYER);  //受信番号: R13, カードを押し付ける対象かどうかの一覧を受信

            System.out.println("押し付ける害虫カード と 宣言する害虫名 と 押し付ける相手 を選びます.");

            int card = -1;
            int say = -1;
            int target = -1;

            while(phase >= 0 && phase < 4){
                if(phase == 0){
                    card = chooseInsectCard();  //どれを押し付けるか
                }else if(phase == 1){
                    say = chooseSayWhat(true);  //なんと宣言するか
                }else if(phase == 2){
                    target = chooseToWhom(choosables);  //誰に押し付けるか
                }else{
                    confirmationPushCard(card, say, target);
                }
            }

            phase = 0;
            handCards[card]--;  //押し付けたカードを減らす

            writer.println(card);  //送信番号: S2, 押し付けるカードを送信
            writer.println(say);  //送信番号: S3, 宣言を送信
            writer.println(target);  //送信番号: S4, 押し付ける相手を送信

        }else if(str.equals(no)){  //メインプレイヤーでないとき

            String str1 = readSingleMessage(); //受信番号: R14, 待機の指示を受信
            System.out.println(str1);  //メッセージを表示
        }
        
        str= readSingleMessage();  //受信番号: R15, メインプレイヤーの行動結果を通知
        System.out.println(str);  //メッセージの表示
    }
    
    private static int chooseInsectCard(){
        int card = -1;
        boolean correctInput = false;
        String str = null;

        while(correctInput == false){

            System.out.println("どの害虫カードを押し付けますか？ 今の手札は");
            showHandCards();
            System.out.println("です.");

            str = sc.next();  //押し付けるカードのidを取得

            try{
                card = Integer.parseInt(str);
                if(card < 0 || card >= INSECTNUM || handCards[card] <= 0){
                    throw new UnexpectedInputException();
                }

                correctInput = true;
                phase++;

            }catch(NumberFormatException e){
                System.err.println("入力は数字ではありません. やり直してください");
            }catch(UnexpectedInputException e){
                System.err.println("そのカードは持っていないか選択肢にありません. やり直してください");
            }
        }
        return card;
    }

    private static int chooseSayWhat(boolean pushCard){
        int say = -1;
        boolean correctInput = false;
        String str = null;

        while(correctInput == false){

            System.out.println("今のカードを何と宣言して相手に押し付けますか？");
            showInsects();

            if(pushCard){
                System.out.println("押し付ける害虫カード を選びなおす場合には b を入力してください");
            }

            str = sc.next();  //宣言を取得

            if(pushCard && back(str)){
                phase--;
                break;
            }

            try{
                say = Integer.parseInt(str);

                if(say < 0 || say >= INSECTNUM){
                    throw new UnexpectedInputException();
                }

                correctInput = true;
                phase++;

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
        boolean correctInput = false;
        String str = null;

        while(correctInput == false){

            System.out.println("押し付ける相手は誰にしますか？");
            showChoosablePlayer(choosables);
            System.out.println("宣言する害虫名 を選びなおす場合には b を入力してください");

            str = sc.next();  //押し付ける相手を取得

            if(back(str)){
                phase--;
                break;
            }

            try{
                target = Integer.parseInt(str);

                if(target < 0 || target >= INSECTNUM || choosables[target].equals(no)){
                    throw new UnexpectedInputException();
                }

                correctInput = true;
                phase++;

            }catch(NumberFormatException e){
                System.err.println("入力は数字ではありません. やり直してください");
            }catch(UnexpectedInputException e){
                System.err.println("そのプレイヤーは選択肢にありません. やり直してください");
            }
        }
        return target;
    }

    private static void confirmationPushCard(int card, int say, int target){
        boolean getReply = false;
        String reply = null;
        while(getReply == false){
            System.out.println("害虫カード: " + insects[card] + " 宣言する害虫名: " + insects[say] + " 押し付ける相手: " + nickNames[target] + " で送信します.");
            System.out.println("これでよければ y を, 押し付ける相手を選びなおす場合には b を入力してください");

            reply = sc.next();

            try{
                getReply = correct(reply) || back(reply);

                if(getReply == false){
                    throw new UnexpectedInputException();
                }

            }catch(UnexpectedInputException e){
                System.err.println("押し付ける相手を選びなおすのか行動を確定するのかわかりません. やり直してください.");
            }
        }

        if(correct(reply)){
            phase++;
        }else if(back(reply)){
            phase--;
        }
    }


    private static void sendAction(){  //カードを押し付けられた時
        String str1 = readSingleMessage();  //受信番号: R16, 各クライアントの役割を受信

        if(str1.equals(yes)){  //メインプレイヤーの時
            
            String str2 = readSingleMessage();  //受信番号: R17, 押し付ける対象がまだいるかを受信
            int pass = -1; //押し付ける場合は1, 自分で判定する場合は0

            System.out.println("カードを押し付けられてしまいました...");

            if(str2.equals(yes)){  //押し付ける対象がまだいる場合
                boolean correctChoice = false;

                while(correctChoice == false){
                    System.out.println("(0) カードを当てますか？ (1)カードをみて他のプレイヤーに押し付けますか？");
                    String str = sc.next();  //自分で判定するか押し付けるかを取得

                    try{
                        pass = Integer.parseInt(str);
                        if(pass != 0 && pass != 1){
                            throw new UnexpectedInputException();
                        }
                        if(pass == 0){
                            System.out.println("カードを当てる でよければ y を入力してください: ");
                        }else if(pass == 1){
                            System.out.println("カードをみて他のプレイヤーに押し付ける でよければ y を入力してください: ");
                        }

                        String reply = sc.next();  //自分で判定するか押し付けるかを確認
                        correctChoice = correct(reply);
                    }catch(NumberFormatException e){
                        System.err.println("入力は数字ではありません. やり直してください");
                    }catch(UnexpectedInputException e){
                        System.err.println("それは選択肢にありません. やり直してください");
                    }
                }

                writer.println(pass == 1? yes : no);  //送信番号: S5, 押し付けるか自分で当てるかを送信

            }else if(str2.equals(no)){  //押し付ける対象がもういない場合
                pass = 0; //たらい回しはできない
                System.out.println("カードを押し付けられるプレイヤーがいません. カードを当てます.");
            }


            if(pass == 1){  //他の人に押し付ける場合
                String str3 = readSingleMessage();  //受信番号: R18, 押し付けられたカードがなんであったかを受信

                int card = Integer.parseInt(str3);
                System.out.println("カードは " + insects[card] + " でした");

                String[] choosables = new String[PLAYER];
                choosables = readMultiMessages(PLAYER);//受信番号: R19, カードを押し付ける対象かどうかの一覧を受信

                int sayAgain = -1;
                int targetAgain = -1;

                while(phase >= 0 && phase < 3){
                    if(phase == 0){
                        sayAgain = chooseSayWhat(false);  //なんと宣言するか
                    }else if(phase == 1){
                        targetAgain = chooseToWhom(choosables);  //誰に押し付けるか
                    }else{
                        confirmationPushCard(card, sayAgain, targetAgain);
                    }
                }

                phase = 0;

                writer.println(sayAgain);  //送信番号: S6, 宣言を送信
                writer.println(targetAgain);  //送信番号: S7, 押し付ける相手を送信

            }else if(pass == 0){  //自分で当てる場合
                String str3 = readSingleMessage();  //受信番号: R20, 押し付けてきた相手を受信
                int senderId = Integer.parseInt(str3);
                str3 = readSingleMessage();  //受信番号: R21, 相手の宣言を受信
                int say = Integer.parseInt(str3);
                boolean correctChoice = false;
                int guess = -1;

                while(correctChoice == false){
                    System.out.println(nickNames[senderId] + " はこのカードを　" + insects[say] + " と宣言しています.");
                    System.out.println("このカードは本当に　" + insects[say] + " だと思いますか？");
                    System.out.println("(0) そう思う (1) そう思わない");

                    String str = sc.next();  //予想を取得

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

                        String reply = sc.next();  //予想の確認を取得
                        correctChoice = correct(reply);
                    }catch(NumberFormatException e){
                        System.err.println("入力は数字ではありません. やり直してください");
                    }catch(UnexpectedInputException e){
                        System.err.println("それは選択肢にありません. やり直してください");
                    }
                }

                writer.println(guess == 0? yes : no);  //受信番号: S8, 予想を送信
                str3 = readSingleMessage(); //受信番号: R22, 予想の可否の通知受信
                String str4 = readSingleMessage();  //受信番号: R23, 正解を受信
                int ans = Integer.parseInt(str4);

                if(str3.equals(yes)){
                    fieldCards[senderId][ans]++;  //正解だったら直前に押し付けたプレイヤーの場のカードが増える
                }else if(str3.equals(no)){
                    fieldCards[playerId][ans]++;  //不正解だったら自分の場のカードが増える
                }

                str3 = readSingleMessage();  //受信番号: R24, 表示用のメッセージを受信
                System.out.println(str3);  //メッセージの表示
            }

        }else if(str1.equals("SENDER")){  //直前に押し付けたプレイヤーの時
            String str2 = readSingleMessage();  //送信番号: R25, 待機の指示を受信
            System.out.println(str2);  //メッセージの表示

            str2 = readSingleMessage();  //受信番号: R26 当てられたか外れたかたらい回しかを受信
            if(str2.equals(yes)){
                String str3 = readSingleMessage();  //受信番号: R27, 当てられたカードを受信
                int ans = Integer.parseInt(str3);
                fieldCards[playerId][ans]++;  //正解だったら自分の場のカードが増える

            }else if(str2.equals(no)){
                String str3 = readSingleMessage();  //受信番号: R34, 外してくれたカードを受信
                int ans = Integer.parseInt(str3);
                str3 = readSingleMessage();  //受審番号: R35, 押し付けた相手を受信
                int target = Integer.parseInt(str3);
                fieldCards[target][ans]++;  //不正解だったら送った相手カードが増える
            }

            str2 = readSingleMessage();  //受信番号: R28, 表示用のメッセージを受信
            System.out.println(str2);  //メッセージを表示

        }else if(str1.equals(no)){  //何事もないプレイヤーのとき
            String str2 = readSingleMessage(); //受信番号: R29, 待機の指示を受信
            System.out.println(str2);  //メッセージを表示

            str2 = readSingleMessage();    //受審番号: R32, 今メインになっているカードを受審
            int card = Integer.parseInt(str2);
            str2 = readSingleMessage();  //受審番号: R33 場のカードが増えてしまったプレイヤーのidを受審
            int receiver = Integer.parseInt(str2);

            if(receiver != -1){  //誰かの場にカードが溜まるとき
                fieldCards[receiver][card]++;
            }
            str2 = readSingleMessage();   //受信番号: R30, カードの判定結果, あるいはたらい回しされたことを受信
            System.out.println(str2);  //メッセージを表示
        }
    }

    private static void haveTooManyInsects(){  //Kimura: 場のカードのうちLIMIT以上のものがあるか
        boolean match = false;
        
        for(int i = 0; i < INSECTNUM; ++i){
            if(fieldCards[playerId][i] >= LIMIT){
                match = true;
                break;
            }
        }

        for(int p = 0; p < PLAYER; ++p){
            System.out.println(nickNames[p] + " が今まで押し付けられてしまった害虫カードは");
            showFieldCards(p);
            System.out.println();
        }

        System.out.println("です. 確認できたら y を押してください.");

        boolean check = false;
        while(check == false){
            String reply = sc.next();
            check = correct(reply);
        }
        System.out.println("全員の確認が取れるまでしばらくお待ちください");
        writer.println(match? yes : no);   //送信番号S9-1: 敗者かどうかを送信.
    }

    private static void haveNoCard(){  //Kimura: 手持ちのカードが残っているか
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
        System.out.println("全員の確認が取れるまでしばらくお待ちください");
        writer.println(match? yes : no);   //送信番号S9-2: 敗者かどうかを送信.
    }

    private static void showResult(){
        String result = readSingleMessage();
        System.out.println(result);
    }

    private static void showHandCards() {//Kimura : 手持ちのカードを可視化
		for(int i = 0; i < INSECTNUM; i++){
			System.out.println("(" + i + ")" + String.format("%-4s", insects[i]) + "\t:" + handCards[i] + "枚");
		}
    }

    private static void showFieldCards(int p) {//Kimura : 場のカードを可視化
		for(int i = 0; i < INSECTNUM; i++){
			System.out.println("(" + i + ")" + String.format("%-4s", insects[i]) + "\t:" + fieldCards[p][i] + "枚");
		}
    }

    private static void showInsects() {
		for(int i = 0; i < INSECTNUM; i++){
			System.out.println("(" + i + ")" + insects[i]);
		}
    }  //害虫カードの種類を全て表示

    private static void showChoosablePlayer(String[] choosables){
        for (int i = 0; i < PLAYER; i++) {
            if(choosables[i].equals(yes)){
                System.out.println("(" + i + ")" + nickNames[i]);
            }
		}
    }


    public static boolean correct(String reply){
        String lowercase = reply.toLowerCase();
        return lowercase.equals("yes") || lowercase.equals("y");
    }  //ユーザからの入力が正しいことの確認

    public static boolean back(String reply){
        String lowercase = reply.toLowerCase();
        return lowercase.equals("back") || lowercase.equals("b");
    }  //ユーザからの入力やり直しを確認


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
    }  //単一データ受信

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
    }  //複数データ受信

    private static boolean isBreak(String breakMessage){
        String str = readSingleMessage();
        if(str.equals(breakMessage)){
            return true;
        }else{
            return false;
        }
    }  //無限ループを抜けるかどうか
}