package com.spring.api.exception.books;

public class TooFewTranslatorsException extends Exception{
	public TooFewTranslatorsException(){
		super("해당 도서에 등록할 저자의 수가 너무 적습니다.");
	}
}