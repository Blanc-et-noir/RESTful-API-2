package com.spring.api.exception.books;

import com.spring.api.util.RegexUtil;

public class BookNameExceededLimitOnMaxbytesException extends Exception{
	public BookNameExceededLimitOnMaxbytesException(){
		super("���� ������ "+RegexUtil.QUESTION_ANSWER_MAXBYTES+"����Ʈ�� �ʰ��߽��ϴ�.");
	}
}