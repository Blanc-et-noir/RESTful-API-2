package com.spring.api.exception.books;

public class TooFewBookQuantityException extends Exception{
	public TooFewBookQuantityException(){
		super("�ش� ������ ��� �ʹ� �����ϴ�.");
	}
}