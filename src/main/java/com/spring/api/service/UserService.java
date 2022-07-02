package com.spring.api.service;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import com.spring.api.exception.CustomException;

public interface UserService {
	public void createNewUserInfo(HashMap<String,String> param);
	
	public HashMap readUserInfo(HttpServletRequest request, String target_user_id);

	public void deleteUserInfo(HashMap<String,String> param);
	
	public String createNewUserKeys();

	public void updateUserInfo(HttpServletRequest request, HashMap<String, String> param);
}