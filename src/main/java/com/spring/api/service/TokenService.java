package com.spring.api.service;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import com.spring.api.exception.users.InvalidPublicKeyException;
import com.spring.api.exception.users.NotFoundUserException;
import com.spring.api.exception.users.UserIdNotMatchedToRegexException;
import com.spring.api.exception.users.UserPwNotMatchedException;
import com.spring.api.exception.users.UserPwNotMatchedToRegexException;

public interface TokenService {
	public HashMap createNewTokens(HashMap<String,String> param) throws
	InvalidPublicKeyException,
	NotFoundUserException,
	UserIdNotMatchedToRegexException,
	UserPwNotMatchedToRegexException,
	UserPwNotMatchedException,
	Exception;
	
	public HashMap updateTokens(HttpServletRequest request);
	
	public void deleteTokens(HttpServletRequest request);
}