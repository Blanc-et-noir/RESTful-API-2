package com.spring.api.controller;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
import com.spring.api.exception.users.UserPwNotMatchedToRegexException;
import com.spring.api.exception.users.UserQuestionAnswerNotMatchedException;
import com.spring.api.service.UserService;

@RestController("userController")
public class UserController {
	@Autowired
	private UserService userService;
	
	@RequestMapping(value="/users/publickeys",method=RequestMethod.GET)
	public ResponseEntity<HashMap> createNewUserKeys() {
		HashMap result = new HashMap();
		try {
			result.put("flag", true);
			result.put("content", "공개키 발급에 성공했습니다.");
			result.put("user_publickey", userService.createNewUserKeys());
			return new ResponseEntity<HashMap>(result,HttpStatus.OK);
		}catch(Exception e) {
			result.put("flag", false);
			result.put("content", "공개키 발급에 실패했습니다.");
			return new ResponseEntity<HashMap>(result,HttpStatus.BAD_REQUEST);
		}
	}
	
	@RequestMapping(value="/users",method=RequestMethod.POST)
	public ResponseEntity<HashMap> createNewUserInfo(@RequestParam HashMap param) {
		HashMap result = new HashMap();
		try {
			userService.createNewUserInfo(param);
			result.put("flag", true);
			result.put("content", "회원가입에 성공했습니다.");
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
			result.put("content", "회원가입에 실패했습니다.");
			return new ResponseEntity<HashMap>(result,HttpStatus.BAD_REQUEST);
		}
	}
	
	@RequestMapping(value="/users/{user_id}",method=RequestMethod.GET)
	public ResponseEntity<HashMap> readUserInfo(HttpServletRequest request, @PathVariable(value="user_id") String user_id) {
		HashMap result = new HashMap();
		try {
			HashMap user = userService.readUserInfo(request, user_id);
			result.put("user", user);
			result.put("flag", true);
			result.put("content", "회원정보 조회에 성공했습니다.");
			return new ResponseEntity<HashMap>(result,HttpStatus.OK);
		}catch(NotFoundUserException e) {
			result.put("flag", false);
			result.put("content", e.getMessage());
			return new ResponseEntity<HashMap>(result,HttpStatus.BAD_REQUEST);
		}catch(NotAuthorizedException e) {
			result.put("flag", false);
			result.put("content", e.getMessage());
			return new ResponseEntity<HashMap>(result,HttpStatus.FORBIDDEN);
		}catch(Exception e) {
			e.printStackTrace();
			result.put("flag", false);
			result.put("content", "회원정보 조회에 실패했습니다.");
			return new ResponseEntity<HashMap>(result,HttpStatus.BAD_REQUEST);
		}
	}
	
	@RequestMapping(value="/users/{user_id}",method={RequestMethod.PUT})
	public ResponseEntity<HashMap> updateUserInfo(HttpServletRequest request, @RequestParam HashMap param, @PathVariable("user_id") String target_user_id) {
		HashMap result = new HashMap();
		try {
			param.put("user_id", target_user_id);
			userService.updateUserInfo(request, param);
			result.put("flag", true);
			result.put("content", "회원정보 변경에 성공했습니다.");
			return new ResponseEntity<HashMap>(result,HttpStatus.OK);
		}catch(InvalidPublicKeyException e) {
			result.put("flag", false);
			result.put("content", e.getMessage());
			return new ResponseEntity<HashMap>(result,HttpStatus.BAD_REQUEST);
		}catch(NotFoundUserException e) {
			result.put("flag", false);
			result.put("content", e.getMessage());
			return new ResponseEntity<HashMap>(result,HttpStatus.BAD_REQUEST);
		}catch(NotAuthorizedException e) {
			result.put("flag", false);
			result.put("content", e.getMessage());
			return new ResponseEntity<HashMap>(result,HttpStatus.FORBIDDEN);
		}catch(UserQuestionAnswerNotMatchedException e) {
			result.put("flag", false);
			result.put("content", e.getMessage());
			return new ResponseEntity<HashMap>(result,HttpStatus.BAD_REQUEST);
		}catch(UserNameNotMatchedToRegexException e) {
			result.put("flag", false);
			result.put("content", e.getMessage());
			return new ResponseEntity<HashMap>(result,HttpStatus.BAD_REQUEST);
		}catch(UserPwNotMatchedToRegexException e) {
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
		}catch(DuplicateUserPhoneException e) {
			result.put("flag", false);
			result.put("content", e.getMessage());
			return new ResponseEntity<HashMap>(result,HttpStatus.BAD_REQUEST);
		}catch(UserPhoneNotMatchedToRegexException e) {
			result.put("flag", false);
			result.put("content", e.getMessage());
			return new ResponseEntity<HashMap>(result,HttpStatus.BAD_REQUEST);
		}catch(NotchangeableUserSaltException e) {
			result.put("flag", false);
			result.put("content", e.getMessage());
			return new ResponseEntity<HashMap>(result,HttpStatus.BAD_REQUEST);
		}catch(Exception e) {
			e.printStackTrace();
			result.put("flag", false);
			result.put("content", "회원정보 변경에 실패했습니다.");
			return new ResponseEntity<HashMap>(result,HttpStatus.BAD_REQUEST);
		}
	}
	
	@RequestMapping(value="/users",method={RequestMethod.DELETE})
	public ResponseEntity<HashMap> deleteUserInfo(@RequestParam HashMap param) {
		HashMap result = new HashMap();
		try {
			result.put("flag", true);
			result.put("content", "회원탈퇴에 성공했습니다.");
			return new ResponseEntity<HashMap>(result,HttpStatus.OK);
		}catch(Exception e) {
			e.printStackTrace();
			result.put("flag", false);
			result.put("content", "회원탈퇴에 실패했습니다.");
			return new ResponseEntity<HashMap>(result,HttpStatus.BAD_REQUEST);
		}
	}
}