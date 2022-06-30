package com.spring.api.exception.books;

public class TooFewBookQuantityException extends Exception{
	public TooFewBookQuantityException(){
		super("해당 도서의 재고가 너무 적습니다.");
	}
}