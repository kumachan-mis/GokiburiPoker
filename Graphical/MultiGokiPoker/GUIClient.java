/*
各受信番号・送信番号は
PokerServer.javaの各送信番号・受信番号に対応しています.
-は条件による送信文字列の分岐を意味します.
*/

import java.lang.management.ThreadInfo;
import java.net.InetAddress;
import processing.core.PApplet;
import processing.core.PFont;

public class GUIClient extends PApplet{

    private static final String yes = "YES";
    private static final String no = "NO";
    static ClientCommunication cc = null;

    public final int WIDTH = 720;
    public final int HEIGHT = 640;
    private ImportantValue value = ImportantValue.getInstance();
    private GameShow show = new GameShow(this, PLAYER, INSECTNUM);
    private GameButton button = new GameButton(this, value, PLAYER, INSECTNUM);

    private int turn = 1;
    private int phase = 0;

    public static final int PLAYER = 4;  //プレイヤーの人数
    public static final String[] insects =
    {"コウモリ", "ハエ", "ネズミ", "サソリ", "ゴキブリ", "カエル", "クモ", "カメムシ"};
    public static final int INSECTNUM = insects.length; //カードの種類の数
    public static final int LIMIT = 4;  //場にたまっても良い各害虫カードの枚数
    static int playerId = -1;
    static int[] handCards = null;  //手持ちのカードの枚数
    static int[][] fieldCards = null;  //場のカードの枚数
    static String[] nickNames = null;
    static int[] sumOfHandCards = null;

    static void makeInstance(InetAddress addr, int PORT){
        handCards = new int[INSECTNUM];
        fieldCards = new int[PLAYER][INSECTNUM];
        nickNames = new String[PLAYER];
        sumOfHandCards = new int[PLAYER];
        cc = new ClientCommunication(addr, PORT);
    }

    public void settings() {// Kimura: ウィンドウの初期設定
		size(WIDTH, HEIGHT);
        noLoop();
    }

    public void setup() {// Kimura: その他初期設定
		colorMode(RGB, 256);
		background(0, 130, 45);
        smooth();
		textFont(createFont("メイリオ", 30));
    }

    private void resetGameView(){
        background(0, 130, 45);
        show.showTurn(turn);
        show.showMyHandCards(sumOfHandCards[playerId], handCards, nickNames[playerId]);;
        show.showMyFieldCards(fieldCards[playerId]);;
        show.showEnemyHandCards(playerId, sumOfHandCards, nickNames);
        show.showEnemyFieldCards(playerId, fieldCards);
        show.inputWindow();
    }
    
    public void draw(){
        showRules();

        while(true){
            turn = Integer.parseInt(cc.readSingleMessage());  //受信番号: R2, ターン数を受信
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
    }
    
    private void showRules(){  //Eguchi: 
        phase = 0;
        button.showConfirmButton(15 * HEIGHT / 16, "読んだ");
        
        while(phase == 0){

        } //読んだボタンを押されるまで
        cc.write("READ");  //送信番号: S10, ルール読み終わりを送信
    }  //フェーズ 0: ルールの表示・読んだことの確定

    private void sendPushCard(){  //カードを手持ちから出すとき
        String str = cc.readSingleMessage();//受信番号: R12, メインプレイヤーのidを受信
        int mainPlayerId = Integer.parseInt(str);

        if(playerId == mainPlayerId){  //メインプレイヤーのとき
            String[] choosables = new String[PLAYER];
            choosables = cc.readMultiMessages(PLAYER);  //受信番号: R13, カードを押し付ける対象かどうかの一覧を受信

            show.showNormalMessage("あなたの番です.");
            getConfirm(1);

            while(phase >= 1 && phase < 5){
                if(phase == 1){
                    chooseInsectCard();  //フェーズ 1: どれを押し付けるか
                }else if(phase == 2){
                    value.setPushCard(true);
                    chooseSayWhat();  //フェーズ 2: なんと宣言するか
                }else if(phase == 3){
                    chooseToWhom(choosables);  //フェーズ 3: 誰に押し付けるか
                }else{
                    confirmationPushCard(); //フェーズ 4: 選択の確認
                }
            }

            handCards[value.getCard()]--;  //押し付けたカードを減らす

            cc.write(value.getCard());  //送信番号: S2, 押し付けるカードを送信
            cc.write(value.getSay());  //送信番号: S3, 宣言を送信
            cc.write(value.getTarget());  //送信番号: S4, 押し付ける相手を送信

        }else{  //メインプレイヤーでないとき
            String str1 = cc.readSingleMessage(); //受信番号: R14, 待機の指示を受信
            show.showNormalMessage(str1);  //メッセージを表示
        }
        
        phase = 5;
        sumOfHandCards[mainPlayerId]--;

        str = cc.readSingleMessage();  //受信番号: R15, メインプレイヤーの行動結果を通知
        show.showNormalMessage(str);  //メッセージの表示
        getConfirm(-1);
    }

    private void sendAction(){  //カードを押し付けられた時
        String str1 = cc.readSingleMessage();  //受信番号: R16, 各クライアントの役割を受信
        if(str1.equals(yes)){  //メインプレイヤーの時

            String str2 = cc.readSingleMessage();  //受信番号: R17, 押し付ける対象がまだいるかを受信
            show.showNormalMessage("カードを押し付けられてしまいました...");
            getConfirm(5);

            if(str2.equals(yes)){  //押し付ける対象がまだいる場合
                passOrBattle();  //フェーズ 5: 押し付けるか自分で当てるか
                cc.write(value.getPass()? yes : no);  //送信番号: S5, 押し付けるか自分で当てるかを送信

            }else if(str2.equals(no)){  //押し付ける対象がもういない場合
                value.setPass(false);//たらい回しはできない
                show.showNormalMessage("カードを押し付けられるプレイヤーがいません. カードを当てます.");
                getConfirm(6);
            }


            if(value.getPass()){  //他の人に押し付ける場合
                String str3 = cc.readSingleMessage();  //受信番号: R18, 押し付けられたカードがなんであったかを受信
                value.setCard(Integer.parseInt(str3));

                show.showNormalMessage("カードは " + insects[value.getCard()] + " でした");
                getConfirm(2);

                String[] choosables = new String[PLAYER];
                choosables = cc.readMultiMessages(PLAYER);//受信番号: R19, カードを押し付ける対象かどうかの一覧を受信

                while(phase >= 2 && phase < 5){
                    if(phase == 2){
                        value.setPushCard(false);
                        chooseSayWhat();  //フェーズ 2: なんと宣言するか
                    }else if(phase == 3){
                        chooseToWhom(choosables);  //フェーズ 3: 誰に押し付けるか
                    }else{
                        confirmationPushCard();  //フェーズ 4: 選択の確認
                    }
                }

                cc.write(value.getSay());  //送信番号: S6, 宣言を送信
                cc.write(value.getTarget());  //送信番号: S7, 押し付ける相手を送信

            }else{  //自分で当てる場合
                String str3 = cc.readSingleMessage();  //受信番号: R20, 押し付けてきた相手を受信
                int senderId = Integer.parseInt(str3);
                str3 = cc.readSingleMessage();  //受信番号: R21, 相手の宣言を受信
                int say = Integer.parseInt(str3);

                phase = 6;
                guessCard(senderId, say);  //フェーズ 6: カードを当てる
                cc.write(value.getGuess()? yes : no);  //受信番号: S8, 予想を送信

                str3 = cc.readSingleMessage(); //受信番号: R22, 予想の可否の通知受信
                String str4 = cc.readSingleMessage();  //受信番号: R23, 正解を受信
                int ans = Integer.parseInt(str4);

                if(str3.equals(yes)){
                    fieldCards[senderId][ans]++;  //正解だったら直前に押し付けたプレイヤーの場のカードが増える
                }else if(str3.equals(no)){
                    fieldCards[playerId][ans]++;  //不正解だったら自分の場のカードが増える
                }

                str3 = cc.readSingleMessage();  //受信番号: R24, 表示用のメッセージを受信
                show.showImportantMessage(str3);  //メッセージの表示
                getConfirm(-1);
            }

        }else if(str1.equals("SENDER")){  //直前に押し付けたプレイヤーの時
            String str2 = cc.readSingleMessage();  //送信番号: R25, 待機の指示を受信
            show.showNormalMessage(str2);  //メッセージの表示

            str2 = cc.readSingleMessage();  //受信番号: R26 当てられたか外れたかたらい回しかを受信

            if(str2.equals(yes)){
                String str3 = cc.readSingleMessage();  //受信番号: R27, 当てられたカードを受信
                int ans = Integer.parseInt(str3);
                fieldCards[playerId][ans]++;  //正解だったら自分の場のカードが増える

            }else if(str2.equals(no)){
                String str3 = cc.readSingleMessage();  //受信番号: R34, 外してくれたカードを受信
                int ans = Integer.parseInt(str3);
                str3 = cc.readSingleMessage();  //受審番号: R35, 押し付けた相手を受信
                int target = Integer.parseInt(str3);
                fieldCards[target][ans]++;  //不正解だったら送った相手カードが増える
            }

            str2 = cc.readSingleMessage();  //受信番号: R28, 表示用のメッセージを受信
            show.showImportantMessage(str2);  //メッセージを表示
            getConfirm(-1);

        }else if(str1.equals(no)){  //何事もないプレイヤーのとき
            String str2 = cc.readSingleMessage(); //受信番号: R29, 待機の指示を受信
            show.showNormalMessage(str2);  //メッセージを表示

            str2 = cc.readSingleMessage();    //受審番号: R32, 今メインになっているカードを受審
            int card = Integer.parseInt(str2);
            str2 = cc.readSingleMessage();  //受審番号: R33 場のカードが増えてしまったプレイヤーのidを受審
            int receiver = Integer.parseInt(str2);

            if(receiver != -1){  //誰かの場にカードが溜まるとき
                fieldCards[receiver][card]++;
            }
            str2 = cc.readSingleMessage();   //受信番号: R30, カードの判定結果, あるいはたらい回しされたことを受信
            show.showImportantMessage(str2);  //メッセージを表示
            getConfirm(-1);
        }
    }
    
    private void chooseInsectCard(){
        value.setBackActive(false);
        show.showNormalMessage("どの害虫カードを押し付けますか？");
        button.showChoosableCard(handCards);
        while(phase == 1){

        }
    }

    private void chooseSayWhat(){
        value.setBackActive(value.getPushCard());
        show.showNormalMessage("今のカードを何と宣言して相手に押し付けますか？");
        button.showInsects();
        if(value.getBackActive()){
            button.showBackButton();
        }

        while(phase == 2){

        }
        value.setBackActive(false);
    }

    private void chooseToWhom(String[] choosables){
        value.setBackActive(true);
        show.showNormalMessage("押し付ける相手は誰にしますか？");
        button.showChoosablePlayer(playerId, nickNames, choosables, yes);;
        button.showBackButton();
        while(phase == 3){

        }
        value.setBackActive(false);
    }

    private void confirmationPushCard(){
        value.setBackActive(true);
        show.showNormalMessage("害虫カード: " + insects[value.getCard()] + " 宣言する害虫名: " + insects[value.getSay()] + " 押し付ける相手: " + nickNames[value.getTarget()] + " で送信します.");
        button.showConfirmButton(HEIGHT / 2, "OK");
        button.showBackButton();
        while(phase == 4){

        }
        value.setBackActive(false);
    }

    private void passOrBattle(){
        show.showNormalMessage("行動を選択してください");
        String[] yes_or_no = {"他の人に押し付ける", "自分で当てる"};
        button.showYesOrNoButton(yes_or_no);
        while(phase == 5){

        }
    }

    private void guessCard(int senderId, int say){
        show.showNormalMessage(nickNames[senderId] + " はこのカードを　" + insects[say] + " と宣言しています.\n" +
        "このカードは本当に　" + insects[say] + " だと思いますか？"); 
        String[] yes_or_no = {"そう思う", "違うと思う"};
        button.showYesOrNoButton(yes_or_no);

        while(phase == 6){

        }
    }

    private void haveTooManyInsects(){  //Kimura: 場のカードのうちLIMIT以上のものがあるか
        boolean match = false;
        for(int i = 0; i < INSECTNUM; ++i){
            if(fieldCards[playerId][i] >= LIMIT){
                match = true;
                break;
            }
        }

        cc.write(match? yes : no);   //送信番号S9-1: 敗者かどうかを送信.
    }

    private void haveNoCard(){  //Kimura: 手持ちのカードが残っているか
        boolean match = true;
        for(int i = 0; i < INSECTNUM; ++i){
            if(handCards[i] > 0){
                match = false; //Kimura: 手札がある時
                break;
            }
        }

        cc.write(match? yes : no);   //送信番号S9-2: 敗者かどうかを送信.
    }

    private void showResult(){
        
    }

    private void getConfirm(int phase){
        button.showConfirmButton(HEIGHT / 2, "OK");  //  フェーズ 7: 確認
        phase = 7;
        while(phase == 7){

        }
        this.phase = phase;
    }

    public void mouseReleased(){
        phase = button.getNextPhase(phase);
    }

    private boolean isBreak(String breakMessage){
        String str = cc.readSingleMessage();
        if(str.equals(breakMessage)){
            return true;
        }else{
            return false;
        }
    }  //無限ループを抜けるかどうか
}