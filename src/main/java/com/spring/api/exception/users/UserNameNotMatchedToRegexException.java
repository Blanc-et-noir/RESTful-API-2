package com.spring.api.exception.users;

public class UserNameNotMatchedToRegexException extends Exception{
	public UserNameNotMatchedToRegexException(){
		super("사용자 이름 형식이 올바르지 않음");
	}
}