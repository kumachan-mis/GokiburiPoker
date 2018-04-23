package GokiPoker;

public class Main {
	public static void main(String[] argv) {
		GameInfo info = new GameInfo();/* ゲーム情報を記憶するフィールド */

		while (true) {/* ゲームの決着がつくまで無限ループ */
			info.Player();/* ターン情報 */
			info.Field();/* 盤上の可視化 */
			if (info.CheckHand()) {/* 手札がない場合 */
				info.EndA();/* エラー表示 */
				break;/* ゲームの終了 */
			} else {
				info.SendA();/* 行動の読み込み */
			}
			if (info.CheckField()) {/* 生き物カードが4枚になった時 */
				info.EndB();
				break;
			}
			info.NextTurn();/* 情報更新 */
		}
		info.Result();
	}
}
