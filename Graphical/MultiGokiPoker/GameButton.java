import processing.core.PApplet;

interface Action{
    void action(int buttonNum);
}

class GameButton{

    private int INSECTNUM;
    private int PLAYER;
    private int WIDTH;
    private int HEIGHT;
    public GUIClient pa;
    private ButtonTemplate bt;
    ImportantValue value;

    private Action noAction = new Action(){
        public void action(int buttonNum){
        }
    };
    
    private Action[] actions = {
        new Action(){
            public void action(int buttonNum) {
                //フェーズ1の時の動作
            }
        },
        new Action(){
            public void action(int buttonNum) {
                //フェーズ2の時の動作
            }
        },
        new Action(){
            public void action(int buttonNum) {
                //フェーズ3の時の動作
            }
        },
        new Action(){
            public void action(int buttonNum) {
                //フェーズ5の時の動作
            }
        },
        new Action(){
            public void action(int buttonNum) {
                //フェーズ6の時の動作
            }
        },
    };

    public GameButton(GUIClient pa, ImportantValue value, int PLAYER, int INSECTNUM){
        this.PLAYER = PLAYER;
        this.INSECTNUM = INSECTNUM;
        this.WIDTH = pa.WIDTH;
        this.HEIGHT = pa.HEIGHT;
        this.pa = pa;
        this.bt = new ButtonTemplate(pa, INSECTNUM);
        this.value = value;
    }

    public void showConfirmButton(int buttonY, String confirm){
        int buttonX = WIDTH / 2;
        int buttonWidth = 2 * WIDTH / 9;
        int buttonHeight = HEIGHT / 16;

        pa.strokeWeight(1);
        pa.fill(255);
        pa.rectMode(pa.CENTER);
        pa.rect(buttonX, buttonY, buttonWidth, buttonHeight);

        pa.strokeWeight(1);
		pa.fill(0);
        pa.textAlign(pa.CENTER, pa.CENTER);
        pa.textSize(30);
        pa.text(confirm, buttonX, buttonY, buttonWidth, buttonHeight);
    }  //OKボタン

    public void showBackButton(){
        int buttonX = 8 * WIDTH / 9;
        int buttonY = HEIGHT / 30;
        int buttonWidth = WIDTH / 9;
        int buttonHeight = HEIGHT / 30;

        bt.setButtonData(1, buttonY, 0, buttonWidth, buttonHeight);
        pa.strokeWeight(1);
        pa.fill(255);
        pa.rectMode(pa.CENTER);
        pa.rect(buttonX, buttonY, buttonWidth, buttonHeight);

        pa.strokeWeight(1);
		pa.fill(0);
        pa.textAlign(pa.CENTER, pa.CENTER);
        pa.textSize(30);
        pa.text("戻る", buttonX, buttonY, buttonWidth, buttonHeight);
    }  //戻るボタン

    public void showYesOrNoButton(String[] yes_or_no){
        int numOfButton = 2;
        int buttonY = 9 * HEIGHT / 20;
        int buttonWidth = WIDTH / 3;
        int buttonHeight = 7 * HEIGHT / 80;
        int interval = WIDTH / 6;
        bt.setButtonData(numOfButton, buttonY, interval, buttonWidth, buttonHeight);
        bt.createButtonRect();
        bt.createButtonText(yes_or_no); 
    }  //2択ボタン

    public void showChoosableCard(int[] handCards){
        showInsects();
        boolean[] choosables = new boolean[INSECTNUM];

        for (int i = 0; i < INSECTNUM; ++i) {
			choosables[i] = (handCards[i] > 0);
        }
        bt.createCross(choosables);
    }  //押し付けられるカードのボタン

    public void showInsects() {
        int numOfButton = INSECTNUM;
        int buttonY = 3 * HEIGHT / 8;
        int buttonWidth = 7 * WIDTH / 90;
        int buttonHeight = HEIGHT / 8;
        int interval = WIDTH / 8 - buttonWidth;

        bt.setButtonData(numOfButton, buttonY, interval, buttonWidth, buttonHeight);
        bt.createButtonImage();
    }  //宣言する昆虫を選ぶボタン

    public void showChoosablePlayer(int playerId, String[] nickNames, String[] choosables, String yes){
        int numOfButton = PLAYER - 1;
        int buttonY = 7 * HEIGHT / 16;
        int interval =  WIDTH / 9;
        int buttonWidth = 2 * WIDTH / 9;
        int buttonHeight = HEIGHT / 8;

        bt.setButtonData(numOfButton, buttonY, interval, buttonWidth, buttonHeight);

        boolean[] cbs = new boolean[PLAYER - 1];

        for(int i = 0; i < PLAYER; ++i){
            if(i < playerId){
                cbs[i] = choosables[i].equals(yes);
            }else if(i > playerId){
                cbs[i - 1] = choosables[i].equals(yes);
            }
        }

        String[] exceptSelfName = new String[PLAYER - 1];
        for(int i = 0; i < PLAYER; ++i){
            if(i < playerId){
                exceptSelfName[i] = nickNames[i];
            }else if(i > playerId){
                exceptSelfName[i - 1] = nickNames[i];
            }
        }

        bt.createButtonRect();
        bt.createButtonText(exceptSelfName);
        bt.createCross(cbs);
    }  //押し付けることのできる人のボタン

    public int getNextPhase(int phase){
        Action a;
        if(phase >= 1 && phase <= 3){
            a = actions[phase - 1];
        }else if(phase >= 5 && phase <= 6){
            a = actions[phase - 2];
        }else{
            a = noAction;
        }
        return bt.releasedAction(a, phase, value.getBackActive());
    }  //ボタンが押された時の動作をphaseによって選択
}