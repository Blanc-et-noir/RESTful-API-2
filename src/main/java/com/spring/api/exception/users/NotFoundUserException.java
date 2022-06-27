package com.spring.api.exception.users;

public class NotFoundUserException extends Exception{
	public NotFoundUserException(){
		super("해당 ID로 가입한 사용자 없음");
	}
}