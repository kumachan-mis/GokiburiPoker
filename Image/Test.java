package Test;

import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PImage;

public class Test extends PApplet {

	int err = 0;// エラー表示スイッチ用変数
	static boolean start = false;
	int WIDE = 900, HIGHT = 800;// ウィンドウサイズ
	static GameInfo info = new GameInfo();/* ゲーム情報を記憶するフィールド */
	public int[] color = { color(100, 0, 255), color(255, 150, 0), color(30, 45, 60), color(255, 0, 60),
			color(140, 60, 0), color(70, 240, 50), color(230, 255, 0), color(0, 255, 200) };
	// 害虫の色
	public String[] Cards = { "コウモリ.png", "ハエ.png", "ネズミ.png", "サソリ.png", "ゴキブリ.png", "カエル.png", "クモ.png", "カメムシ.png",
			"カード裏.png" };
	// コウモリ→0、ハエ→1、ネズミ→2、サソリ→3、ゴキブリ→4、カエル→5、クモ→6、カメムシ→7
	int sw = 0;// 行動スイッチ用変数
	public PImage[] img = new PImage[9];
	public PImage background;

	public void settings() {// ウィンドウの初期設定
		size(WIDE, HIGHT);
		noLoop();// draw()を一回ずつ表示する
	}

	public void setup() {// その他初期設定
		colorMode(RGB, 256);
		background(0, 130, 45);
		smooth();
		PFont font = createFont("メイリオ", 30);
		textFont(font);
		for (int i = 0; i < 9; i++) {
			img[i] = loadImage(Cards[i]);
		}
		background = loadImage("背景.png");
	}

	public void draw() {// メイン

		if (start) {// ゲームが開始しているなら
			info.Player();
			info.Field();
			imageMode(CORNER);
			image(background, 0, 0, WIDE, HIGHT);// 背景の上書き
			Turn();// ターン表示
			MyHand();// 自分の手札の表示
			MyField();// 自分の場の表示
			EHand();// 敵の手札の表示
			EField();// 敵の場の表示
		} else {// ゲームが開始していないなら
			fill(0);
			textAlign(CENTER, CENTER);
			text("プレイヤーが集まるまでお待ちください...", WIDE / 2, HIGHT / 2);
		}

		InputF();// 操作入力用枠表示
		switch (sw) {// 操作分岐
		case 0:
			WhichCard();// 手札から送るカードを選択
			break;

		case 1:
			WhoSend();// カードの送り先を選択
			break;

		case 2:
			WhatSay();// カードの宣言を選択
			break;

		case 3:
			Recieve();// カード受け取り時の行動を選択
			break;

		case 4:
			Open();// カードを当てるときの行動を選択
			break;

		case 5:
			Answer();// カードを送るときの表面確認
			break;

		case 6:
			OK();// カードを当てたときの表示
			break;

		case 7:
			NG();// カードを外したときの表示
			break;

		case 8:// 手札切れで終わり
			info.EndA();
			strokeWeight(1);
			fill(255, 0, 0);
			textAlign(CENTER);
			textSize(30);
			text("手札がありません！", WIDE / 2, 250);
			textSize(45);
			text("player" + (info.P + 1) + "の負けです！", WIDE / 2, 300);
			textSize(25);
			text("ゲームを終了します。", WIDE / 2, 350);
			break;

		case 9:// 4枚そろって終わり
			info.EndB();
			strokeWeight(1);
			fill(255, 0, 0);
			textAlign(CENTER);
			textSize(30);
			text("player" + (info.P + 1) + "の場に" + info.Name[info.Num] + "のカードが4枚になりました！", WIDE / 2, 250);
			textSize(45);
			text("player" + (info.P + 1) + "の負けです！", WIDE / 2, 300);
			textSize(25);
			text("ゲームを終了します。", WIDE / 2, 350);
			break;
		}

		switch (err) {// エラー表示分岐

		case 1:// 手札切れ
			strokeWeight(1);
			fill(255, 0, 0);
			textAlign(CENTER);
			textSize(20);
			text("そのカードは手札にありません", WIDE / 2, 280);
			System.out.println("そのカードは持っていません");
			err = 0;
			break;

		case 2:// 送れない相手
			strokeWeight(1);
			fill(255, 0, 0);
			textAlign(CENTER);
			textSize(20);
			text("そのプレイヤーには送れません", WIDE / 2, 280);
			System.out.println("そのプレイヤーには送れません");
			err = 0;
			break;

		case 3:// 送れる相手が一人もいない
			strokeWeight(1);
			fill(255, 0, 0);
			textAlign(CENTER);
			textSize(20);
			text("送れるプレイヤーがいません", WIDE / 2, 280);
			System.out.println("送れるプレイヤーがいません");
			err = 0;
			break;
		}

	}

	public void Turn() {// ターン表示
		fill(0);
		textAlign(CENTER);
		textSize(30);
		text("Turn" + info.turns + " player" + (info.P + 1), WIDE / 2, 450);
	}

	public void MyHand() {// 自分の手札の表示
		int j = 0, sum = 0;
		for (int i = 0; i < 8; i++) {
			fill(color[i]);
			strokeWeight(1);
			while (j < info.Hand[info.P][i]) {
				if (info.Hand[info.P][8] % 2 == 1) {// 手札が奇数の時
					imageMode(CENTER);
					image(img[i], (WIDE / 2) + (sum - info.Hand[info.P][8] / 2) * 35, 700, 70, 100);
				} else {// 偶数の時
					imageMode(CORNER);
					image(img[i], (WIDE / 2) - 18 + (sum - info.Hand[info.P][8] / 2) * 35, 650, 70, 100);
				}
				sum++;
				j++;
			}
			j = 0;
		}
		fill(0);
		textAlign(CENTER);
		textSize(30);
		text("player" + (info.P + 1), WIDE / 2, 785);
	}

	public void MyField() {// 自分の場の表示
		int j = 0;
		for (int i = 0; i < 8; i++) {
			fill(color[i]);
			strokeWeight(1);
			while (j < info.Open[info.P][i]) {
				imageMode(CENTER);
				image(img[i], (WIDE / 9 * (i + 1)), 570 - (j * 30), 56, 80);
				j++;
			}
			j = 0;
		}
	}

	public void EHand() {// 敵の手札の数を表示
		int i = 1;
		for (int p = 0; p < info.player; p++) {
			if (p != info.P) {
				for (int j = 0; j < info.Hand[p][8]; j++) {
					fill(255, 255, 255);
					strokeWeight(1);
					if (info.Hand[p][8] % 2 == 1) {// 手札が奇数の時
						imageMode(CENTER);
						image(img[8], (WIDE * (2 * i - 1) / 6) + (j - info.Hand[p][8] / 2) * 10, 75, 35, 50);
					} else {// 偶数の時
						imageMode(CORNER);
						image(img[8], (WIDE * (2 * i - 1) / 6) - 8 + (j - info.Hand[p][8] / 2) * 10, 50, 35, 50);
					}
					fill(0);
					textAlign(CENTER);
					textSize(30);
					text("player" + (p + 1), (WIDE * (2 * i - 1) / 6), 30);
				}
				i++;

			}

		}
	}

	public void EField() {// 敵の場の表示
		int k = 1;
		for (int p = 0; p < info.player; p++) {
			if (p != info.P) {
				int j = 0;
				for (int i = 0; i < 8; i++) {
					fill(color[i]);
					while (j < info.Open[p][i]) {
						imageMode(CORNER);
						strokeWeight(1);
						image(img[i], (WIDE * (2 * k - 1) / 6) + (i - 4) * 30, 150 - (j * 5), 21, 30);
						j++;
					}
					j = 0;
				}
				k++;
			}
		}
	}

	public void InputF() {// 操作用疑似ウィンドウの表示用背景
		rectMode(CORNER);
		strokeWeight(3);
		fill(0, 90, 30);
		rect(0, 200, 900, 220);
	}

	public void WhichCard() {// 送る手札の選択
		strokeWeight(1);
		fill(0);
		textAlign(CENTER);
		textSize(30);
		text("送るカードを選んでください", WIDE / 2, 250);
		info.ShowHand();
		for (int i = 0; i < 8; i++) {
			fill(color[i]);
			imageMode(CORNER);
			strokeWeight(1);
			image(img[i], (WIDE / 8 * i) + 25, 300, 70, 100);
			if (info.Hand[info.P][i] == 0) {// もし手札になかったら
				strokeWeight(3);
				fill(0);
				line((WIDE / 8 * i) + 25, 300, (WIDE / 8 * i) + 95, 400);// そのカードに×を表示
				line((WIDE / 8 * i) + 95, 300, (WIDE / 8 * i) + 25, 400);
			}
		}
	}

	public void WhoSend() {// カードの送り先を選択
		strokeWeight(1);
		fill(0);
		textAlign(CENTER);
		textSize(30);
		text("送る相手を選んでください", WIDE / 2, 250);
		int j = 1;
		for (int i = 0; i < info.player; i++) {
			fill(255);
			rectMode(CENTER);
			strokeWeight(1);
			if (i != info.P) {
				rect((WIDE * (2 * j - 1) / 6), 350, 200, 100);
				strokeWeight(1);
				fill(0);
				textAlign(CENTER);
				textSize(30);
				text("Player" + (i + 1), (WIDE * (2 * j - 1) / 6), 365);
				if (info.Send[i] == 1) {
					strokeWeight(3);
					fill(0);
					line((WIDE * (2 * j - 1) / 6) - 100, 300, (WIDE * (2 * j - 1) / 6) + 100, 400);
					line((WIDE * (2 * j - 1) / 6) + 100, 300, (WIDE * (2 * j - 1) / 6) - 100, 400);
				}
				j++;
			}
		}
	}

	public void WhatSay() {
		strokeWeight(1);
		fill(0);
		textAlign(CENTER);
		textSize(30);
		text("宣言するカード名を選んでください", WIDE / 2, 250);
		info.ShowHand();
		for (int i = 0; i < 8; i++) {
			fill(color[i]);
			imageMode(CORNER);
			strokeWeight(1);
			image(img[i], (WIDE / 8 * i) + 25, 300, 70, 100);
		}
	}

	public void Recieve() {
		strokeWeight(1);
		fill(0);
		textAlign(CENTER);
		textSize(30);
		text("Player" + (info.From + 1) + "さんからカードが送られました\n" + "行動を選んでください", WIDE / 2, 250);
		for (int i = 1; i < 3; i++) {
			fill(255);
			rectMode(CENTER);
			strokeWeight(1);
			rect((WIDE * (2 * i - 1) / 4), 360, 300, 70);
		}
		strokeWeight(1);
		fill(0);
		textAlign(CENTER);
		textSize(25);
		text("カードをめくる", (WIDE / 4), 370);
		text("他の人に渡す", (WIDE * 3 / 4), 370);
	}

	public void Open() {
		strokeWeight(1);
		fill(0);
		textAlign(CENTER);
		textSize(30);
		text("Player" + (info.From + 1) + "はこのカードを「" + info.Name[info.Say] + "」だと言っています。\nこのカードは…", WIDE / 2, 250);
		for (int i = 1; i < 3; i++) {
			fill(255);
			rectMode(CENTER);
			strokeWeight(1);
			rect((WIDE * (2 * i - 1) / 4), 360, 300, 70);
		}
		strokeWeight(1);
		fill(0);
		textAlign(CENTER);
		textSize(25);
		text(info.Name[info.Say] + "である", (WIDE / 4), 370);
		text(info.Name[info.Say] + "ではない", (WIDE * 3 / 4), 370);
	}

	public void Answer() {
		strokeWeight(1);
		fill(0);
		textAlign(CENTER);
		textSize(30);
		text("このカードは" + info.Name[info.Num] + "です", WIDE / 2, 250);
		fill(color[info.Num]);
		imageMode(CENTER);
		strokeWeight(1);
		image(img[info.Num], WIDE / 2, 310, 70, 100);

		fill(255);
		rect(WIDE / 2, 390, 200, 50);
		fill(0);
		text("OK", WIDE / 2, 400);
	}

	public void OK() {
		strokeWeight(1);
		fill(0);
		textAlign(CENTER);
		textSize(25);
		text("このカードは" + info.Name[info.Num] + "でした", WIDE / 2, 225);
		fill(255, 0, 0);
		text("正解です！", WIDE / 2, 250);

		fill(color[info.Num]);
		imageMode(CENTER);
		strokeWeight(1);
		image(img[info.Num], WIDE / 2, 310, 70, 100);

		rectMode(CENTER);
		fill(255);
		rect(WIDE / 2, 390, 200, 50);
		fill(0);
		text("OK", WIDE / 2, 400);

		info.OpenCard();
	}

	public void NG() {
		strokeWeight(1);
		fill(0);
		textAlign(CENTER);
		textSize(25);
		text("このカードは" + info.Name[info.Num] + "でした", WIDE / 2, 225);
		fill(255, 0, 0);
		text("不正解です！", WIDE / 2, 250);

		fill(color[info.Num]);
		imageMode(CENTER);
		strokeWeight(1);
		image(img[info.Num], WIDE / 2, 310, 70, 100);

		rectMode(CENTER);
		fill(255);
		rect(WIDE / 2, 390, 200, 50);
		fill(0);
		text("OK", WIDE / 2, 400);

		info.OpenCard();
	}

	public void mousePressed() {
		switch (sw) {
		case 0:// 手札のクリック判定
			System.out.println("a");
			for (int i = 0; i < 8; i++) {
				if (mouseX >= (WIDE / 8 * i) + 25 && mouseX <= (WIDE / 8 * i) + 95 && mouseY >= 300 && mouseY <= 400) {
					if (info.Hand[info.P][i] == 0) {// 手札にない
						err = 1;// エラー変数を更新して再度ドロー
						redraw();
					} else {// 手札にあるとき
						info.Num = i;
						info.SendA();
						sw = 1;
						redraw();
					}
				}
			}
			break;

		case 1:// 送り相手のクリック判定
			int j = 1;
			for (int i = 0; i < info.player; i++) {
				if (i != info.P) {
					if (mouseX >= (WIDE * (2 * j - 1) / 6) - 100 && mouseX <= (WIDE * (2 * j - 1) / 6) + 100
							&& mouseY >= 300 && mouseY <= 400) {
						if (info.Send[i] == 1) {// 送れない
							err = 2;
							redraw();
						} else {
							info.To = i;
							info.SendB();
							sw = 2;
							redraw();
						}
					}
					j++;
				}
			}
			break;

		case 2:// 宣言のクリック判定
			for (int i = 0; i < 8; i++) {
				if (mouseX >= (WIDE / 8 * i) + 25 && mouseX <= (WIDE / 8 * i) + 95 && mouseY >= 300 && mouseY <= 400) {
					info.Say = i;
					info.SendC();
					sw = 3;
					redraw();
				}
			}
			break;

		case 3:// 受け取り時のクリック判定
			if (mouseX >= (WIDE / 4) - 150 && mouseX <= (WIDE / 4) + 150 && mouseY >= 325 && mouseY <= 395) {// 表にする
				sw = 4;
				redraw();
			}
			if (mouseX >= (WIDE * 3 / 4) - 150 && mouseX <= (WIDE * 3 / 4) + 150 && mouseY >= 325 && mouseY <= 395) {// 他に送る
				if (info.ACanSend()) {
					sw = 5;
					redraw();
				} else {// 送れるプレイヤーがいない
					err = 3;
					redraw();
				}
			}
			break;

		case 4:// 表にするときのクリック判定
			if (mouseX >= (WIDE / 4) - 150 && mouseX <= (WIDE / 4) + 150 && mouseY >= 325 && mouseY <= 395) {// ～である
				info.choise = 0;
				if (info.Num == info.Say) {
					sw = 6;
					redraw();
				} else {
					sw = 7;
					redraw();
				}

			}
			if (mouseX >= (WIDE * 3 / 4) - 150 && mouseX <= (WIDE * 3 / 4) + 150 && mouseY >= 325 && mouseY <= 395) {// ～でない
				info.choise = 1;
				if (info.Num != info.Say) {
					sw = 6;
					redraw();
				} else {
					sw = 7;
					redraw();
				}
			}
			break;

		case 5:// 表を確認したときのクリック判定
			if (mouseX >= WIDE / 2 - 100 && mouseX <= WIDE / 2 + 100 && mouseY >= 365 && mouseY <= 415) {
				sw = 1;// 送り先選択に移る
				redraw();
			}
			break;

		case 6:// 正解したときのクリック判定
			if (mouseX >= WIDE / 2 - 100 && mouseX <= WIDE / 2 + 100 && mouseY >= 365 && mouseY <= 415) {
				if (info.CheckField()) {
					sw = 9;
					redraw();
				} else {
					info.NextTurn();
					if (info.CheckHand()) {
						sw = 8;
						redraw();
					} else {
						sw = 0;// 送り先選択に移る
						redraw();
					}
				}
			}
			break;

		case 7:// 不正解のときのクリック判定
			if (mouseX >= WIDE / 2 - 100 && mouseX <= WIDE / 2 + 100 && mouseY >= 365 && mouseY <= 415) {
				if (info.CheckField()) {
					sw = 9;
					redraw();
				} else {
					info.NextTurn();
					if (info.CheckHand()) {
						sw = 8;
						redraw();
					} else {
						sw = 0;// 送り先選択に移る
						redraw();
					}
				}
			}
			break;
		}

	}

	public static void main(String[] args) {
		PApplet.main("Test.Test");
		start = true;
	}

}