package com.spring.api.exception.users;

public class InvalidPublicKeyException extends Exception{
	public InvalidPublicKeyException(){
		super("해당 공개키는 유효하지 않습니다.");
	}
}