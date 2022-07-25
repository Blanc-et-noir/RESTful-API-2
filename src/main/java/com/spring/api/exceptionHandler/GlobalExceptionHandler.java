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

@RestControllerAdvice
public class GlobalExceptionHandler {
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
	
	@ExceptionHandler({ Exception.class })
	private ResponseEntity<HashMap> handleServerException(Exception e){
		e.printStackTrace();
		HashMap result = new HashMap();
		result.put("flag", false);
		result.put("content", "서버 내부에서 오류가 발생했습니다.");
		
		StringWriter errors = new StringWriter();
        e.printStackTrace(new PrintWriter(errors));
         
		result.put("errors", errors.toString());
		return new ResponseEntity<HashMap>(result,HttpStatus.INTERNAL_SERVER_ERROR);
	}
}