package com.spring.api.service;

import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

public interface MessageService {

	void createNewMessage(HttpServletRequest request,HashMap<String, String> param);

	HashMap readMessages(HttpServletRequest request, HashMap<String, String> param);

	void deleteMessages(HttpServletRequest request, HashMap<String, String> param);

}