package com.spring.api.service;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.multipart.MultipartRequest;

import com.spring.api.exception.books.AuthorNameNotMatchedToRegexException;
import com.spring.api.exception.books.BookIsbnNotMatchedToRegexException;
import com.spring.api.exception.books.BookNameExceededLimitOnMaxbytesException;
import com.spring.api.exception.books.BookQuantityNotMatchedToRegexException;
import com.spring.api.exception.books.NotFoundBookTypeException;
import com.spring.api.exception.books.PublisherNameNotMatchedToRegexException;
import com.spring.api.exception.books.TooFewAuthorsException;
import com.spring.api.exception.books.TranslatorNameNotMatchedToRegexException;
import com.spring.api.exception.users.NotAuthorizedException;
import com.spring.api.exception.users.NotFoundUserException;
import com.spring.api.exception.users.UUIDNotMatchedToRegexException;

public interface BookService {

	void createNewBookInfo(MultipartRequest mRequest,HttpServletRequest request) throws TooFewAuthorsException, AuthorNameNotMatchedToRegexException, BookQuantityNotMatchedToRegexException, NotAuthorizedException, NotFoundUserException, BookIsbnNotMatchedToRegexException, BookNameExceededLimitOnMaxbytesException, PublisherNameNotMatchedToRegexException, TranslatorNameNotMatchedToRegexException, UUIDNotMatchedToRegexException, NotFoundBookTypeException, Exception;

}
