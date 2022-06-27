package com.spring.api.exception.users;

public class NotFoundUserException extends Exception{
	public NotFoundUserException(){
		super("해당 사용자 ID로 가입한 회원정보가 존재하지 않습니다.");
	}
}