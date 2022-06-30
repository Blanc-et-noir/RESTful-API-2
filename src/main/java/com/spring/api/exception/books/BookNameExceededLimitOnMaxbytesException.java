package com.spring.api.exception.books;

import com.spring.api.util.RegexUtil;

public class BookNameExceededLimitOnMaxbytesException extends Exception{
	public BookNameExceededLimitOnMaxbytesException(){
		super("도서 제목이 "+RegexUtil.QUESTION_ANSWER_MAXBYTES+"바이트를 초과했습니다.");
	}
}