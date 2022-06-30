package com.spring.api.exception.books;

public class BookQuantityNotMatchedToRegexException extends Exception{
	public BookQuantityNotMatchedToRegexException(){
		super("해당 도서 재고의 형식이 올바르지 않습니다.");
	}
}