package Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;

public class GameInfo {

	public static final int max = 64;/* �J�[�h�̖��� */
	public int turns = 0;/* �^�[���� */
	public int player = 4;/* �v���C���[�̐� */
	public int P;/* ���݂̃v���C���[ */
	public int From;
	public int To;
	public int Say;
	public int Num;
	public int choise;
	public int[] Send = new int[player];/* ���łɑ������l�̋L�� */
	public int[][] Open = new int[player][8];/* �e�v���C���[�̕\�ɂȂ����Q���̃J�E���g */
	public int[][] Hand = new int[player][9];/* ��D�̏����f�[�^ */
	public String[] Name = { "�R�E����", "�n�G", "�l�Y�~", "�T�\��", "�S�L�u��", "�J�G��", "�N��", "�J�����V" };/* �������̎�� */
	// �R�E������0�A�n�G��1�A�l�Y�~��2�A�T�\����3�A�S�L�u����4�A�J�G����5�A�N����6�A�J�����V��7

	public GameInfo() {/* �Q�[�����̏����� */
		turns = 1;
		P = 0;
		for (int i = 0; i < player; i++) {
			Send[i] = 0;
			for (int j = 0; j < 8; j++) {
				Open[i][j] = 0;
				Hand[i][j] = 0;
			}
		}
		ArrayList<Integer> list = new ArrayList<Integer>();/* �J�[�h�̃��X�g���쐬 */
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				list.add(j);/* ���X�g��64�̐������ */
			}
		}
		Collections.shuffle(list);/* ���X�g���V���b�t�� */
		for (int i = 0; i < 64; i++) {/* �e�v���C���[�Ɏ�D��z�� */
			int a = list.get(i);/* ���X�g��i�Ԗڂ̐������擾 */
			int b = i / (64 / player);
			for (int j = 0; j < 8; j++) {
				if (a == j) {/* ���̐����̉񐔂��J�E���g�B���ꂪ�e�������̃J�[�h�̐� */
					Hand[b][j] += 1;
				}
			}
		}
		for (int i = 0; i < player; i++) {// �ǋL�A��D�̐�
			Hand[i][8] = (64 / player);
		}
	}

	public void NextTurn() {/* ���̃^�[�� */
		turns++;
		for (int i = 0; i < player; i++) {
			Send[i] = 0;
		}
	}

	public void Player() {
		System.out.println("[Turn" + turns + "]");
		System.out.println("���̃v���C���[��player" + (P + 1) + "�ł�");
	}

	public void Field() {/* ��̏������� */
		for (int i = 0; i < player; i++) {
			System.out.println("<player" + (i + 1) + ">");
			for (int j = 0; j < 8; j++) {
				if (Open[i][j] != 0) {
					System.out.print(Name[j] + ":" + Open[i][j] + "���@");
				}
			}
			System.out.println();
		}
	}

	public void ShowHand() {/* �莝���̃J�[�h������ */
		for (int i = 0; i < 8; i++) {
			if (Hand[P][i] != 0) {
				System.out.print(Name[i] + "(" + i + ")" + ":" + Hand[P][i] + "���@");
			}
		}
		System.out.println();
	}

	public void ShowSend() {
		for (int i = 0; i < player; i++) {
			if (i != P && Send[i] == 0) {
				System.out.print("player" + (i + 1) + "�@");
			}
		}
		System.out.println();
	}

	public void ShowNum() {
		for (int i = 0; i < 8; i++) {
			System.out.print(Name[i] + "(" + i + ")�@");
		}
		System.out.println();
	}

	public boolean CheckHand() {/* ����Ώۃv���C���[�������ɂ��� */
		for (int i = 0; i < 8; i++) {
			if (Hand[P][i] != 0) {/* ��D�����鎞 */
				return false;
			}
		}
		/* ��D���Ȃ��Ƃ� */
		return true;
	}

	public boolean CheckField() {/* ����Ώۃv���C���[�������ɂ��� */
		if (Open[P][Num] == 4) {/* ���̃v���C���[�̌��ݒǉ����ꂽ��ނ̃J�[�h��4���ɂȂ����� */
			return true;/* �����J�[�h��4���ȏ�ŕ��� */
		}
		return false;/* ����ȊO�̓X���[ */
	}

	public boolean CanSend(int to) {/* ���łɑ���ɑ����Ă��邩�ǂ������`�F�b�N */
		if (Send[to] != 0) {/* ���łɑ����Ă����ꍇ */
			return true;
		}
		return false;
	}

	public boolean ACanSend() {/* ����悪���邩�ǂ����̃`�F�b�N */
		for (int i = 0; i < player; i++) {
			if (i != P && CanSend(i) == false) {
				return true;
			}
		}
		return false;
	}

	public boolean NoHand(int num) {/* �莝���ɂ��邩�ǂ������`�F�b�N */
		if (Hand[P][8] == 0) {
			return true;
		}
		return false;
	}

	public int Reader() {
		String line = null;
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));/* �I���̓ǂݎ�� */
		try {
			line = reader.readLine();
		} catch (IOException e) {
		}
		return Integer.parseInt(line);
	}

	public void SendA() {
		// int num = 0;
		System.out.println("�ǂ̃J�[�h�𑗂�܂����H�i���ʓ��̐������́j���̎�D��");
		ShowHand();/* �v���C���[�̎�D��\�� */
		// num = Reader();/* �J�[�h�̎�� */
		/*
		 * if (NoHand(num)) { System.out.println("���̃J�[�h�͎����Ă��܂���"); SendA();
		 * return; }
		 */
		// Num = num;
		System.out.println(Num);
		Hand[P][Num]--;/* ��D����w��̐������J�[�h���o�� */
		Hand[P][8]--;
		// SendB();
	}

	public void SendB() {
		// int to = 0, say = 0;
		System.out.println("�N�ɃJ�[�h�𑗂�܂����H(��������)�@�������̂�");
		ShowSend();/* ����鑊���\�� */
		/*
		 * to = Reader() - 1; if (CanSend(to)) {
		 * System.out.println("���̃v���C���[�ɂ͑���܂���"); SendB(); return; }
		 */
		System.out.println(To);
	}

	public void SendC() {
		System.out.println("�Ȃ�̓����Ɛ錾���܂����H(��������)�@�����̎�ނ�");
		ShowNum();/* �����ɑΉ�����������\�� */
		/* say = Reader();/* �錾���铮�� */

		// �J�[�h�𑗂�
		From = P;
		P = To;
		Send[From]++;
		System.out.println("player" + (From + 1) + "�@�u���̃J�[�h��" + Name[Say] + "�ł��B�v");
		System.out.println("player" + (P + 1) + "����ɃJ�[�h�������܂���");
		System.out.println();
		// Receive();
	}

	public void OpenCard() {
		System.out.println("player" + (From + 1) + "�͂��̃J�[�h���u" + Name[Say] + "�v���ƌ����Ă��܂��B���̃J�[�h�́c");
		System.out.println("(0)" + Name[Say] + "�ł��B�@(1)" + Name[Say] + "�ł͂���܂���B");
		// int choise = 0;
		// choise = Reader();/* �s���̑I�� */

		System.out.println("���̃J�[�h��" + Name[Num] + "�ł����I");
		if ((choise == 0 && Num == Say) || (choise == 1 && Num != Say)) {/* ���������Ƃ� */
			System.out.println("player" + (From + 1) + "�̃~�X�ł��I");
			Open[From][Num]++;/* �J�[�h�̑����ɕ\�����Œǉ� */
			P = From;
		} else {/* �s�����̎� */
			System.out.println("player" + (P + 1) + "�̃~�X�ł��I");
			Open[P][Num]++;/* ���݂̃v���C���[�ɕ\�����Œǉ� */
		}
		System.out.println();
	}

	public void Receive() {
		// int choise;
		System.out.println("player" + (P + 1) + "����");
		System.out.println("�J�[�h�𑗂�܂���(0)�H����Ƃ��錾���ĕ\�ɂ��܂���(1)�H");
		System.out.println("���ʓ��̐�������͂��Ă��������B");

		// choise = Reader();/* �s���̑I�� */
		// if (choise == 1) {
		// OpenCard();
		// } else {
		// if (ACanSend()) {/* �J�[�h����悪�܂��c���Ă��鎞 */
		// System.out.println("���̃J�[�h�́u" + Name[Num] + "�v�ł��B");
		// SendB();/* �J�[�h�𑗂� */
		// } else {
		// System.out.println("�J�[�h�𑗂��v���C���[�����܂���B�錾���ĕ\�ɂ��܂��B");
		// OpenCard();
		// }
		// }

	}

	public void EndA() {
		System.out.println("��D������܂���I");
	}

	public void EndB() {
		System.out.println("player" + (P + 1) + "�̏��" + Name[Num] + "�̃J�[�h��4���ɂȂ�܂����I");
	}

	public void Result() {
		System.out.println("player" + (P + 1) + "�̕����ł��I");
		System.out.println("�Q�[�����I�����܂��B");
	}
}