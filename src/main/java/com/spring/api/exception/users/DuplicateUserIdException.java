package com.spring.api.exception.users;

public class DuplicateUserIdException extends Exception{
	public DuplicateUserIdException(){
		super("����� ID �ߺ�");
	}
}