package com.spring.api.exception.users;

public class UserNameNotMatchedToRegexException extends Exception{
	public UserNameNotMatchedToRegexException(){
		super("해당 사용자 이름의 형식이 올바르지 않습니다.");
	}
}