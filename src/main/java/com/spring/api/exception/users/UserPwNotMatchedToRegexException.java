package com.spring.api.exception.users;

public class UserPwNotMatchedToRegexException extends Exception{
	public UserPwNotMatchedToRegexException(){
		super("����� PW ������ �ùٸ��� ����.");
	}
}