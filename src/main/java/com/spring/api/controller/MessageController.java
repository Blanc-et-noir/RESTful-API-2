package com.spring.api.controller;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.spring.api.service.MessageService;
import com.spring.api.util.JwtUtil;

@RestController("messageController")
public class MessageController {
	@Autowired
	private JwtUtil jwtUtil;
	@Autowired
	private MessageService messageService;
	
	
	//1. 메세지 송신 요청
	@RequestMapping(value = "/users/{user_id}/messages",method = RequestMethod.POST)
	public ResponseEntity<HashMap> createNewMessage(HttpServletRequest request, @RequestBody HashMap<String,String> param, @PathVariable("user_id") String user_id){
		HashMap result = new HashMap();
		param.put("user_id", user_id);
		messageService.createNewMessage(request, param);
		result.put("flag", true);
		result.put("content", "메세지 송신에 성공했습니다.");
		return new ResponseEntity<HashMap>(result,HttpStatus.CREATED);
	}
	
	//2. 메세지 조회 요청
	@RequestMapping(value = "/users/{user_id}/messages",method = RequestMethod.GET)
	public ResponseEntity<HashMap> readMessages(HttpServletRequest request, @RequestParam HashMap<String,String> param, @PathVariable("user_id") String user_id){
		param.put("user_id", user_id);
		HashMap result = messageService.readMessages(request, param);
		result.put("flag", true);
		result.put("content", "메세지 조회에 성공했습니다.");
		return new ResponseEntity<HashMap>(result,HttpStatus.OK);
	}
	
	//3. 메세지 삭제 요청
	@RequestMapping(value = "/users/{user_id}/messages/{message_id}",method = RequestMethod.DELETE)
	public ResponseEntity<HashMap> deleteMessages(HttpServletRequest request, @PathVariable("user_id") String user_id,  @PathVariable("message_id") String message_id){
		HashMap result = new HashMap();
		HashMap param = new HashMap();
		param.put("user_id", user_id);
		param.put("message_id", message_id);
		messageService.deleteMessages(request, param);
		result.put("flag", true);
		result.put("content", "메세지 삭제에 성공했습니다.");
		return new ResponseEntity<HashMap>(result,HttpStatus.OK);
	}
}
