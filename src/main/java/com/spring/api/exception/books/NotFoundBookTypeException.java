package com.spring.api.exception.books;

public class NotFoundBookTypeException extends Exception{
	public NotFoundBookTypeException(){
		super("해당 도서 장르가 존재하지 않습니다.");
	}
}