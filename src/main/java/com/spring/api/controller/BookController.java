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

import com.spring.api.exception.books.AuthorNameNotMatchedToRegexException;
import com.spring.api.exception.books.BookIsbnNotMatchedToRegexException;
import com.spring.api.exception.books.BookNameExceededLimitOnMaxbytesException;
import com.spring.api.exception.books.BookQuantityNotMatchedToRegexException;
import com.spring.api.exception.books.DuplicateBookIsbnException;
import com.spring.api.exception.books.NotFoundBookTypeException;
import com.spring.api.exception.books.PublisherNameNotMatchedToRegexException;
import com.spring.api.exception.books.TooFewAuthorsException;
import com.spring.api.exception.books.TranslatorNameNotMatchedToRegexException;
import com.spring.api.exception.users.NotAuthorizedException;
import com.spring.api.exception.users.NotFoundUserException;
import com.spring.api.exception.users.UUIDNotMatchedToRegexException;
import com.spring.api.service.BookService;

@RestController("bookController")
public class BookController {
	@Autowired
	private BookService bookService;
	
	@RequestMapping(value= {"/books"}, method= {RequestMethod.POST})
	public ResponseEntity<HashMap> createNewBookInfo(MultipartRequest mRequest, HttpServletRequest request){
		HashMap result = new HashMap();
		try {
			bookService.createNewBookInfo(mRequest, request);
			result.put("flag", true);
			result.put("content", "신규 도서 등록에 성공했습니다.");
			return new ResponseEntity<HashMap>(result,HttpStatus.CREATED);
		}catch(TooFewAuthorsException e) {
			result.put("flag", false);
			result.put("content", e.getMessage());
			return new ResponseEntity<HashMap>(result,HttpStatus.BAD_REQUEST);
		}catch(DuplicateBookIsbnException e) {
			result.put("flag", false);
			result.put("content", e.getMessage());
			return new ResponseEntity<HashMap>(result,HttpStatus.BAD_REQUEST);
		}catch(AuthorNameNotMatchedToRegexException e) {
			result.put("flag", false);
			result.put("content", e.getMessage());
			return new ResponseEntity<HashMap>(result,HttpStatus.BAD_REQUEST);
		}catch(BookQuantityNotMatchedToRegexException e) {
			result.put("flag", false);
			result.put("content", e.getMessage());
			return new ResponseEntity<HashMap>(result,HttpStatus.BAD_REQUEST);
		}catch(NotAuthorizedException e) {
			result.put("flag", false);
			result.put("content", e.getMessage());
			return new ResponseEntity<HashMap>(result,HttpStatus.FORBIDDEN);
		}catch(NotFoundUserException e) {
			result.put("flag", false);
			result.put("content", e.getMessage());
			return new ResponseEntity<HashMap>(result,HttpStatus.BAD_REQUEST);
		}catch(BookIsbnNotMatchedToRegexException e) {
			result.put("flag", false);
			result.put("content", e.getMessage());
			return new ResponseEntity<HashMap>(result,HttpStatus.BAD_REQUEST);
		}catch(BookNameExceededLimitOnMaxbytesException e) {
			result.put("flag", false);
			result.put("content", e.getMessage());
			return new ResponseEntity<HashMap>(result,HttpStatus.BAD_REQUEST);
		}catch(PublisherNameNotMatchedToRegexException e) {
			result.put("flag", false);
			result.put("content", e.getMessage());
			return new ResponseEntity<HashMap>(result,HttpStatus.BAD_REQUEST);
		}catch(TranslatorNameNotMatchedToRegexException e) {
			result.put("flag", false);
			result.put("content", e.getMessage());
			return new ResponseEntity<HashMap>(result,HttpStatus.BAD_REQUEST);
		}catch(UUIDNotMatchedToRegexException e) {
			result.put("flag", false);
			result.put("content", e.getMessage());
			return new ResponseEntity<HashMap>(result,HttpStatus.BAD_REQUEST);
		}catch(NotFoundBookTypeException e) {
			result.put("flag", false);
			result.put("content", e.getMessage());
			return new ResponseEntity<HashMap>(result,HttpStatus.BAD_REQUEST);
		}catch(Exception e) {
			e.printStackTrace();
			result.put("flag", false);
			result.put("content", "신규 도서 등록에 실패했습니다.");
			return new ResponseEntity<HashMap>(result,HttpStatus.BAD_REQUEST);
		}
	}
}
