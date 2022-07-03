package com.spring.api.service;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.multipart.MultipartRequest;

import com.spring.api.exception.CustomException;

public interface BookService {

	void createNewBookInfo(MultipartRequest mRequest,HttpServletRequest request);

	List readBookTypes();

}
