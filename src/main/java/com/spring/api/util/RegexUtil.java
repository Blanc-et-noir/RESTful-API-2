package com.spring.api.util;

//���ԽĿ� �����ϴ��� �Ǵ��Ҷ� ����� ��ƿ Ŭ����
public class RegexUtil {
	//����� ���̵�� 8~16���� ���� �Ǵ� ���ڷ� �����ؾ���
	public final static String USER_ID_REGEX = "^[a-zA-Z]{1}[a-zA-Z0-9_]{7,15}$";
	//����� ��й�ȣ�� ���� �Ǵ� ���ڷ� 8~16�ڷ� �����ؾ���
	public final static String USER_PW_REGEX = "[a-zA-Z0-9_]{8,16}$";
	//����� ��ȭ��ȣ ������ 000-0000-0000�̾����
	public final static String USER_PHONE_REGEX = "\\d{3}-\\d{4}-\\d{4}";
	//UUID������ 8-4-4-4-12 �ڸ��� ���� �Ǵ� ���ڷ� �����Ǿ����
	public final static String UUID_REGEX = "[0-9a-fA-F]{8}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{12}";
	//����� �̸��� 2~5�ڸ��� �ѱ��̾����
	public final static String USER_NAME_REGEX = "^[��-�R]{2,5}$";
	//�������� �̸��� ���� �Ǵ� �ѱ۷� 1~20�ڿ�����
	public final static String BOOK_TRANSLATOR_NAME_REGEX = "^[a-zA-Z��-�R]{1,20}";
	//�۰� �̸��� ���� �Ǵ� �ѱ۷� 1~20�ڿ�����
	public final static String BOOK_AUTHOR_NAME_REGEX = "^[a-zA-Z��-�R]{1,20}";
	//���� ISBN�ڵ�� -���� ���ڷθ� 13�ڸ�������
	public final static String BOOK_ISBN_REGEX = "^[0-9]{13,13}";
	//���ǻ� �̸��� 1~20�ڸ��� ���� �Ǵ� �ѱ��̾����
	public final static String BOOK_PUBLISHER_NAME_REGEX = "^[a-zA-Z��-�R]{1,20}";
	//��¥������ YYYY-MM-DD���¿�����
	public final static String DATE_REGEX = "^([12]\\d{3}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01]))$";
	
	//��й�ȣ ã�� ������ ���� 512����Ʈ���Ͽ�����
	public final static int QUESTION_ANSWER_MAXBYTES = 512;
	//���� �̸��� 120����Ʈ ���Ͽ�����
	public final static int BOOK_NAME_MAXBYTES = 120;
	//���� �帣���� 60����Ʈ ���Ͽ�����
	public final static int BOOK_TYPE_CONTENT_MAXBYTES = 60;
	//�޼��� ������ 900����Ʈ ���Ͽ�����
	public final static int MESSAGE_CONTENT_MAXBYTES = 900;
	//�޼��� ������ 300����Ʈ ���Ͽ�����
	public final static int MESSAGE_TITLE_MAXBYTES = 300;
	
	//�ش� ���ڿ��� Ư�� ��ġ ������ ����Ʈ ũ�⸦ ������ �Ǵ��ϴ� �޼ҵ�
	//null�̰ų� Ư�� ����Ʈ �̻��̶�� false �ƴ϶�� true��
	public static boolean checkBytes(String str, final int maxLength) {
		if(str==null) {
			return false;
		}
		if(str.getBytes().length<=maxLength) {
			return true;
		}else {
			return false;
		}
	}
	
	//�ش� ���ڿ��� ���ԽĿ� �����ϴ��� �Ǵ��ϴ� �޼ҵ�
	public static boolean checkRegex(String str, String regex) {
		if(str == null) {
			return false;
		}else if(str.matches(regex)) {
			return true;
		}else {
			return false;
		}
	}
}