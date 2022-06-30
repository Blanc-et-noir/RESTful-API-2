package com.spring.api.exception.books;

public class DuplicateBookIsbnException extends Exception{
	public DuplicateBookIsbnException(){
		super("해당 ISBN의 정보를 갖는 도서가 이미 존재합니다.");
	}
}