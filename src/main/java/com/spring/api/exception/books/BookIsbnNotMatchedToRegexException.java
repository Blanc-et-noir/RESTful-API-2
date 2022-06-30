package com.spring.api.exception.books;

public class BookIsbnNotMatchedToRegexException extends Exception{
	public BookIsbnNotMatchedToRegexException(){
		super("해당 도서의 ISBN 형식이 올바르지 않습니다.");
	}
}