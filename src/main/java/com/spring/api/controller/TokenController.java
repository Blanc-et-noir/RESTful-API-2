package com.spring.api.controller;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.spring.api.exception.users.InvalidPublicKeyException;
import com.spring.api.exception.users.NotFoundUserException;
import com.spring.api.exception.users.UserIdNotMatchedToRegexException;
import com.spring.api.exception.users.UserPwNotMatchedException;
import com.spring.api.exception.users.UserPwNotMatchedToRegexException;
import com.spring.api.service.TokenService;
import com.spring.api.util.JwtUtil;

@RestController("tokenController")
public class TokenController {
	@Autowired
	private TokenService tokenService;
	
	@RequestMapping(value = "/tokens",method = RequestMethod.POST)
	public ResponseEntity<HashMap> createNewTokens(HttpServletResponse response, @RequestParam HashMap<String,String> param) {
		HashMap result = new HashMap();
		try {
			HashMap<String,String> tokens = tokenService.createNewTokens(param);
			
			JwtUtil.setAccesstoken(response, tokens.get("user_accesstoken"));
			JwtUtil.setRefreshtoken(response, tokens.get("user_refreshtoken"));
			
			result.put("flag", true);
			result.put("content", "로그인에 성공했습니다.");
			return new ResponseEntity<HashMap>(result,HttpStatus.CREATED);
		}catch(InvalidPublicKeyException e) {
			result.put("flag", false);
			result.put("content", e.getMessage());
			return new ResponseEntity<HashMap>(result,HttpStatus.BAD_REQUEST);
		}catch(NotFoundUserException e) {
			result.put("flag", false);
			result.put("content", e.getMessage());
			return new ResponseEntity<HashMap>(result,HttpStatus.BAD_REQUEST);
		}catch(UserIdNotMatchedToRegexException e) {
			result.put("flag", false);
			result.put("content", e.getMessage());
			return new ResponseEntity<HashMap>(result,HttpStatus.BAD_REQUEST);
		}catch(UserPwNotMatchedToRegexException e) {
			result.put("flag", false);
			result.put("content", e.getMessage());
			return new ResponseEntity<HashMap>(result,HttpStatus.BAD_REQUEST);
		}catch(UserPwNotMatchedException e) {
			result.put("flag", false);
			result.put("content", e.getMessage());
			return new ResponseEntity<HashMap>(result,HttpStatus.BAD_REQUEST);
		}catch(Exception e) {
			e.printStackTrace();
			result.put("flag", false);
			result.put("content", "로그인에 실패했습니다.");
			return new ResponseEntity<HashMap>(result,HttpStatus.BAD_REQUEST);
		}
	}
	
	@RequestMapping(value = "/tokens",method = RequestMethod.PUT)
	public ResponseEntity<HashMap> updateTokens(HttpServletRequest request, HttpServletResponse response) {
		HashMap result = new HashMap();
		try {
			HashMap<String,String> tokens = tokenService.updateTokens(request);
			
			String user_accesstoken = tokens.get("user_accesstoken");
			String user_refreshtoken = tokens.get("user_refreshtoken");
			
			JwtUtil.setAccesstoken(response, user_accesstoken);
			JwtUtil.setRefreshtoken(response, user_refreshtoken);
			
			result.put("flag", true);
			result.put("content", "액세스 및 리프레쉬 토큰 갱신에 성공했습니다.");
			return new ResponseEntity<HashMap>(result,HttpStatus.OK);
		}catch(Exception e) {
			e.printStackTrace();
			result.put("flag", false);
			result.put("content", "액세스 및 리프레쉬 토큰 갱신에 실패했습니다.");
			return new ResponseEntity<HashMap>(result,HttpStatus.BAD_REQUEST);
		}
	}
	
	@RequestMapping(value = "/tokens",method = RequestMethod.DELETE)
	public ResponseEntity<HashMap> deleteTokens(HttpServletRequest request) {
		HashMap result = new HashMap();
		try {
			tokenService.deleteTokens(request);
			result.put("flag", true);
			result.put("content", "로그아웃에 성공했습니다.");
			return new ResponseEntity<HashMap>(result,HttpStatus.OK);
		}catch(Exception e) {
			e.printStackTrace();
			result.put("flag", false);
			result.put("content", "로그아웃에 실패했습니다.");
			return new ResponseEntity<HashMap>(result,HttpStatus.BAD_REQUEST);
		}
	}
}