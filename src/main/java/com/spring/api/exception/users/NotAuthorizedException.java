package com.spring.api.exception.users;

public class NotAuthorizedException extends Exception{
	public NotAuthorizedException(){
		super("해당 작업을 요청할 권한이 없습니다.");
	}
}