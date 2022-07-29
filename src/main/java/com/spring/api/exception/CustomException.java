package com.spring.api.exception;

import com.spring.api.code.ErrorCode;

//사용자 예외, 런타임 예외이므로 try-catch 또는 throws 같은 예외처리 할 필요 없음
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
