package com.spring.api.exception;

import com.spring.api.code.ErrorCode;

//����� ����, ��Ÿ�� �����̹Ƿ� try-catch �Ǵ� throws ���� ����ó�� �� �ʿ� ����
public class CustomException extends RuntimeException{
	private ErrorCode errorCode;

	public CustomException(ErrorCode errorCode){
		this.errorCode = errorCode;
	}

	public ErrorCode getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(ErrorCode errorCode) {
		this.errorCode = errorCode;
	}
}
