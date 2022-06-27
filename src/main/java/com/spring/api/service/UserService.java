package com.spring.api.service;

import java.util.HashMap;

import com.spring.api.exception.users.DuplicateUserIdException;
import com.spring.api.exception.users.DuplicateUserPhoneException;
import com.spring.api.exception.users.InvalidPublicKeyException;
import com.spring.api.exception.users.QuestionAnswerExceededLimitOnMaxbytesException;
import com.spring.api.exception.users.UUIDNotMatchedToRegexException;
import com.spring.api.exception.users.UserIdNotMatchedToRegexException;
import com.spring.api.exception.users.UserNameNotMatchedToRegexException;
import com.spring.api.exception.users.UserPhoneNotMatchedToRegexException;
import com.spring.api.exception.users.UserPwNotMatchedToRegexException;

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
	
	public HashMap readUserInfo(HashMap<String,String> param);
	public void updateUserInfo(HashMap<String,String> param);
	public void deleteUserInfo(HashMap<String,String> param);
	public String createNewUserKeys() throws Exception;
}