package com.spring.api.exception.users;

public class DuplicateUserIdException extends Exception{
	public DuplicateUserIdException(){
		super("사용자 ID 중복");
	}
}