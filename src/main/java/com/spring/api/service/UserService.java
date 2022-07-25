package com.spring.api.service;

import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

public interface UserService {
	public void createNewUserInfo(HashMap<String,String> param);
	
	public HashMap readUserInfo(HttpServletRequest request, String target_user_id);

	public void deleteUserInfo(HashMap<String,String> param);
	
	public void updateUserInfo(HttpServletRequest request, HashMap<String, String> param);

	public void createCheckoutInfo(HttpServletRequest request, HashMap param);

	public void deleteCheckoutInfo(HttpServletRequest request, HashMap param);

	public void createNewReservationInfo(HttpServletRequest request, HashMap param);

	public void deleteReservationInfo(HttpServletRequest request, HashMap param);

	public List<HashMap> readCheckoutInfo(HttpServletRequest request, HashMap param);

	public List<HashMap> readReservationInfo(HttpServletRequest request, HashMap param);

	public void updateCheckoutInfo(HttpServletRequest request, HashMap param);

	public List<HashMap> readQuestions();
}