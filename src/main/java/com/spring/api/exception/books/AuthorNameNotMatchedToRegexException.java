package com.spring.api.exception.books;

public class AuthorNameNotMatchedToRegexException extends Exception{
	public AuthorNameNotMatchedToRegexException(){
		super("해당 도서 저자의 이름 형식이 올바르지 않습니다.");
	}
}