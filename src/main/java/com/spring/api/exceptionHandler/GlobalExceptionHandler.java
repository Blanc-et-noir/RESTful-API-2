package com.spring.api.exceptionHandler;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.spring.api.code.ErrorCode;
import com.spring.api.exception.CustomException;

//����ó���� �������� ������ ����ó�� �����̽� Ŭ����
@RestControllerAdvice
public class GlobalExceptionHandler {
	
	//����� ���� ���ܴ� �ش� �������� ó����
	@ExceptionHandler({ CustomException.class })
	private ResponseEntity<HashMap> handleCustomException(CustomException e){
		ErrorCode errorCode = e.getErrorCode();
		e.printStackTrace();
		HashMap result = new HashMap();
		result.put("flag", false);
		result.put("content", errorCode.getERROR_MESSAGE());
		
		StringWriter errors = new StringWriter();
        e.printStackTrace(new PrintWriter(errors));
         
		result.put("errors", errors.toString());
		
		return new ResponseEntity<HashMap>(result,HttpStatus.valueOf(errorCode.getERROR_CODE()));
	}
	
	//����� ���ǰ� �ƴ� ���ܴ� �ش� �������� ó����
	@ExceptionHandler({ Exception.class })
	private ResponseEntity<HashMap> handleServerException(Exception e){
		e.printStackTrace();
		HashMap result = new HashMap();
		result.put("flag", false);
		result.put("content", "���� ���ο��� ������ �߻��߽��ϴ�.");
		
		StringWriter errors = new StringWriter();
        e.printStackTrace(new PrintWriter(errors));
         
		result.put("errors", errors.toString());
		return new ResponseEntity<HashMap>(result,HttpStatus.INTERNAL_SERVER_ERROR);
	}
}