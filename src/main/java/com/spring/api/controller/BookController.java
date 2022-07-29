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
	
	//1. 신규 도서 등록 요청
	@RequestMapping(value= {"/books"}, method= {RequestMethod.POST})
	public ResponseEntity<HashMap> createNewBookInfo(MultipartRequest mRequest, HttpServletRequest request){
		HashMap result = new HashMap();
		bookService.createNewBookInfo(mRequest, request);
		result.put("flag", true);
		result.put("content", "신규 도서 등록에 성공했습니다.");
		return new ResponseEntity<HashMap>(result,HttpStatus.CREATED);
	}
	
	//2. 도서 목록 조회 요청
	@RequestMapping(value= {"/books"}, method= {RequestMethod.GET})
	public ResponseEntity<HashMap> readBooks(@RequestParam HashMap<String,String> param){
		HashMap result = bookService.readBooks(param);
		result.put("flag", true);
		result.put("content", "도서 조회에 성공했습니다.");
		return new ResponseEntity<HashMap>(result,HttpStatus.OK);
	}
	
	//3. 도서 장르 조회 요청
	@RequestMapping(value= {"/books/book_types"}, method= {RequestMethod.GET})
	public ResponseEntity<HashMap> readBookTypes(){
		HashMap result = new HashMap();
		result.put("flag", true);
		result.put("book_types", bookService.readBookTypes());
		result.put("content", "도서 장르 조회에 성공했습니다.");
		return new ResponseEntity<HashMap>(result,HttpStatus.OK);
	}
	
	//4. 신규 도서 장르 등록 요청
	@RequestMapping(value= {"/books/book_types"}, method= {RequestMethod.POST})
	public ResponseEntity<HashMap> createNewBookTypes(HttpServletRequest request, @RequestBody HashMap param){
		HashMap result = new HashMap();
		result.put("flag", true);
		bookService.createNewBookTypes(request, param);
		result.put("content", "도서 장르 등록에 성공했습니다.");
		return new ResponseEntity<HashMap>(result,HttpStatus.CREATED);
	}
	
	//5. 도서 장르 수정 요청
	@RequestMapping(value= {"/books/book_types/{book_type_id}"}, method= {RequestMethod.PUT})
	public ResponseEntity<HashMap> updateBookTypes(HttpServletRequest request, @RequestBody HashMap param, @PathVariable("book_type_id") String book_type_id){
		HashMap result = new HashMap();
		param.put("book_type_id", book_type_id);
		bookService.updateBookTypes(request, param);
		result.put("flag", true);
		result.put("content", "도서 장르 수정에 성공했습니다.");
		return new ResponseEntity<HashMap>(result,HttpStatus.OK);
	}
	
	//6. 도서 장르 삭제 요청
	@RequestMapping(value= {"/books/book_types/{book_type_id}"}, method= {RequestMethod.DELETE})
	public ResponseEntity<HashMap> deleteBookTypes(HttpServletRequest request, @PathVariable("book_type_id") String book_type_id){
		HashMap result = new HashMap();
		HashMap param = new HashMap();
		param.put("book_type_id", book_type_id);
		bookService.deleteBookTypes(request, param);
		result.put("flag", true);
		result.put("content", "도서 장르 삭제에 성공했습니다.");
		return new ResponseEntity<HashMap>(result,HttpStatus.OK);
	}
}
