package GokiPoker;

public class Main {
	public static void main(String[] argv) {
		GameInfo info = new GameInfo();/* �Q�[�������L������t�B�[���h */

		while (true) {/* �Q�[���̌��������܂Ŗ������[�v */
			info.Player();/* �^�[����� */
			info.Field();/* �Տ�̉��� */
			if (info.CheckHand()) {/* ��D���Ȃ��ꍇ */
				info.EndA();/* �G���[�\�� */
				break;/* �Q�[���̏I�� */
			} else {
				info.SendA();/* �s���̓ǂݍ��� */
			}
			if (info.CheckField()) {/* �������J�[�h��4���ɂȂ����� */
				info.EndB();
				break;
			}
			info.NextTurn();/* ���X�V */
		}
		info.Result();
	}
}
