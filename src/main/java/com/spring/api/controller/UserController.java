package com.spring.api.controller;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.spring.api.exception.users.DuplicateUserIdException;
import com.spring.api.exception.users.DuplicateUserPhoneException;
import com.spring.api.exception.users.InvalidPublicKeyException;
import com.spring.api.exception.users.QuestionAnswerExceededLimitOnMaxbytesException;
import com.spring.api.exception.users.UUIDNotMatchedToRegexException;
import com.spring.api.exception.users.UserIdNotMatchedToRegexException;
import com.spring.api.exception.users.UserNameNotMatchedToRegexException;
import com.spring.api.exception.users.UserPhoneNotMatchedToRegexException;
import com.spring.api.exception.users.UserPwNotMatchedToRegexException;
import com.spring.api.service.UserService;

//@CrossOrigin
@RestController("userController")
public class UserController {
	@Autowired
	private UserService userService;
	
	@RequestMapping(value="/users/publickeys",method=RequestMethod.GET)
	public ResponseEntity<HashMap> createNewUserKeys() {
		HashMap result = new HashMap();
		try {
			result.put("flag", true);
			result.put("content", "공개키 발급 성공");
			result.put("user_publickey", userService.createNewUserKeys());
			return new ResponseEntity<HashMap>(result,HttpStatus.OK);
		}catch(Exception e) {
			result.put("flag", false);
			result.put("content", "공개키 발급 실패");
			return new ResponseEntity<HashMap>(result,HttpStatus.BAD_REQUEST);
		}
	}
	
	@RequestMapping(value="/users",method=RequestMethod.POST)
	public ResponseEntity<HashMap> createNewUserInfo(@RequestParam HashMap param) {
		HashMap result = new HashMap();
		try {
			userService.createNewUserInfo(param);
			result.put("flag", true);
			result.put("content", "회원가입 성공");
			return new ResponseEntity<HashMap>(result,HttpStatus.CREATED);
		}catch(InvalidPublicKeyException e) {
			result.put("flag", false);
			result.put("content", e.getMessage());
			return new ResponseEntity<HashMap>(result,HttpStatus.BAD_REQUEST);
		}catch(UserIdNotMatchedToRegexException e) {
			result.put("flag", false);
			result.put("content", e.getMessage());
			return new ResponseEntity<HashMap>(result,HttpStatus.BAD_REQUEST);
		}catch(DuplicateUserIdException e) {
			result.put("flag", false);
			result.put("content", e.getMessage());
			return new ResponseEntity<HashMap>(result,HttpStatus.BAD_REQUEST);
		}catch(UserPwNotMatchedToRegexException e) {
			result.put("flag", false);
			result.put("content", e.getMessage());
			return new ResponseEntity<HashMap>(result,HttpStatus.BAD_REQUEST);
		}catch(UserNameNotMatchedToRegexException e) {
			result.put("flag", false);
			result.put("content", e.getMessage());
			return new ResponseEntity<HashMap>(result,HttpStatus.BAD_REQUEST);
		}catch(UserPhoneNotMatchedToRegexException e) {
			result.put("flag", false);
			result.put("content", e.getMessage());
			return new ResponseEntity<HashMap>(result,HttpStatus.BAD_REQUEST);
		}catch(DuplicateUserPhoneException e) {
			result.put("flag", false);
			result.put("content", e.getMessage());
			return new ResponseEntity<HashMap>(result,HttpStatus.BAD_REQUEST);
		}catch(UUIDNotMatchedToRegexException e) {
			result.put("flag", false);
			result.put("content", e.getMessage());
			return new ResponseEntity<HashMap>(result,HttpStatus.BAD_REQUEST);
		}catch(QuestionAnswerExceededLimitOnMaxbytesException e) {
			result.put("flag", false);
			result.put("content", e.getMessage());
			return new ResponseEntity<HashMap>(result,HttpStatus.BAD_REQUEST);
		}catch(Exception e) {
			result.put("flag", false);
			result.put("content", "회원가입 실패");
			return new ResponseEntity<HashMap>(result,HttpStatus.BAD_REQUEST);
		}
	}
	
	@RequestMapping(value="/users",method=RequestMethod.GET)
	public ResponseEntity<HashMap> readUserInfo(@RequestParam HashMap param) {
		HashMap result = new HashMap();
		try {
			userService.readUserInfo(param);
			result.put("flag", true);
			result.put("content", "정보조회 성공");
			return new ResponseEntity<HashMap>(result,HttpStatus.OK);
		}catch(Exception e) {
			e.printStackTrace();
			result.put("flag", false);
			result.put("content", "정보조회 실패");
			return new ResponseEntity<HashMap>(result,HttpStatus.BAD_REQUEST);
		}
	}
}