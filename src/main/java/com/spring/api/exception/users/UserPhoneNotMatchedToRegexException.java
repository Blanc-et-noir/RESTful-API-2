package com.spring.api.exception.users;

public class UserPhoneNotMatchedToRegexException extends Exception{
	public UserPhoneNotMatchedToRegexException(){
		super("사용자 전화번호 형식이 올바르지 않음");
	}
}