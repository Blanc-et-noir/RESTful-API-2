package com.spring.api.exception.users;

public class UserIdNotMatchedToRegexException extends Exception{
	public UserIdNotMatchedToRegexException(){
		super("해당 사용자 ID 형식이 올바르지 않습니다.");
	}
}