package com.spring.api.service;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import com.spring.api.exception.CustomException;

public interface TokenService {
	public HashMap createNewTokens(HashMap<String,String> param);
	
	public HashMap updateTokens(HttpServletRequest request);
	
	public void deleteTokens(HttpServletRequest request);
}