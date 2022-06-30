package com.spring.api.exception.books;

public class TranslatorNameNotMatchedToRegexException extends Exception{
	public TranslatorNameNotMatchedToRegexException(){
		super("해당 도서 번역자의 이름 형식이 올바르지 않습니다.");
	}
}