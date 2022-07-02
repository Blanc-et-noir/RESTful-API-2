package com.spring.api.exceptionHandler;

import java.util.HashMap;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.spring.api.errorCode.ErrorCode;
import com.spring.api.exception.CustomException;

@RestControllerAdvice
public class GlobalExceptionHandler {
	@ExceptionHandler({ CustomException.class })
	private ResponseEntity<HashMap> handleCustomException(CustomException e){
		ErrorCode errorCode = e.getErrorCode();
		
		HashMap result = new HashMap();
		result.put("flag", false);
		result.put("content", errorCode.getERROR_MESSAGE());
		
		return new ResponseEntity<HashMap>(result,HttpStatus.valueOf(errorCode.getERROR_CODE()));
	}
	
	@ExceptionHandler({ Exception.class })
	private ResponseEntity<HashMap> handleServerException(Exception e){
		HashMap result = new HashMap();
		result.put("flag", false);
		result.put("content", "서버 내부에서 오류가 발생했습니다.");
		
		return new ResponseEntity<HashMap>(result,HttpStatus.INTERNAL_SERVER_ERROR);
	}
}