package com.spring.api.controller;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartRequest;

import com.spring.api.service.BookService;

@RestController("bookController")
public class BookController {
	@Autowired
	private BookService bookService;
	
	//1. �ű� ���� ��� ��û
	@RequestMapping(value= {"/books"}, method= {RequestMethod.POST})
	public ResponseEntity<HashMap> createNewBookInfo(MultipartRequest mRequest, HttpServletRequest request){
		HashMap result = new HashMap();
		bookService.createNewBookInfo(mRequest, request);
		result.put("flag", true);
		result.put("content", "�ű� ���� ��Ͽ� �����߽��ϴ�.");
		return new ResponseEntity<HashMap>(result,HttpStatus.CREATED);
	}
	
	//2. ���� ��� ��ȸ ��û
	@RequestMapping(value= {"/books"}, method= {RequestMethod.GET})
	public ResponseEntity<HashMap> readBooks(@RequestParam HashMap<String,String> param){
		HashMap result = bookService.readBooks(param);
		result.put("flag", true);
		result.put("content", "���� ��ȸ�� �����߽��ϴ�.");
		return new ResponseEntity<HashMap>(result,HttpStatus.OK);
	}
	
	//3. ���� �帣 ��ȸ ��û
	@RequestMapping(value= {"/books/book_types"}, method= {RequestMethod.GET})
	public ResponseEntity<HashMap> readBookTypes(){
		HashMap result = new HashMap();
		result.put("flag", true);
		result.put("book_types", bookService.readBookTypes());
		result.put("content", "���� �帣 ��ȸ�� �����߽��ϴ�.");
		return new ResponseEntity<HashMap>(result,HttpStatus.OK);
	}
	
	//4. �ű� ���� �帣 ��� ��û
	@RequestMapping(value= {"/books/book_types"}, method= {RequestMethod.POST})
	public ResponseEntity<HashMap> createNewBookTypes(HttpServletRequest request, @RequestBody HashMap param){
		HashMap result = new HashMap();
		result.put("flag", true);
		bookService.createNewBookTypes(request, param);
		result.put("content", "���� �帣 ��Ͽ� �����߽��ϴ�.");
		return new ResponseEntity<HashMap>(result,HttpStatus.CREATED);
	}
	
	//5. ���� �帣 ���� ��û
	@RequestMapping(value= {"/books/book_types/{book_type_id}"}, method= {RequestMethod.PUT})
	public ResponseEntity<HashMap> updateBookTypes(HttpServletRequest request, @RequestBody HashMap param, @PathVariable("book_type_id") String book_type_id){
		HashMap result = new HashMap();
		param.put("book_type_id", book_type_id);
		bookService.updateBookTypes(request, param);
		result.put("flag", true);
		result.put("content", "���� �帣 ������ �����߽��ϴ�.");
		return new ResponseEntity<HashMap>(result,HttpStatus.OK);
	}
	
	//6. ���� �帣 ���� ��û
	@RequestMapping(value= {"/books/book_types/{book_type_id}"}, method= {RequestMethod.DELETE})
	public ResponseEntity<HashMap> deleteBookTypes(HttpServletRequest request, @PathVariable("book_type_id") String book_type_id){
		HashMap result = new HashMap();
		HashMap param = new HashMap();
		param.put("book_type_id", book_type_id);
		bookService.deleteBookTypes(request, param);
		result.put("flag", true);
		result.put("content", "���� �帣 ������ �����߽��ϴ�.");
		return new ResponseEntity<HashMap>(result,HttpStatus.OK);
	}
}
