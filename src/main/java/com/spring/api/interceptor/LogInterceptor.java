package com.spring.api.interceptor;

import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartRequest;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import com.spring.api.util.JwtUtil;

public class LogInterceptor implements HandlerInterceptor{
	@Autowired
	private JwtUtil jwtUtil;
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private long getExpiration(String token) {
		long exp = jwtUtil.getExpiration(token);
		return exp > 1 ? exp : 0;
	}
	
	private String getUserId(String token) {
		String user_id = (String) jwtUtil.getData(token,"user_id");
		return user_id;
	}
	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

		return true;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
		ModelAndView modelAndView) throws Exception {
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
		String method = request.getMethod();
		String uri = request.getRequestURI();
		String contentType = request.getContentType();
		int contentLength = request.getContentLength();
		
		String user_accesstoken = jwtUtil.getAccesstoken(request);
		String user_refreshtoken = jwtUtil.getRefreshtoken(request);

		logger.info("\n");
		
		Enumeration params = request.getParameterNames();
		
		if(params.hasMoreElements()) {
			uri += "?";
		}

		while(params.hasMoreElements()) {
			String param = (String) params.nextElement();
			String value = request.getParameter(param);
			uri += param+"="+value+"&";
		}
		
		if(uri.endsWith("&")) {
			uri = uri.substring(0, uri.length()-1);
		}
		
		logger.info(method+" "+uri+"\n");
		
		if(contentType!=null) {
			logger.info("Content-type : "+contentType+"\n");
		}
		if(contentLength!=-1) {
			logger.info("Content-Length : "+contentLength+"\n");
		}
		
		if(user_accesstoken!=null) {
			long exp = jwtUtil.getExpiration(user_accesstoken);
			logger.info("user_accesstoken : "+getUserId(user_accesstoken)+", "+getExpiration(user_accesstoken)+"\n");
		}
		
		if(user_refreshtoken!=null) {
			long exp = jwtUtil.getExpiration(user_refreshtoken);
			logger.info("user_refreshtoken : "+getUserId(user_refreshtoken)+", "+getExpiration(user_refreshtoken)+"\n");
		}

		if(contentType!=null&&contentType.equals("application/json")) {
	        ContentCachingRequestWrapper req = (ContentCachingRequestWrapper)request;
	        ContentCachingResponseWrapper resp = (ContentCachingResponseWrapper)response;
			
	        byte[] requestBody = req.getContentAsByteArray();
	        byte[] responseBody = resp.getContentAsByteArray();
	        
	        if(requestBody!=null) {
	        	logger.info(new String(requestBody, StandardCharsets.UTF_8)+"\n");
	        }
		}
	}
}