package com.spring.api.service;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import com.spring.api.exception.users.DuplicateUserIdException;
import com.spring.api.exception.users.DuplicateUserPhoneException;
import com.spring.api.exception.users.InvalidPublicKeyException;
import com.spring.api.exception.users.NotAuthorizedException;
import com.spring.api.exception.users.NotFoundUserException;
import com.spring.api.exception.users.NotchangeableUserSaltException;
import com.spring.api.exception.users.QuestionAnswerExceededLimitOnMaxbytesException;
import com.spring.api.exception.users.UUIDNotMatchedToRegexException;
import com.spring.api.exception.users.UserIdNotMatchedToRegexException;
import com.spring.api.exception.users.UserNameNotMatchedToRegexException;
import com.spring.api.exception.users.UserPhoneNotMatchedToRegexException;
import com.spring.api.exception.users.UserPwNotMatchedException;
import com.spring.api.exception.users.UserPwNotMatchedToRegexException;
import com.spring.api.exception.users.UserQuestionAnswerNotMatchedException;

public interface UserService {
	public void createNewUserInfo(HashMap<String,String> param) throws
		InvalidPublicKeyException,
		UserIdNotMatchedToRegexException,
		DuplicateUserIdException,
		UserPwNotMatchedToRegexException,
		UserNameNotMatchedToRegexException,
		UserPhoneNotMatchedToRegexException,
		DuplicateUserPhoneException,
		UUIDNotMatchedToRegexException,
		QuestionAnswerExceededLimitOnMaxbytesException,
		Exception;
	
	public HashMap readUserInfo(HttpServletRequest request, String target_user_id) throws
		NotFoundUserException,
		NotAuthorizedException,
		Exception ;

	public void deleteUserInfo(HashMap<String,String> param);
	
	public String createNewUserKeys() throws Exception;

	public void updateUserInfo(HttpServletRequest request, HashMap<String, String> param) throws 
		NotchangeableUserSaltException, 
		InvalidPublicKeyException, 
		NotFoundUserException, 
		NotAuthorizedException, 
		UserPwNotMatchedException, 
		UserQuestionAnswerNotMatchedException, 
		UserPwNotMatchedToRegexException, 
		QuestionAnswerExceededLimitOnMaxbytesException, 
		DuplicateUserPhoneException, 
		UserNameNotMatchedToRegexException, 
		UUIDNotMatchedToRegexException, 
		UserPhoneNotMatchedToRegexException, 
		Exception;
}