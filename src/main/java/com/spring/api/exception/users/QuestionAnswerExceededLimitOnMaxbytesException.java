package com.spring.api.exception.users;

import com.spring.api.util.RegexUtil;

public class QuestionAnswerExceededLimitOnMaxbytesException extends Exception{
	public QuestionAnswerExceededLimitOnMaxbytesException(){
		super("비밀번호 찾기 질문의 답이 "+RegexUtil.QUESTION_ANSWER_MAXBYTES+"바이트를 초과함.");
	}
}