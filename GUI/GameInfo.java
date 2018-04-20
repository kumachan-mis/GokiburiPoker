package Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;

public class GameInfo {

	public static final int max = 64;/* カードの枚数 */
	public int turns = 0;/* ターン数 */
	public int player = 4;/* プレイヤーの数 */
	public int P;/* 現在のプレイヤー */
	public int From;
	public int To;
	public int Say;
	public int Num;
	public int choise;
	public int[] Send = new int[player];/* すでに送った人の記憶 */
	public int[][] Open = new int[player][8];/* 各プレイヤーの表になった害虫のカウント */
	public int[][] Hand = new int[player][9];/* 手札の初期データ */
	public String[] Name = { "コウモリ", "ハエ", "ネズミ", "サソリ", "ゴキブリ", "カエル", "クモ", "カメムシ" };/* 生き物の種類 */
	// コウモリ→0、ハエ→1、ネズミ→2、サソリ→3、ゴキブリ→4、カエル→5、クモ→6、カメムシ→7

	public GameInfo() {/* ゲーム情報の初期化 */
		turns = 1;
		P = 0;
		for (int i = 0; i < player; i++) {
			Send[i] = 0;
			for (int j = 0; j < 8; j++) {
				Open[i][j] = 0;
				Hand[i][j] = 0;
			}
		}
		ArrayList<Integer> list = new ArrayList<Integer>();/* カードのリストを作成 */
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				list.add(j);/* リストに64個の数を入力 */
			}
		}
		Collections.shuffle(list);/* リストをシャッフル */
		for (int i = 0; i < 64; i++) {/* 各プレイヤーに手札を配る */
			int a = list.get(i);/* リストのi番目の数字を取得 */
			int b = i / (64 / player);
			for (int j = 0; j < 8; j++) {
				if (a == j) {/* その数字の回数をカウント。これが各生き物のカードの数 */
					Hand[b][j] += 1;
				}
			}
		}
		for (int i = 0; i < player; i++) {// 追記、手札の数
			Hand[i][8] = (64 / player);
		}
	}

	public void NextTurn() {/* 次のターン */
		turns++;
		for (int i = 0; i < player; i++) {
			Send[i] = 0;
		}
	}

	public void Player() {
		System.out.println("[Turn" + turns + "]");
		System.out.println("今のプレイヤーはplayer" + (P + 1) + "です");
	}

	public void Field() {/* 場の情報を可視化 */
		for (int i = 0; i < player; i++) {
			System.out.println("<player" + (i + 1) + ">");
			for (int j = 0; j < 8; j++) {
				if (Open[i][j] != 0) {
					System.out.print(Name[j] + ":" + Open[i][j] + "枚　");
				}
			}
			System.out.println();
		}
	}

	public void ShowHand() {/* 手持ちのカードを可視化 */
		for (int i = 0; i < 8; i++) {
			if (Hand[P][i] != 0) {
				System.out.print(Name[i] + "(" + i + ")" + ":" + Hand[P][i] + "枚　");
			}
		}
		System.out.println();
	}

	public void ShowSend() {
		for (int i = 0; i < player; i++) {
			if (i != P && Send[i] == 0) {
				System.out.print("player" + (i + 1) + "　");
			}
		}
		System.out.println();
	}

	public void ShowNum() {
		for (int i = 0; i < 8; i++) {
			System.out.print(Name[i] + "(" + i + ")　");
		}
		System.out.println();
	}

	public boolean CheckHand() {/* 判定対象プレイヤーを引数にする */
		for (int i = 0; i < 8; i++) {
			if (Hand[P][i] != 0) {/* 手札がある時 */
				return false;
			}
		}
		/* 手札がないとき */
		return true;
	}

	public boolean CheckField() {/* 判定対象プレイヤーを引数にする */
		if (Open[P][Num] == 4) {/* そのプレイヤーの現在追加された種類のカードが4枚になったら */
			return true;/* 動物カードが4枚以上で負け */
		}
		return false;/* それ以外はスルー */
	}

	public boolean CanSend(int to) {/* すでに相手に送っているかどうかをチェック */
		if (Send[to] != 0) {/* すでに送っていた場合 */
			return true;
		}
		return false;
	}

	public boolean ACanSend() {/* 送り先があるかどうかのチェック */
		for (int i = 0; i < player; i++) {
			if (i != P && CanSend(i) == false) {
				return true;
			}
		}
		return false;
	}

	public boolean NoHand(int num) {/* 手持ちにあるかどうかをチェック */
		if (Hand[P][8] == 0) {
			return true;
		}
		return false;
	}

	public int Reader() {
		String line = null;
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));/* 選択の読み取り */
		try {
			line = reader.readLine();
		} catch (IOException e) {
		}
		return Integer.parseInt(line);
	}

	public void SendA() {
		// int num = 0;
		System.out.println("どのカードを送りますか？（括弧内の数字入力）今の手札は");
		ShowHand();/* プレイヤーの手札を表示 */
		// num = Reader();/* カードの種類 */
		/*
		 * if (NoHand(num)) { System.out.println("そのカードは持っていません"); SendA();
		 * return; }
		 */
		// Num = num;
		System.out.println(Num);
		Hand[P][Num]--;/* 手札から指定の生き物カードを出す */
		Hand[P][8]--;
		// SendB();
	}

	public void SendB() {
		// int to = 0, say = 0;
		System.out.println("誰にカードを送りますか？(数字入力)　今送れるのは");
		ShowSend();/* 送れる相手を表示 */
		/*
		 * to = Reader() - 1; if (CanSend(to)) {
		 * System.out.println("そのプレイヤーには送れません"); SendB(); return; }
		 */
		System.out.println(To);
	}

	public void SendC() {
		System.out.println("なんの動物と宣言しますか？(数字入力)　動物の種類は");
		ShowNum();/* 動物に対応した数字を表示 */
		/* say = Reader();/* 宣言する動物 */

		// カードを送る
		From = P;
		P = To;
		Send[From]++;
		System.out.println("player" + (From + 1) + "　「このカードは" + Name[Say] + "です。」");
		System.out.println("player" + (P + 1) + "さんにカードが送られました");
		System.out.println();
		// Receive();
	}

	public void OpenCard() {
		System.out.println("player" + (From + 1) + "はこのカードを「" + Name[Say] + "」だと言っています。このカードは…");
		System.out.println("(0)" + Name[Say] + "です。　(1)" + Name[Say] + "ではありません。");
		// int choise = 0;
		// choise = Reader();/* 行動の選択 */

		System.out.println("このカードは" + Name[Num] + "でした！");
		if ((choise == 0 && Num == Say) || (choise == 1 && Num != Say)) {/* 正解したとき */
			System.out.println("player" + (From + 1) + "のミスです！");
			Open[From][Num]++;/* カードの送り主に表向きで追加 */
			P = From;
		} else {/* 不正解の時 */
			System.out.println("player" + (P + 1) + "のミスです！");
			Open[P][Num]++;/* 現在のプレイヤーに表向きで追加 */
		}
		System.out.println();
	}

	public void Receive() {
		// int choise;
		System.out.println("player" + (P + 1) + "さん");
		System.out.println("カードを送りますか(0)？それとも宣言して表にしますか(1)？");
		System.out.println("括弧内の数字を入力してください。");

		// choise = Reader();/* 行動の選択 */
		// if (choise == 1) {
		// OpenCard();
		// } else {
		// if (ACanSend()) {/* カード送る先がまだ残っている時 */
		// System.out.println("このカードは「" + Name[Num] + "」です。");
		// SendB();/* カードを送る */
		// } else {
		// System.out.println("カードを送れるプレイヤーがいません。宣言して表にします。");
		// OpenCard();
		// }
		// }

	}

	public void EndA() {
		System.out.println("手札がありません！");
	}

	public void EndB() {
		System.out.println("player" + (P + 1) + "の場に" + Name[Num] + "のカードが4枚になりました！");
	}

	public void Result() {
		System.out.println("player" + (P + 1) + "の負けです！");
		System.out.println("ゲームを終了します。");
	}
}