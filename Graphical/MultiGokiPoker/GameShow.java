import processing.core.PApplet;
import processing.core.PImage;

class GameShow{

    public static final String[] insectsImage =
    {"", "", "", "", "", "", "", ""};
    private int PLAYER;
    private int INSECTNUM;
    private int WIDTH;
    private int HEIGHT;
    private GUIClient pa;

    public GameShow(GUIClient pa, int PLAYER, int INSECTNUM){
        this.PLAYER = PLAYER;
        this.INSECTNUM = INSECTNUM;
        this.WIDTH = pa.WIDTH;
        this.HEIGHT = pa.HEIGHT;
        this.pa = pa;
    }

    public void showTurn(int turn) {  // // Kimura: ターン表示
		pa.fill(0);
		pa.textAlign(pa.CENTER);
		pa.textSize(30);
		pa.text("<Turn " + turn + ">", WIDTH / 2, 9 * HEIGHT / 16);
    }

    public void inputWindow(){  // Kimura: 操作用疑似ウィンドウの表示用背景
        pa.rectMode(pa.CORNER);
		pa.strokeWeight(3);
		pa.fill(0, 90, 30);
		pa.rect(0, HEIGHT / 4, WIDTH, 11 * HEIGHT / 40);
    }

    public void showNormalMessage(String message){  //Kimura: ゲーム中のメッセージ表示
        pa.strokeWeight(1);
		pa.fill(0);
		pa.textAlign(pa.CENTER);
		pa.textSize(30);
        pa.text(message, WIDTH / 2, 5 * HEIGHT / 16);
    }

    public void showImportantMessage(String message){  //Kimura: ゲーム中の重要メッセージ表示
        pa.strokeWeight(1);
		pa.fill(255, 0, 0);
		pa.textAlign(pa.CENTER);
		pa.textSize(30);
		pa.text(message, WIDTH / 2, 5 * HEIGHT / 16 + 10);
    }

    public void showMyHandCards(int sumOfMyHandCards, int[] handCards, String myNickName) {  //Kimura: 自分の手札の表示
        int sum = 0;
        
		for (int i = 0; i < INSECTNUM; ++i) {
			//fill(color[i]);
			pa.strokeWeight(1);
			for(int j = 0; j < handCards[i]; ++j) {
                int cardX = WIDTH / 2 + (sum - sumOfMyHandCards / 2) * 7 * WIDTH / 90;

				if (sumOfMyHandCards % 2 == 1) {// 手札が奇数の時
					pa.rectMode(pa.CENTER);
					pa.rect(cardX, 7 * HEIGHT / 8, 7 * WIDTH / 90, HEIGHT / 8);
				} else {// 手札が奇数の時
					pa.rectMode(pa.CORNER);
					pa.rect(cardX - WIDTH / 50, 7 * HEIGHT / 8, 7 * WIDTH / 90, HEIGHT / 8);
				}
				sum++;
			}
        }
        
		pa.fill(0);
		pa.textAlign(pa.CENTER);
		pa.textSize(30);
		pa.text(myNickName, WIDTH / 2, 49 * HEIGHT / 50);
    }

    public void showEnemyHandCards(int playerId, int[] sumOfHandCards, String[] nickNames) {  //Kimura: 敵の手札の数を表示
        pa.fill(255, 255, 255);
        pa.strokeWeight(1);
        int i = 1;
		for (int p = 0; p < PLAYER; ++p) {
			if (p != playerId) {
				for (int j = 0; j <  sumOfHandCards[p]; ++j) {
                    
                    int cardX = WIDTH * (2 * i - 1) / 6 + (j -  sumOfHandCards[p] / 2) * WIDTH / 90;

					if (sumOfHandCards[p] % 2 == 1) {// 手札が奇数の時
						pa.rectMode(pa.CENTER);
						pa.rect(cardX, 5 * HEIGHT / 80, 7 * WIDTH / 180, HEIGHT / 16);
					} else {// 偶数の時
						pa.rectMode(pa.CORNER);
						pa.rect(cardX - WIDTH / 100, 5 * HEIGHT / 80, 7 * WIDTH / 180, HEIGHT / 16);
					}
					pa.fill(0);
					pa.textAlign(pa.CENTER);
					pa.textSize(30);
					pa.text(nickNames[p], WIDTH * (2 * i - 1) / 6, 3 * HEIGHT / 80);
				}
				i++;

			}

		}
    }

    public void showMyFieldCards(int[] myFieldCards) {  //Kimura: 自分の場の表示
        pa.strokeWeight(1);
		for (int i = 0; i < INSECTNUM; ++i) {
			//fill(color[i]);
			for(int j = 0; j < myFieldCards[i]; ++j) {
				pa.rectMode(pa.CENTER);
				pa.rect(WIDTH / 9 * (i + 1), 7 * HEIGHT / 10 - (j * 3 * HEIGHT / 80), 16 * WIDTH / 225, HEIGHT / 10);
			}
		}
    }

    public void showEnemyFieldCards(int playerId, int[][] fieldCards) {  //Kimura: 敵の場の表示
        pa.rectMode(pa.CORNER);
		pa.strokeWeight(1);
        int k = 1;
		for (int p = 0; p < PLAYER; p++) {
			if (p != playerId) {
				for (int i = 0; i < INSECTNUM; ++i) {
					//fill(color[i]);
					for(int j = 0; j < fieldCards[p][i]; ++j) {
						pa.rect(WIDTH * (2 * k - 1) / 6 + (i - 4) * WIDTH / 30, 3 * HEIGHT / 16 - j * HEIGHT / 160, 7 * WIDTH / 300, 3 * HEIGHT / 80);
					}
				}
				k++;
			}
		}
    }
}