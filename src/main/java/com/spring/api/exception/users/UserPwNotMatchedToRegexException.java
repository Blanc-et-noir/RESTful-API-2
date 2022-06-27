package com.spring.api.exception.users;

public class UserPwNotMatchedToRegexException extends Exception{
	public UserPwNotMatchedToRegexException(){
		super("사용자 PW 형식이 올바르지 않음.");
	}
}