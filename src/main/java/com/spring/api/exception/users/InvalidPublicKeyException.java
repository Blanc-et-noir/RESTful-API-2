package com.spring.api.exception.users;

public class InvalidPublicKeyException extends Exception{
	public InvalidPublicKeyException(){
		super("유효하지 않은 공개키");
	}
}