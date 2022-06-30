package com.spring.api.exception.books;

public class PublisherNameNotMatchedToRegexException extends Exception{
	public PublisherNameNotMatchedToRegexException(){
		super("해당 도서 출판사의 이름 형식이 올바르지 않습니다.");
	}
}