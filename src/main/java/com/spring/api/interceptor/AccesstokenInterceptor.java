package com.spring.api.interceptor;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.spring.api.util.JwtUtil;
import com.spring.api.util.RedisUtil;

import io.jsonwebtoken.ExpiredJwtException;

public class AccesstokenInterceptor implements HandlerInterceptor{
	@Autowired
	private JwtUtil jwtUtil;
	@Autowired
	private RedisUtil redisUtil;
	
	//�׼��� ��ū�� �ʿ��� ����� ��û�Ҷ�, �ش� �׼��� ��ū�� ��ȿ���� ������� ���� �޼����� ������.
	private static void setErrorMessage(HttpServletResponse response, int errorcode, String message){
		//1. JSON�� ���·� ��û ���� ���� �� �޼����� ������.
		try {
			JSONObject json = new JSONObject();
			json.put("flag", false);
			json.put("content", message);
			
			response.setStatus(errorcode);
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			response.getWriter().print(json);
		//2. ��Ÿ ���ܰ� �߻��� ���.
		} catch (IOException e) {
			
		}
	}
	
	@Override
	//�׼��� ��ū�� �ʿ��� ����� ȣ���ϸ�, �ش� �׼��� ��ū�� ��ȿ�� ���θ� �Ǵ��Ͽ� �ش� ��û�� ��Ʈ�ѷ��� �������� ���θ� ������.
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		String user_accesstoken = jwtUtil.getAccesstoken(request);
		String uri = request.getRequestURI();
		String method = request.getMethod();
		
		System.out.println(uri +" "+method+" ");
		
		if(uri.endsWith("/")) {
			uri = uri.substring(0,uri.length()-1);
		}
		
		if(method.equalsIgnoreCase("OPTIONS")) {
			System.out.println("0");
			return true;
		}
		
		//1. �ű� ȸ������ ��û�ÿ��� �׼��� ��ū�� �ʿ����.
		if(uri.equals("/api/users")&&method.equalsIgnoreCase("POST")){
			System.out.println("1");
			return true;
		//2. ����Ű �߱� �Ǵ� ���� �帣 ��ȸ�� �׼��� ��ū�� �ʿ����.
		}else if((/*uri.equals("/api/users/publickeys")||*/uri.equals("/api/books/book_types"))&&method.equalsIgnoreCase("GET")) {
			System.out.println("2");
			return true;
		//3. ��й�ȣ ã�� ���� ��ȸ�� �׼��� ��ū�� �ʿ����.
		}else if(uri.equals("/api/users/questions")&&method.equalsIgnoreCase("GET")) {
			System.out.println("3");
			return true;
		}

		//3. �׼��� ��ū�� �ʿ��ϳ� �׼��� ��ū�� ���� ��쿡��, UNAUTHORIZED ����.
		if(user_accesstoken==null) {
			setErrorMessage(response,401,"�׼��� ��ū�� �������� �ʽ��ϴ�.");
			System.out.println("4");
			return false;
		//4. �׼��� ��ū�� �ʿ��ϰ�, �׼��� ��ū�� �ִ� ��쿡�� �Ʒ��� ó�������� ������.
		}else {
			//5. Redis�� ������Ʈ�� �����ϴ� �׼��� ��ū�� ��쿡�� ���������� �α׾ƿ� ó���� ��ū�̹Ƿ� UNAUTHORIZED ����.
			if(redisUtil.getData(user_accesstoken)!=null) {
				setErrorMessage(response,400,"�ش� �׼��� ��ū�� �̹� �α׾ƿ� ó���Ǿ����ϴ�.");
				System.out.println("5");
				return false;
			}
			
			//6. �׼��� ��ū�� �������� �ʾҰ�, ���� ��ȿ�Ⱓ�� �����ִ� ��� ��Ʈ�ѷ��� �ش� ��û�� ������.
			try {
				jwtUtil.validateToken(user_accesstoken);
				System.out.println("6");
				return true;
			//7. �׼��� ��ū�� ��ȿ�Ⱓ�� ���� ��� UNAUTHORIZED ����.
			}catch(ExpiredJwtException e) {
				setErrorMessage(response,401,"�ش� �׼��� ��ū�� ������ ����Ǿ����ϴ�.");
				System.out.println("7");
				return false;
			//8. �׼��� ��ū�� ������ ��� UNAUTHORIZED ����.
			}catch(Exception e) {
				e.printStackTrace();
				setErrorMessage(response,401,"�ش� �׼��� ��ū�� �����Ǿ����ϴ�.");
				System.out.println("8");
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