package com.spring.api.exception.users;

public class UserQuestionAnswerNotMatchedException extends Exception{
	public UserQuestionAnswerNotMatchedException(){
		super("해당 사용자 비밀번호 찾기 질문의 답이 일치하지 않습니다.");
	}
}