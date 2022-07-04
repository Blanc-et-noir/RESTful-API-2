package com.spring.api.code;

import com.spring.api.util.RegexUtil;

public enum ErrorCode {
	//����Ű ��ȿ��������
	INVALID_PUBLICKEY(400,"�ش� ����Ű�� ��ȿ���� �ʽ��ϴ�."),
	
	//���� ����ġ
	USER_PW_NOT_MATCHED(400,"�ش� ����� PW�� ��ġ���� �ʽ��ϴ�."),
	QUESTION_ANSWER_NOT_MATCHED(400,"�ش� ����� ��й�ȣ ã�� ������ ���� ��ġ���� �ʽ��ϴ�."),
	
	//�Է°��� ���ԽĿ� ���� ����
	AUTHOR_NAME_NOT_MATCHED_TO_REGEX(400,"�ش� ���� ������ �̸� ������ �ùٸ��� �ʽ��ϴ�."),
	BOOK_ISBN_NOT_MATCHED_TO_REGEX(400,"�ش� ������ ISBN ������ �ùٸ��� �ʽ��ϴ�."),
	BOOK_QUANTITY_NOT_MATCHED_TO_REGEX(400,"�ش� ���� ����� ������ �ùٸ��� �ʽ��ϴ�."),
	PUBLISHER_NAME_NOT_MATCHED_TO_REGEX(400,"�ش� ���� ���ǻ��� �̸� ������ �ùٸ��� �ʽ��ϴ�."),
	TRANSLATOR_NAME_NOT_MATCHED_TO_REGEX(400,"�ش� ���� �������� �̸� ������ �ùٸ��� �ʽ��ϴ�."),
	USER_ID_NOT_MATCHED_TO_REGEX(400,"�ش� ����� ID�� ������ �ùٸ��� �ʽ��ϴ�."),
	USER_NAME_NOT_MATCHED_TO_REGEX(400,"�ش� ����� �̸��� ������ �ùٸ��� �ʽ��ϴ�."),
	USER_PHONE_NOT_MATCHED_TO_REGEX(400,"�ش� ����� ��ȭ��ȣ�� ������ �ùٸ��� �ʽ��ϴ�."),
	USER_PW_NOT_MATCHED_TO_REGEX(400,"�ش� ����� PW ������ �ùٸ��� �ʽ��ϴ�."),
	UUID_NOT_MATCHED_TO_REGEX(400,"�ش� UUID ������ �ùٸ��� �ʽ��ϴ�."),
	QUESTION_ANSWER_REQUIRED(400,"��й�ȣ ã�� ������ ���� ���� �����ؾ��մϴ�."),
	
	//�ߺ�
	DUPLICATE_USER_ID(400,"�ش� ����� ID�� �̹� ������Դϴ�."),
	DUPLICATE_USER_PHONE(400,"�ش� ����� ��ȭ��ȣ�� �̹� ������Դϴ�."),
	DUPLICATE_BOOK_ISBN(400,"�ش� ISBN�� ������ ���� ������ �̹� �����մϴ�."),
	DUPLICATE_BOOK_ISBN_OF_RESERVATION(400,"�ش� ������ �̹� �����߽��ϴ�."),
	
	//���� ��
	NOT_FOUND_BOOK_TYPE(400,"�ش� ���� �帣�� �������� �ʽ��ϴ�."),
	NOT_FOUND_BOOK(400,"�ش� ISBN �ڵ�� ��ϵ� ���� ������ �������� �ʽ��ϴ�."),
	NOT_FOUND_USER(400,"�ش� ����� ID�� ������ ȸ�������� �������� �ʽ��ϴ�."),
	NOT_FOUND_CHECKOUT(400,"�ش� ���� ���� ������ �������� �ʽ��ϴ�."),
	
	//����Ʈ�ʰ�
	BOOK_NAME_EXCEEDED_LIMIT_ON_MAXBYTES(400,"���� ������ "+RegexUtil.QUESTION_ANSWER_MAXBYTES+"����Ʈ�� �ʰ��߽��ϴ�."),
	QUESTION_ANSWER_EXCEEDED_LIMIT_ON_MAXBYTES(400,"��й�ȣ ã�� ������ ���� "+RegexUtil.QUESTION_ANSWER_MAXBYTES+"����Ʈ�� �ʰ��߽��ϴ�."),
	BOOK_CONTENT_TYPE_EXCEEDED_LIMIT_IN_MAXBYTES(400,"���� �帣�� "+RegexUtil.BOOK_TYPE_CONTENT_MAXBYTES+"����Ʈ�� �ʰ��߽��ϴ�."),
	//�ʹ� ���ų� ����
	TOO_FEW_AUTHORS(400,"�ش� ������ ����� ������ ���� �ʹ� �����ϴ�."),
	TOO_MANY_AUTHORS(400,"�ش� ������ ����� ������ ���� �ʹ� �����ϴ�."),
	TOO_FEW_BOOK_QUANTITY(400,"�ش� ������ ��� �ʹ� �����ϴ�."),
	
	//���Ѿ���
	NOT_AUTHORIZED(403,"�ش� �۾��� ��û�� ������ �����ϴ�."),
	
	//��Ʈ�� ����
	NOT_CHANGEABLE_USER_SALT(400,"����� PW, ��й�ȣ ã�� ����, ��й�ȣ ã�� ������ ���� ���ÿ� ����Ǿ�� �մϴ�."),
	
	//���� �Ұ�
	NOT_ABLE_TO_CHECK_OUT_DUE_TO_DATE(400,"���� ���� ���� �ð��� ������ �ʾҽ��ϴ�."),
	NOT_ABLE_TO_CHECK_OUT_DUE_TO_LIMIT(400,"���� ���� �ѵ��� �ʰ��Ͽ����ϴ�."),
	NOT_ABLE_TO_CHECK_OUT_DUE_TO_OVERDUE(400,"������ ��ü�� ���� ������ �����Ͽ� ������ ������ �� �����ϴ�."),
	NOT_ABLE_TO_CHECK_OUT_DUE_TO_TOO_MANY_RESERVATIONS(400,"�ش� ������ ���� ���������� ���� ������ �� �����ϴ�."),
	NOT_ABLE_TO_CHECK_OUT_DUE_TO_PRIORITY_OF_RESERVATIONS(400,"�ش� ������ ���� ��������� ���� ������ �� �����ϴ�."),
	
	//���� �Ұ�
	NOT_ABLE_TO_RESERVE_DUE_TO_OVERDUE(400,"������ ��ü�� ���� ������ �����Ͽ� ������ ������ �� �����ϴ�."),
	NOT_ABLE_TO_RESERVE_DUE_TO_DATE(400,"���� ���� ���� �ð��� ������ �ʾҽ��ϴ�."),
	NOT_ABLE_TO_RESERVE_DUE_TO_FULL(400,"���� ���� ���� �ο��� �ʰ��Ǿ����ϴ�."),
	NOT_ABLE_TO_RESERVE_DUE_TO_MANY(400,"���� ���� ���� Ƚ���� �ʰ��Ǿ����ϴ�."),
	
	//���ο���
	INTERNAL_SERVER_ERROR(500,"���� ���ο��� ������ �߻��߽��ϴ�.");
	
	private int ERROR_CODE;
	private String ERROR_MESSAGE;
	
	public int getERROR_CODE() {
		return ERROR_CODE;
	}

	public void setERROR_CODE(int eRROR_CODE) {
		ERROR_CODE = eRROR_CODE;
	}

	public String getERROR_MESSAGE() {
		return ERROR_MESSAGE;
	}

	public void setERROR_MESSAGE(String eRROR_MESSAGE) {
		ERROR_MESSAGE = eRROR_MESSAGE;
	}

	ErrorCode(int ERROR_CODE, String ERROR_MESSAGE){
		this.ERROR_CODE = ERROR_CODE;
		this.ERROR_MESSAGE = ERROR_MESSAGE;
	}
}
