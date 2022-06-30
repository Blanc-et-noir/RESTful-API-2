package com.spring.api.exception.books;

public class TooFewAuthorsException extends Exception{
	public TooFewAuthorsException(){
		super("해당 도서에 등록할 저자의 수가 너무 적습니다.");
	}
}