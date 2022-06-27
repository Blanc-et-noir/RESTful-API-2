package com.spring.api.exception.users;

public class DuplicateUserPhoneException extends Exception{
	public DuplicateUserPhoneException(){
		super("사용자 전화번호 중복");
	}
}