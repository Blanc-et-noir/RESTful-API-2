package com.spring.api.exception.users;

public class UserPwNotMatchedException extends Exception{
	public UserPwNotMatchedException(){
		super("사용자 PW가 일치하지 않음");
	}
}