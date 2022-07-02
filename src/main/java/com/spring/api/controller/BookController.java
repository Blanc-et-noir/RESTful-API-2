package com.spring.api.controller;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartRequest;

import com.spring.api.service.BookService;

@RestController("bookController")
public class BookController {
	@Autowired
	private BookService bookService;
	
	@RequestMapping(value= {"/books"}, method= {RequestMethod.POST})
	public ResponseEntity<HashMap> createNewBookInfo(MultipartRequest mRequest, HttpServletRequest request){
		HashMap result = new HashMap();
		bookService.createNewBookInfo(mRequest, request);
		result.put("flag", true);
		result.put("content", "신규 도서 등록에 성공했습니다.");
		return new ResponseEntity<HashMap>(result,HttpStatus.CREATED);
	}
}
