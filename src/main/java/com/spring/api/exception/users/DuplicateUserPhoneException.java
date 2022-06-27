package com.spring.api.exception.users;

public class DuplicateUserPhoneException extends Exception{
	public DuplicateUserPhoneException(){
		super("해당 사용자 전화번호는 이미 사용중입니다.");
	}
}