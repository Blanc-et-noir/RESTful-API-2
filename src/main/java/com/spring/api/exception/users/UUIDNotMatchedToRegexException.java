package com.spring.api.exception.users;

public class UUIDNotMatchedToRegexException extends Exception{
	public UUIDNotMatchedToRegexException(){
		super("해당 UUID 형식이 올바르지 않습니다.");
	}
}