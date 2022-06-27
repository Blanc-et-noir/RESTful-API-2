package com.spring.api.exception.users;

public class DuplicateUserIdException extends Exception{
	public DuplicateUserIdException(){
		super("해당 사용자 ID는 이미 사용중입니다.");
	}
}