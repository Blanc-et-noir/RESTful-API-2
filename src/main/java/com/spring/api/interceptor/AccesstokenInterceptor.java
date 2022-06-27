package com.spring.api.interceptor;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.spring.api.util.JwtUtil;
import com.spring.api.util.RedisUtil;

import io.jsonwebtoken.ExpiredJwtException;

public class AccesstokenInterceptor implements HandlerInterceptor{
	
	//�׼��� ��ū�� �ʿ��� ����� ��û�Ҷ�, �ش� �׼��� ��ū�� ��ȿ���� ������� ���� �޼����� ������.
	private static void setErrorMessage(HttpServletResponse response, int errorcode, String message){
		//1. JSON�� ���·� ��û ���� ���� �� �޼����� ������.
		try {
			JSONObject json = new JSONObject();
			json.put("flag", false);
			json.put("content", message);
			
			response.setStatus(401);
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			response.getWriter().write(json.toString());
		//2. ��Ÿ ���ܰ� �߻��� ���.
		} catch (IOException e) {
			
		}
	}
	
	@Override
	//�׼��� ��ū�� �ʿ��� ����� ȣ���ϸ�, �ش� �׼��� ��ū�� ��ȿ�� ���θ� �Ǵ��Ͽ� �ش� ��û�� ��Ʈ�ѷ��� �������� ���θ� ������.
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		String user_accesstoken = JwtUtil.getAccesstoken(request);
		String uri = request.getRequestURI();
		String method = request.getMethod();
		
		if(uri.endsWith("/")) {
			uri = uri.substring(0,uri.length()-1);
		}

		//1. �ű� ȸ������ ��û�ÿ��� �׼��� ��ū�� �ʿ����.
		if(uri.equals("/api/users")&&method.equalsIgnoreCase("POST")){
			return true;
		//2. ȸ�������� ��ȸ�ϴ� ��û ���� ��� GET ��û�� �׼��� ��ū�� �ʿ����.
		}else if(!uri.equals("/api/users")&&method.equalsIgnoreCase("GET")) {
			return true;
		}
		
		//3. �׼��� ��ū�� �ʿ��ϳ� �׼��� ��ū�� ���� ��쿡��, UNAUTHORIZED ����.
		if(user_accesstoken==null) {
			setErrorMessage(response,401,"�α��� ������ ��ȿ���� ����");
			return false;
		//4. �׼��� ��ū�� �ʿ��ϰ�, �׼��� ��ū�� �ִ� ��쿡�� �Ʒ��� ó�������� ������.
		}else {
			//5. Redis�� ������Ʈ�� �����ϴ� �׼��� ��ū�� ��쿡�� ���������� �α׾ƿ� ó���� ��ū�̹Ƿ� UNAUTHORIZED ����.
			if(RedisUtil.getData(user_accesstoken)!=null) {
				setErrorMessage(response,401,"�α��� ������ ��ȿ���� ����");
				return false;
			}
			
			//6. �׼��� ��ū�� �������� �ʾҰ�, ���� ��ȿ�Ⱓ�� �����ִ� ��� ��Ʈ�ѷ��� �ش� ��û�� ������.
			try {
				JwtUtil.validateToken(user_accesstoken);
				return true;
			//7. �׼��� ��ū�� ��ȿ�Ⱓ�� ���� ��� UNAUTHORIZED ����.
			}catch(ExpiredJwtException e) {
				setErrorMessage(response,401,"�α��� ������ ��ȿ���� ����");
				return false;
			//8. �׼��� ��ū�� ������ ��� UNAUTHORIZED ����.
			}catch(Exception e) {
				setErrorMessage(response,401,"�α��� ������ ��ȿ���� ����");
				return false;
			}
		}
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,ModelAndView modelAndView) throws Exception {
		// TODO Auto-generated method stub
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
		// TODO Auto-generated method stub
		
	}
}