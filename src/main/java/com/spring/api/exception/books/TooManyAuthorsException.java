package com.spring.api.exception.books;

public class TooManyAuthorsException extends Exception{
	public TooManyAuthorsException(){
		super("해당 도서에 등록할 저자의 수가 너무 많습니다.");
	}
}