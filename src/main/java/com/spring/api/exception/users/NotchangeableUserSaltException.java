package com.spring.api.exception.users;

public class NotchangeableUserSaltException extends Exception{
	public NotchangeableUserSaltException(){
		super("사용자 PW, 비밀번호 찾기 질문, 비밀번호 찾기 질문의 답은 동시에 변경되어야 합니다.");
	}
}