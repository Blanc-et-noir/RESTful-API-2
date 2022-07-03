package com.spring.api.controller;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.spring.api.service.TokenService;
import com.spring.api.util.JwtUtil;

@RestController("tokenController")
public class TokenController {
	@Autowired
	private JwtUtil jwtUtil;
	@Autowired
	private TokenService tokenService;
	
	@RequestMapping(value = "/tokens",method = RequestMethod.POST)
	public ResponseEntity<HashMap> createNewTokens(HttpServletResponse response, @RequestBody HashMap<String,String> param){
		HashMap result = new HashMap();
		HashMap<String,String> tokens = tokenService.createNewTokens(param);
		
		jwtUtil.setAccesstoken(response, tokens.get("user_accesstoken"));
		jwtUtil.setRefreshtoken(response, tokens.get("user_refreshtoken"));
		
		result.put("flag", true);
		result.put("content", "로그인에 성공했습니다.");
		return new ResponseEntity<HashMap>(result,HttpStatus.CREATED);
	}
	
	@RequestMapping(value = "/tokens",method = RequestMethod.PUT)
	public ResponseEntity<HashMap> updateTokens(HttpServletRequest request, HttpServletResponse response){
		HashMap result = new HashMap();
		HashMap<String,String> tokens = tokenService.updateTokens(request);
		
		String user_accesstoken = tokens.get("user_accesstoken");
		String user_refreshtoken = tokens.get("user_refreshtoken");
		
		jwtUtil.setAccesstoken(response, user_accesstoken);
		jwtUtil.setRefreshtoken(response, user_refreshtoken);
		
		result.put("flag", true);
		result.put("content", "액세스 및 리프레쉬 토큰 갱신에 성공했습니다.");
		return new ResponseEntity<HashMap>(result,HttpStatus.OK);
	}
	
	@RequestMapping(value = "/tokens",method = RequestMethod.DELETE)
	public ResponseEntity<HashMap> deleteTokens(HttpServletRequest request){
		HashMap result = new HashMap();
		tokenService.deleteTokens(request);
		result.put("flag", true);
		result.put("content", "로그아웃에 성공했습니다.");
		return new ResponseEntity<HashMap>(result,HttpStatus.OK);
	}
}