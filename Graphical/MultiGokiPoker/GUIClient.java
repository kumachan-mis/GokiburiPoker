/*
各受信番号・送信番号は
PokerServer.javaの各送信番号・受信番号に対応しています.
-は条件による送信文字列の分岐を意味します.
*/

import java.net.InetAddress;
import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PImage;

public class GUIClient extends PApplet{

    private static final String yes = "YES";
    private static final String no = "NO";

    public final int WIDTH = 900;
    public final int HEIGHT = 800;
    private int numOfButton = 0;
    private int buttonY = 0;
    private int interval = 0;
    private int buttonWidth = 0;
    private int buttonHeight = 0;
    private boolean[] choosables = null;
    private ImportantValue value = ImportantValue.getInstance();

    private int turn = 1;
    private int phase = 0;
    public static final int PLAYER = 4;  //プレイヤーの人数
    public static final String[] insects =
    {"コウモリ", "ハエ", "ネズミ", "サソリ", "ゴキブリ", "カエル", "クモ", "カメムシ"};
    public static final String[] insectsImage =
    {"", "", "", "", "", "", "", ""};
    public static final int INSECTNUM = insects.length; //カードの種類の数
    public static final int LIMIT = 4;  //場にたまっても良い各害虫カードの枚数

    static int playerId = -1;
    static int[] handCards = null;  //手持ちのカードの枚数
    static int[][] fieldCards = null;  //場のカードの枚数
    static String[] nickNames = null;
    static int[] sumOfHandCards = null;

    static ClientCommunication cc = null;

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
        numOfButton = 0;
        buttonY = 0;
        interval = 0;
        buttonWidth = 0;
        buttonHeight = 0;
        choosables = null;

        background(0, 130, 45);
        showTurn();
        showMyHandCards();
        showMyFieldCards();
        showEnemyHandCards();
        showEnemyFieldCards();
        inputWindow();

        System.gc();
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

    //**********ゲームでの動作(ここから)**********
    private void showRules(){  //Eguchi: 
        phase = 1;

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

            showNormalMessage("あなたの番です.");
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
            showNormalMessage(str1);  //メッセージを表示
        }
        
        phase = 5;
        sumOfHandCards[mainPlayerId]--;

        str = cc.readSingleMessage();  //受信番号: R15, メインプレイヤーの行動結果を通知
        showNormalMessage(str);  //メッセージの表示
        getConfirm(-1);
    }

    private void sendAction(){  //カードを押し付けられた時
        String str1 = cc.readSingleMessage();  //受信番号: R16, 各クライアントの役割を受信
        if(str1.equals(yes)){  //メインプレイヤーの時

            String str2 = cc.readSingleMessage();  //受信番号: R17, 押し付ける対象がまだいるかを受信
            showNormalMessage("カードを押し付けられてしまいました...");
            getConfirm(5);

            if(str2.equals(yes)){  //押し付ける対象がまだいる場合
                passOrBattle();  //フェーズ 5: 押し付けるか自分で当てるか
                cc.write(value.getPass()? yes : no);  //送信番号: S5, 押し付けるか自分で当てるかを送信

            }else if(str2.equals(no)){  //押し付ける対象がもういない場合
                value.setPass(false);//たらい回しはできない
                showNormalMessage("カードを押し付けられるプレイヤーがいません. カードを当てます.");
                getConfirm(6);
            }


            if(value.getPass()){  //他の人に押し付ける場合
                String str3 = cc.readSingleMessage();  //受信番号: R18, 押し付けられたカードがなんであったかを受信
                value.setCard(Integer.parseInt(str3));

                showNormalMessage("カードは " + insects[value.getCard()] + " でした");
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
                showImportantMessage(str3);  //メッセージの表示
                getConfirm(-1);
            }

        }else if(str1.equals("SENDER")){  //直前に押し付けたプレイヤーの時
            String str2 = cc.readSingleMessage();  //送信番号: R25, 待機の指示を受信
            showNormalMessage(str2);  //メッセージの表示

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
            showImportantMessage(str2);  //メッセージを表示
            getConfirm(-1);

        }else if(str1.equals(no)){  //何事もないプレイヤーのとき
            String str2 = cc.readSingleMessage(); //受信番号: R29, 待機の指示を受信
            showNormalMessage(str2);  //メッセージを表示

            str2 = cc.readSingleMessage();    //受審番号: R32, 今メインになっているカードを受審
            int card = Integer.parseInt(str2);
            str2 = cc.readSingleMessage();  //受審番号: R33 場のカードが増えてしまったプレイヤーのidを受審
            int receiver = Integer.parseInt(str2);

            if(receiver != -1){  //誰かの場にカードが溜まるとき
                fieldCards[receiver][card]++;
            }
            str2 = cc.readSingleMessage();   //受信番号: R30, カードの判定結果, あるいはたらい回しされたことを受信
            showImportantMessage(str2);  //メッセージを表示
            getConfirm(-1);
        }
    }
    
    private void chooseInsectCard(){
        value.setBackActive(false);
        showNormalMessage("どの害虫カードを押し付けますか？");
        showChoosableCard();
        while(phase == 1){

        }
    }

    private void chooseSayWhat(){
        value.setBackActive(value.getPushCard());
        showNormalMessage("今のカードを何と宣言して相手に押し付けますか？");
        showInsects();
        while(phase == 2){

        }
        value.setBackActive(false);
    }

    private void chooseToWhom(String[] choosables){
        value.setBackActive(true);
        showNormalMessage("押し付ける相手は誰にしますか？");
        showChoosablePlayer(choosables);
        while(phase == 3){

        }
        value.setBackActive(false);
    }

    private void confirmationPushCard(){
        value.setBackActive(true);
        showNormalMessage("害虫カード: " + insects[value.getCard()] + " 宣言する害虫名: " + insects[value.getSay()] + " 押し付ける相手: " + nickNames[value.getTarget()] + " で送信します.");
        showConfirmButton(HEIGHT / 2, "OK");
        while(phase == 4){

        }
        value.setBackActive(false);
    }

    private void passOrBattle(){
        showNormalMessage("行動を選択してください");
        numOfButton = 2;
        buttonY = 9 * HEIGHT / 20;
        buttonWidth = WIDTH / 3;
        buttonHeight = 7 * HEIGHT / 80;
        interval = WIDTH / 6;
        createButtonRect();
        String[] texts = {"他の人に押し付ける", "自分で当てる"};
        createButtonText(texts);

        while(phase == 5){

        }
    }

    private void guessCard(int senderId, int say){
        showNormalMessage(nickNames[senderId] + " はこのカードを　" + insects[say] + " と宣言しています.\n" +
        "このカードは本当に　" + insects[say] + " だと思いますか？"); 
        numOfButton = 2;
        buttonY = 9 * HEIGHT / 20;
        buttonWidth = WIDTH / 3;
        buttonHeight = 7 * HEIGHT / 80;
        interval = WIDTH / 6;
        createButtonRect();
        String[] texts = {"そう思う", "違うと思う"};
        createButtonText(texts); 

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
        showConfirmButton(HEIGHT / 2, "OK");  //  フェーズ 7: 確認
        phase = 7;
        while(phase == 7){

        }
        this.phase = phase;
    }
    //**********ゲームでの動作(ここまで)**********

    //**********ボタン生成・動作(ここから)**********
    private void createButtonRect(){
        int buttonX;
        strokeWeight(1);
        fill(255);
        rectMode(CENTER);
        int mid = numOfButton / 2 + 1;

        for(int i = 0; i < numOfButton; ++i){
            if(numOfButton % 2 == 0){
                buttonX =
                (buttonWidth + interval) * (i - numOfButton / 2)
                + buttonWidth / 2 - interval / 2 + WIDTH / 2;
            }else{
                buttonX = (buttonWidth + interval) * (i - mid) + WIDTH / 2;
            }

            rect(buttonX, buttonY, buttonWidth, buttonHeight);
        }
    }

    private void createButtonImage(){
        int buttonX;
        strokeWeight(1);
        imageMode(CENTER);
        int mid = INSECTNUM / 2 + 1;

        for(int i = 0; i < INSECTNUM; ++i){
            PImage img = loadImage("Images/" + insectsImage);

            if(INSECTNUM % 2 == 0){
                buttonX =
                (buttonWidth + interval) * (i - INSECTNUM / 2)
                + buttonWidth / 2 - interval / 2 + WIDTH / 2;

            }else{
                buttonX = (buttonWidth + interval) * (i - mid) + WIDTH / 2;
            }
            
            image(img, buttonX, buttonY, buttonWidth, buttonHeight);
        }
    }

    private void createButtonText(String[] texts){
        int buttonX;

        strokeWeight(1);
		fill(0);
        textAlign(CENTER, CENTER);
        textSize(30);
        
        int mid = numOfButton / 2 + 1;

        for(int i = 0; i < numOfButton; ++i){
            if(numOfButton % 2 == 0){
                buttonX =
                (buttonWidth + interval) * (i - numOfButton / 2)
                + buttonWidth / 2 - interval / 2 + WIDTH / 2;
            }else{
                buttonX = (buttonWidth + interval) * (i - mid) + WIDTH / 2;
            }

            text(texts[i], buttonX, buttonY, buttonWidth, buttonHeight);
        }
    }

    private void createCross(){  // Kimura: 選択できないカードに×を表示
        int buttonX;
        strokeWeight(3);
        fill(0);
        int mid = numOfButton / 2 + 1;

        for(int i = 0; i < numOfButton; ++i){
            if(numOfButton % 2 == 0){
                buttonX =
                (buttonWidth + interval) * (i - numOfButton / 2)
                + buttonWidth / 2 - interval / 2 + WIDTH / 2;
            }else{
                buttonX = (buttonWidth + interval) * (i - mid) + WIDTH / 2;
            }

            if(choosables[i] == false){
                line(buttonX - buttonWidth / 2, buttonY - buttonHeight / 2,
                buttonX + buttonWidth / 2, buttonY + buttonHeight / 2);
                line(buttonX + buttonWidth / 2, buttonY - buttonHeight / 2,
                buttonX - buttonWidth / 2, buttonY + buttonHeight / 2);
            }
        }
    }

    private void releasedAction(Action action){
        int mid = numOfButton / 2 + 1;
        if(value.getBackActive() &&
        mouseX >= WIDTH * 15 / 18 && mouseX <= WIDTH * 17 / 18 &&
        mouseY >= HEIGHT / 60 && mouseY <= HEIGHT / 20){
            phase--;
         }

        if(numOfButton % 2 == 0){
            for(int i = 0; i < numOfButton; ++i){
                int buttonX =
                (buttonWidth + interval) * (i - numOfButton / 2)
                + buttonWidth / 2 - interval / 2 + WIDTH / 2;

                if(mouseX >= buttonX - buttonWidth / 2 && mouseX <= buttonX + buttonWidth / 2 &&
                mouseY >= buttonY - buttonHeight / 2 && mouseY <= buttonY + buttonHeight / 2){
                    action.action(i);
                    break;
                }
            }
        }else{
            for(int i = 0; i < numOfButton; ++i){
                int buttonX = (buttonWidth + interval) * (i - mid) + WIDTH / 2;

                if(mouseX >= buttonX - buttonWidth / 2 && mouseX <= buttonX + buttonWidth / 2 &&
                mouseY >= buttonY - buttonHeight / 2 && mouseY <= buttonY + buttonHeight / 2){
                    action.action(i);
                    break;
                }
            }
        }
    }

    public void mouseReleased(){
        Action a;
        if(phase == 0){  //ルール読み
            a = new Action(){
                @Override
                public void action(int i) {
                    phase = 1;
                }
            };
        }else if(phase == 1){  //カード選び
            a = new Action(){
                @Override
                public void action(int i) {
                    if(choosables[i]) value.setCard(i);
                    phase = 2;
                }
            };
        }else if(phase == 2){  //宣言選び
            a = new Action(){
                @Override
                public void action(int i) {
                    value.setSay(i);
                    phase = 3;
                }
            };
        }else if(phase == 3){  //相手選び
            a = new Action(){
                @Override
                public void action(int i) {
                    if(choosables[i]){
                        if(i < playerId){
                            value.setTarget(i);
                        }else{
                            value.setTarget(i + 1);
                        }
                    }
                    phase = 4;
                }
            };
        }else if(phase == 4){  //行動確認
            a = new Action(){
                @Override
                public void action(int i) {
                    phase = -1;
                }
            };
        }else if(phase == 5){  //押し付けるか, 当てるか
            a = new Action(){
                @Override
                public void action(int i) {
                    if(i == 0){
                        value.setPass(true);
                    }else if(i == 1){
                        value.setPass(false);
                    }
                    phase = -1;
                }
            };
        }else if(phase == 6){  //予想
            a = new Action(){
                @Override
                public void action(int i) {
                    if(i == 0){
                        value.setGuess(true);
                    }else if(i == 1){
                        value.setGuess(false);
                    }
                    phase = -1;
                }
            };
        }else{  //メッセージ読み確認
            a = new Action(){
                @Override
                public void action(int i) {
                    phase = -1;
                }
            };
        }
        releasedAction(a);
    }
    //**********ボタン生成・動作(ここまで)**********

    //**********ボタン列の表示(ここから)**********
    private void showConfirmButton(int buttonY, String text){
        numOfButton = 1;
        this.buttonY = buttonY;
        interval = 0;
        buttonWidth = 2 * WIDTH / 9;
        buttonHeight = HEIGHT / 16;
        createButtonRect();

        strokeWeight(1);
		fill(0);
        textAlign(CENTER, CENTER);
        textSize(30);
        text(text, WIDTH / 2, buttonY, buttonWidth, buttonHeight);
    }  //OKボタン

    private void showBackButton(){
        strokeWeight(1);
        fill(255);
        rectMode(CENTER);
        rect(8 * WIDTH / 9, HEIGHT / 30, WIDTH / 9, HEIGHT / 30);

		fill(0);
        textAlign(CENTER, CENTER);
        textSize(30);
        text(8 * WIDTH / 9, HEIGHT / 32, WIDTH / 9, HEIGHT / 32);
    }  //戻るボタン

    private void showChoosableCard(){
        showInsects();
        choosables = new boolean[INSECTNUM];

        for (int i = 0; i < INSECTNUM; ++i) {
			choosables[i] = (handCards[i] > 0);
        }
        createCross();
    }

    private void showInsects() {
        numOfButton = INSECTNUM;
        buttonY = 3 * HEIGHT / 8;
        buttonWidth = 7 * WIDTH / 90;
        buttonHeight = HEIGHT / 8;
        interval = WIDTH / 8 - buttonWidth;
        createButtonImage();
    }

    private void showChoosablePlayer(String[] choosables){
        numOfButton = PLAYER - 1;
        buttonY = 7 * HEIGHT / 16;
        interval =  WIDTH / 9;
        buttonWidth = 2 * WIDTH / 9;
        buttonHeight = HEIGHT / 8;
        this.choosables = new boolean[PLAYER - 1];

        for(int i = 0; i < PLAYER; ++i){
            if(i < playerId){
                this.choosables[i] = choosables.equals(yes);
            }else if(i > playerId){
                this.choosables[i - 1] = choosables.equals(yes);
            }
        }

        createButtonRect();

        String[] exceptSelfName = new String[PLAYER - 1];
        for(int i = 0; i < PLAYER; ++i){
            if(i < playerId){
                exceptSelfName[i] = nickNames[i];
            }else if(i > playerId){
                exceptSelfName[i - 1] = nickNames[i];
            }
        }
        createButtonText(exceptSelfName);
        createCross();
    }
    //**********ボタン列の表示(ここまで)**********


    //**********各種画面表示(ここから)**********
    private void showTurn() {  // // Kimura: ターン表示
		fill(0);
		textAlign(CENTER);
		textSize(30);
		text("<Turn " + turn + ">", WIDTH / 2, 9 * HEIGHT / 16);
    }

    private void inputWindow(){  // Kimura: 操作用疑似ウィンドウの表示用背景
        rectMode(CORNER);
		strokeWeight(3);
		fill(0, 90, 30);
		rect(0, HEIGHT / 4, WIDTH, 11 * HEIGHT / 40);
    }

    private void showNormalMessage(String message){  //Kimura: ゲーム中のメッセージ表示
        resetGameView();
        strokeWeight(1);
		fill(0);
		textAlign(CENTER);
		textSize(30);
        text(message, WIDTH / 2, 5 * HEIGHT / 16);
    }

    private void showImportantMessage(String message){  //Kimura: ゲーム中の重要メッセージ表示
        strokeWeight(1);
		fill(255, 0, 0);
		textAlign(CENTER);
		textSize(30);
		text(message, WIDTH / 2, 5 * HEIGHT / 16 + 10);
    }

    private void showMyHandCards() {  //Kimura: 自分の手札の表示
        int sum = 0;
        
		for (int i = 0; i < INSECTNUM; ++i) {
			//fill(color[i]);
			strokeWeight(1);
			for(int j = 0; j < handCards[i]; ++j) {
                int cardX = WIDTH / 2 + (sum - sumOfHandCards[playerId] / 2) * 7 * WIDTH / 90;

				if (sumOfHandCards[playerId] % 2 == 1) {// 手札が奇数の時
					rectMode(CENTER);
					rect(cardX, 7 * HEIGHT / 8, 7 * WIDTH / 90, HEIGHT / 8);
				} else {// 手札が奇数の時
					rectMode(CORNER);
					rect(cardX - WIDTH / 50, 7 * HEIGHT / 8, 7 * WIDTH / 90, HEIGHT / 8);
				}
				sum++;
			}
        }
        
		fill(0);
		textAlign(CENTER);
		textSize(30);
		text(nickNames[playerId], WIDTH / 2, 49 * HEIGHT / 50);
    }

    private void showEnemyHandCards() {  //Kimura: 敵の手札の数を表示
        fill(255, 255, 255);
        strokeWeight(1);
        int i = 1;
		for (int p = 0; p < PLAYER; ++p) {
			if (p != playerId) {
				for (int j = 0; j <  sumOfHandCards[p]; ++j) {
                    
                    int cardX = WIDTH * (2 * i - 1) / 6 + (j -  sumOfHandCards[p] / 2) * WIDTH / 90;

					if (sumOfHandCards[p] % 2 == 1) {// 手札が奇数の時
						rectMode(CENTER);
						rect(cardX, 5 * HEIGHT / 80, 7 * WIDTH / 180, HEIGHT / 16);
					} else {// 偶数の時
						rectMode(CORNER);
						rect(cardX - WIDTH / 100, 5 * HEIGHT / 80, 7 * WIDTH / 180, HEIGHT / 16);
					}
					fill(0);
					textAlign(CENTER);
					textSize(30);
					text(nickNames[p], WIDTH * (2 * i - 1) / 6, 3 * HEIGHT / 80);
				}
				i++;

			}

		}
    }

    private void showMyFieldCards() {  //Kimura: 自分の場の表示
        strokeWeight(1);
		for (int i = 0; i < INSECTNUM; ++i) {
			//fill(color[i]);
			for(int j = 0; j < fieldCards[playerId][i]; ++j) {
				rectMode(CENTER);
				rect(WIDTH / 9 * (i + 1), 7 * HEIGHT / 10 - (j * 3 * HEIGHT / 80), 16 * WIDTH / 225, HEIGHT / 10);
			}
		}
    }

    private void showEnemyFieldCards() {  //Kimura: 敵の場の表示
        rectMode(CORNER);
		strokeWeight(1);
        int k = 1;
		for (int p = 0; p < PLAYER; p++) {
			if (p != playerId) {
				for (int i = 0; i < INSECTNUM; ++i) {
					//fill(color[i]);
					for(int j = 0; j < fieldCards[p][i]; ++j) {
						rect(WIDTH * (2 * k - 1) / 6 + (i - 4) * WIDTH / 30, 3 * HEIGHT / 16 - j * HEIGHT / 160, 7 * WIDTH / 300, 3 * HEIGHT / 80);
					}
				}
				k++;
			}
		}
    }
    //**********各種画面表示(ここまで)**********

    private boolean isBreak(String breakMessage){
        String str = cc.readSingleMessage();
        if(str.equals(breakMessage)){
            return true;
        }else{
            return false;
        }
    }  //無限ループを抜けるかどうか
}

interface Action{
    void action(int i);
}

class ImportantValue{
    private int card;
    private int say;
    private int target;
    private boolean pass;
    private boolean guess;
    private boolean pushCard;
    private boolean backActive;
    private static ImportantValue value = new ImportantValue();

    private ImportantValue(){
        card = -1;
        say = -1;
        target = -1;
        backActive = false;
    }

    static ImportantValue getInstance(){
        return value;
    }

    int getCard(){
        return card;
    }
    int getSay(){
        return say;
    }
    int getTarget(){
        return target;
    }
    boolean getPass(){
        return pass;
    }
    boolean getGuess(){
        return guess;
    }
    boolean getPushCard(){
        return pushCard;
    }
    boolean getBackActive(){
        return backActive;
    }


    void setCard(int card){
        this.card = card;
    }
    void setSay(int say){
        this.say = say;
    }
    void setTarget(int target){
        this.target = target;
    }
    void setPass(boolean pass){
        this.pass = pass;
    }
    void setGuess(boolean guess){
        this.guess = guess;
    }
    void setPushCard(boolean pushCard){
        this.pushCard = pushCard;
    }
    void setBackActive(boolean backActive){
        this.backActive = backActive;
    }
}