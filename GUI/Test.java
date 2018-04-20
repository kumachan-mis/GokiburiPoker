package Test;

import processing.core.PApplet;
import processing.core.PFont;

public class Test extends PApplet {

	int err = 0;// �G���[�\���X�C�b�`�p�ϐ�
	static boolean start = false;
	int WIDE = 900, HIGHT = 800;// �E�B���h�E�T�C�Y
	static GameInfo info = new GameInfo();/* �Q�[�������L������t�B�[���h */
	public int[] color = { color(100, 0, 255), color(255, 150, 0), color(30, 45, 60), color(255, 0, 60),
			color(140, 60, 0), color(70, 240, 50), color(230, 255, 0), color(0, 255, 200) };
	// �Q���̐F
	// �R�E������0�A�n�G��1�A�l�Y�~��2�A�T�\����3�A�S�L�u����4�A�J�G����5�A�N����6�A�J�����V��7
	int sw = 0;// �s���X�C�b�`�p�ϐ�

	public void settings() {// �E�B���h�E�̏����ݒ�
		size(WIDE, HIGHT);
		noLoop();// draw()����񂸂\������
	}

	public void setup() {// ���̑������ݒ�
		colorMode(RGB, 256);
		background(0, 130, 45);
		smooth();
		PFont font = createFont("���C���I", 30);
		textFont(font);
	}

	public void draw() {// ���C��

		if (start) {// �Q�[�����J�n���Ă���Ȃ�
			info.Player();
			info.Field();
			background(0, 130, 45);// �w�i�̏㏑��
			Turn();// �^�[���\��
			MyHand();// �����̎�D�̕\��
			MyField();// �����̏�̕\��
			EHand();// �G�̎�D�̕\��
			EField();// �G�̏�̕\��
		} else {// �Q�[�����J�n���Ă��Ȃ��Ȃ�
			fill(0);
			textAlign(CENTER, CENTER);
			text("�v���C���[���W�܂�܂ł��҂���������...", WIDE / 2, HIGHT / 2);
		}

		InputF();// ������͗p�g�\��
		switch (sw) {// ���앪��
		case 0:
			WhichCard();// ��D���瑗��J�[�h��I��
			break;

		case 1:
			WhoSend();// �J�[�h�̑�����I��
			break;

		case 2:
			WhatSay();// �J�[�h�̐錾��I��
			break;

		case 3:
			Recieve();// �J�[�h�󂯎�莞�̍s����I��
			break;

		case 4:
			Open();// �J�[�h�𓖂Ă�Ƃ��̍s����I��
			break;

		case 5:
			Answer();// �J�[�h�𑗂�Ƃ��̕\�ʊm�F
			break;

		case 6:
			OK();// �J�[�h�𓖂Ă��Ƃ��̕\��
			break;

		case 7:
			NG();// �J�[�h���O�����Ƃ��̕\��
			break;

		case 8:// ��D�؂�ŏI���
			info.EndA();
			strokeWeight(1);
			fill(255, 0, 0);
			textAlign(CENTER);
			textSize(30);
			text("��D������܂���I", WIDE / 2, 250);
			textSize(45);
			text("player" + (info.P + 1) + "�̕����ł��I", WIDE / 2, 300);
			textSize(25);
			text("�Q�[�����I�����܂��B", WIDE / 2, 350);
			break;

		case 9:// 4��������ďI���
			info.EndB();
			strokeWeight(1);
			fill(255, 0, 0);
			textAlign(CENTER);
			textSize(30);
			text("player" + (info.P + 1) + "�̏��" + info.Name[info.Num] + "�̃J�[�h��4���ɂȂ�܂����I", WIDE / 2, 250);
			textSize(45);
			text("player" + (info.P + 1) + "�̕����ł��I", WIDE / 2, 300);
			textSize(25);
			text("�Q�[�����I�����܂��B", WIDE / 2, 350);
			break;
		}

		switch (err) {// �G���[�\������

		case 1:// ��D�؂�
			strokeWeight(1);
			fill(255, 0, 0);
			textAlign(CENTER);
			textSize(20);
			text("���̃J�[�h�͎�D�ɂ���܂���", WIDE / 2, 280);
			System.out.println("���̃J�[�h�͎����Ă��܂���");
			err = 0;
			break;

		case 2:// ����Ȃ�����
			strokeWeight(1);
			fill(255, 0, 0);
			textAlign(CENTER);
			textSize(20);
			text("���̃v���C���[�ɂ͑���܂���", WIDE / 2, 280);
			System.out.println("���̃v���C���[�ɂ͑���܂���");
			err = 0;
			break;

		case 3:// ����鑊�肪��l�����Ȃ�
			strokeWeight(1);
			fill(255, 0, 0);
			textAlign(CENTER);
			textSize(20);
			text("�����v���C���[�����܂���", WIDE / 2, 280);
			System.out.println("�����v���C���[�����܂���");
			err = 0;
			break;
		}

	}

	public void Turn() {// �^�[���\��
		fill(0);
		textAlign(CENTER);
		textSize(30);
		text("Turn" + info.turns + " player" + (info.P + 1), WIDE / 2, 450);
	}

	public void MyHand() {// �����̎�D�̕\��
		int j = 0, sum = 0;
		for (int i = 0; i < 8; i++) {
			fill(color[i]);
			strokeWeight(1);
			while (j < info.Hand[info.P][i]) {
				if (info.Hand[info.P][8] % 2 == 1) {// ��D����̎�
					rectMode(CENTER);
					rect((WIDE / 2) + (sum - info.Hand[info.P][8] / 2) * 35, 700, 70, 100);
				} else {// �����̎�
					rectMode(CORNER);
					rect((WIDE / 2) - 18 + (sum - info.Hand[info.P][8] / 2) * 35, 650, 70, 100);
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

	public void MyField() {// �����̏�̕\��
		int j = 0;
		for (int i = 0; i < 8; i++) {
			fill(color[i]);
			strokeWeight(1);
			while (j < info.Open[info.P][i]) {
				rectMode(CENTER);
				rect((WIDE / 9 * (i + 1)), 570 - (j * 30), 56, 80);
				j++;
			}
			j = 0;
		}
	}

	public void EHand() {// �G�̎�D�̐���\��
		int i = 1;
		for (int p = 0; p < info.player; p++) {
			if (p != info.P) {
				for (int j = 0; j < info.Hand[p][8]; j++) {
					fill(255, 255, 255);
					strokeWeight(1);
					if (info.Hand[p][8] % 2 == 1) {// ��D����̎�
						rectMode(CENTER);
						rect((WIDE * (2 * i - 1) / 6) + (j - info.Hand[p][8] / 2) * 10, 75, 35, 50);
					} else {// �����̎�
						rectMode(CORNER);
						rect((WIDE * (2 * i - 1) / 6) - 8 + (j - info.Hand[p][8] / 2) * 10, 50, 35, 50);
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

	public void EField() {// �G�̏�̕\��
		int k = 1;
		for (int p = 0; p < info.player; p++) {
			if (p != info.P) {
				int j = 0;
				for (int i = 0; i < 8; i++) {
					fill(color[i]);
					while (j < info.Open[p][i]) {
						rectMode(CORNER);
						strokeWeight(1);
						rect((WIDE * (2 * k - 1) / 6) + (i - 4) * 30, 150 - (j * 5), 21, 30);
						j++;
					}
					j = 0;
				}
				k++;
			}
		}
	}

	public void InputF() {// ����p�^���E�B���h�E�̕\���p�w�i
		rectMode(CORNER);
		strokeWeight(3);
		fill(0, 90, 30);
		rect(0, 200, 900, 220);
	}

	public void WhichCard() {// �����D�̑I��
		strokeWeight(1);
		fill(0);
		textAlign(CENTER);
		textSize(30);
		text("����J�[�h��I��ł�������", WIDE / 2, 250);
		info.ShowHand();
		for (int i = 0; i < 8; i++) {
			fill(color[i]);
			rectMode(CORNER);
			strokeWeight(1);
			rect((WIDE / 8 * i) + 25, 300, 70, 100);
			if (info.Hand[info.P][i] == 0) {// ������D�ɂȂ�������
				strokeWeight(3);
				fill(0);
				line((WIDE / 8 * i) + 25, 300, (WIDE / 8 * i) + 95, 400);// ���̃J�[�h�Ɂ~��\��
				line((WIDE / 8 * i) + 95, 300, (WIDE / 8 * i) + 25, 400);
			}
		}
	}

	public void WhoSend() {// �J�[�h�̑�����I��
		strokeWeight(1);
		fill(0);
		textAlign(CENTER);
		textSize(30);
		text("���鑊���I��ł�������", WIDE / 2, 250);
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
		text("�錾����J�[�h����I��ł�������", WIDE / 2, 250);
		info.ShowHand();
		for (int i = 0; i < 8; i++) {
			fill(color[i]);
			rectMode(CORNER);
			strokeWeight(1);
			rect((WIDE / 8 * i) + 25, 300, 70, 100);
		}
	}

	public void Recieve() {
		strokeWeight(1);
		fill(0);
		textAlign(CENTER);
		textSize(30);
		text("Player" + (info.From + 1) + "���񂩂�J�[�h�������܂���\n" + "�s����I��ł�������", WIDE / 2, 250);
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
		text("�J�[�h���߂���", (WIDE / 4), 370);
		text("���̐l�ɓn��", (WIDE * 3 / 4), 370);
	}

	public void Open() {
		strokeWeight(1);
		fill(0);
		textAlign(CENTER);
		textSize(30);
		text("Player" + (info.From + 1) + "�͂��̃J�[�h���u" + info.Name[info.Say] + "�v���ƌ����Ă��܂��B\n���̃J�[�h�́c", WIDE / 2, 250);
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
		text(info.Name[info.Say] + "�ł���", (WIDE / 4), 370);
		text(info.Name[info.Say] + "�ł͂Ȃ�", (WIDE * 3 / 4), 370);
	}

	public void Answer() {
		strokeWeight(1);
		fill(0);
		textAlign(CENTER);
		textSize(30);
		text("���̃J�[�h��" + info.Name[info.Num] + "�ł�", WIDE / 2, 250);
		fill(color[info.Num]);
		rectMode(CENTER);
		strokeWeight(1);
		rect(WIDE / 2, 310, 70, 100);

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
		text("���̃J�[�h��" + info.Name[info.Num] + "�ł���", WIDE / 2, 225);
		fill(255, 0, 0);
		text("�����ł��I", WIDE / 2, 250);

		fill(color[info.Num]);
		rectMode(CENTER);
		strokeWeight(1);
		rect(WIDE / 2, 310, 70, 100);

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
		text("���̃J�[�h��" + info.Name[info.Num] + "�ł���", WIDE / 2, 225);
		fill(255, 0, 0);
		text("�s�����ł��I", WIDE / 2, 250);

		fill(color[info.Num]);
		rectMode(CENTER);
		strokeWeight(1);
		rect(WIDE / 2, 310, 70, 100);

		fill(255);
		rect(WIDE / 2, 390, 200, 50);
		fill(0);
		text("OK", WIDE / 2, 400);

		info.OpenCard();
	}

	public void mousePressed() {
		switch (sw) {
		case 0:// ��D�̃N���b�N����
			System.out.println("a");
			for (int i = 0; i < 8; i++) {
				if (mouseX >= (WIDE / 8 * i) + 25 && mouseX <= (WIDE / 8 * i) + 95 && mouseY >= 300 && mouseY <= 400) {
					if (info.Hand[info.P][i] == 0) {// ��D�ɂȂ�
						err = 1;// �G���[�ϐ����X�V���čēx�h���[
						redraw();
					} else {// ��D�ɂ���Ƃ�
						info.Num = i;
						info.SendA();
						sw = 1;
						redraw();
					}
				}
			}
			break;

		case 1:// ���葊��̃N���b�N����
			int j = 1;
			for (int i = 0; i < info.player; i++) {
				if (i != info.P) {
					if (mouseX >= (WIDE * (2 * j - 1) / 6) - 100 && mouseX <= (WIDE * (2 * j - 1) / 6) + 100
							&& mouseY >= 300 && mouseY <= 400) {
						if (info.Send[i] == 1) {// ����Ȃ�
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

		case 2:// �錾�̃N���b�N����
			for (int i = 0; i < 8; i++) {
				if (mouseX >= (WIDE / 8 * i) + 25 && mouseX <= (WIDE / 8 * i) + 95 && mouseY >= 300 && mouseY <= 400) {
					info.Say = i;
					info.SendC();
					sw = 3;
					redraw();
				}
			}
			break;

		case 3:// �󂯎�莞�̃N���b�N����
			if (mouseX >= (WIDE / 4) - 150 && mouseX <= (WIDE / 4) + 150 && mouseY >= 325 && mouseY <= 395) {// �\�ɂ���
				sw = 4;
				redraw();
			}
			if (mouseX >= (WIDE * 3 / 4) - 150 && mouseX <= (WIDE * 3 / 4) + 150 && mouseY >= 325 && mouseY <= 395) {// ���ɑ���
				if (info.ACanSend()) {
					sw = 5;
					redraw();
				} else {// �����v���C���[�����Ȃ�
					err = 3;
					redraw();
				}
			}
			break;

		case 4:// �\�ɂ���Ƃ��̃N���b�N����
			if (mouseX >= (WIDE / 4) - 150 && mouseX <= (WIDE / 4) + 150 && mouseY >= 325 && mouseY <= 395) {// �`�ł���
				info.choise = 0;
				if (info.Num == info.Say) {
					sw = 6;
					redraw();
				} else {
					sw = 7;
					redraw();
				}

			}
			if (mouseX >= (WIDE * 3 / 4) - 150 && mouseX <= (WIDE * 3 / 4) + 150 && mouseY >= 325 && mouseY <= 395) {// �`�łȂ�
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

		case 5:// �\���m�F�����Ƃ��̃N���b�N����
			if (mouseX >= WIDE / 2 - 100 && mouseX <= WIDE / 2 + 100 && mouseY >= 365 && mouseY <= 415) {
				sw = 1;// �����I���Ɉڂ�
				redraw();
			}
			break;

		case 6:// ���������Ƃ��̃N���b�N����
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
						sw = 0;// �����I���Ɉڂ�
						redraw();
					}
				}
			}
			break;

		case 7:// �s�����̂Ƃ��̃N���b�N����
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
						sw = 0;// �����I���Ɉڂ�
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
