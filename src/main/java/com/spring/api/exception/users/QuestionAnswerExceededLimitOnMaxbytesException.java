package com.spring.api.exception.users;

import com.spring.api.util.RegexUtil;

public class QuestionAnswerExceededLimitOnMaxbytesException extends Exception{
	public QuestionAnswerExceededLimitOnMaxbytesException(){
		super("��й�ȣ ã�� ������ ���� "+RegexUtil.QUESTION_ANSWER_MAXBYTES+"����Ʈ�� �ʰ���.");
	}
}