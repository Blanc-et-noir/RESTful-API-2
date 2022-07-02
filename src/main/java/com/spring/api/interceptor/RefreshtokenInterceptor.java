package com.spring.api.interceptor;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.spring.api.dao.TokenDAO;
import com.spring.api.util.JwtUtil;
import com.spring.api.util.RedisUtil;

import io.jsonwebtoken.ExpiredJwtException;

public class RefreshtokenInterceptor implements HandlerInterceptor{
	@Autowired
	private JwtUtil jwtUtil;
	@Autowired
	private RedisUtil redisUtil;
	@Autowired
	private TokenDAO tokenDAO;
	
	//�������� ��ū�� �ʿ��� ����� ��û�Ҷ�, �ش� �������� ��ū�� ��ȿ���� ������� ���� �޼����� ������.
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
	//�������� ��ū�� �ʿ��� ����� ȣ���ϸ�, �ش� �������� ��ū�� ��ȿ�� ���θ� �Ǵ��Ͽ� �ش� ��û�� ��Ʈ�ѷ��� �������� ���θ� ������.
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {	
		String user_accesstoken = jwtUtil.getAccesstoken(request);
		String user_refreshtoken = jwtUtil.getRefreshtoken(request);
		String uri = request.getRequestURI();
		String method = request.getMethod();
		
		System.out.println(method+" "+uri+" "+(user_accesstoken!=null)+" "+(user_refreshtoken!=null));
		
		if(method.equalsIgnoreCase("OPTIONS")) {
			System.out.println(0);
			return true;
		}
		
		if(uri.endsWith("/")) {
			uri = uri.substring(0,uri.length()-1);
		}
		
		//1. �α����� �õ��Ͽ� �׼���, �������� ��ū�� ���� �߱޹����� ��û�ϴ� ��쿡�� �������� ��ū�� �ʿ����.
		if(uri.equals("/api/tokens")&&method.equals("POST")) {
			System.out.println("1");
			return true;
		}
		
		//2. �������� ��ū�� �ʿ��ϳ� �������� ��ū�� ���� ��쿡��, UNAUTHORIZED ����.
		if(user_refreshtoken == null) {
			setErrorMessage(response,401,"�������� ��ū�� �������� �ʽ��ϴ�.");
			System.out.println("2");
			return false;
		//3. �׼��� ��ū�� �ʿ��ϳ� �׼��� ��ū�� ���� ��쿡��, UNAUTHORIZED ����.
		}else if(user_accesstoken==null) {
			setErrorMessage(response,401,"�׼��� ��ū�� �������� �ʽ��ϴ�.");
			System.out.println("3");
			return false;
		}else {
			//4. Redis�� ������Ʈ�� �����ϴ� �������� ��ū�� ��쿡�� ���������� �α׾ƿ� ó���� ��ū�̹Ƿ� UNAUTHORIZED ����.
			if(redisUtil.getData(user_refreshtoken)!=null) {
				setErrorMessage(response,401,"�ش� �������� ��ū�� �̹� �α׾ƿ� ó���Ǿ����ϴ�.");
				System.out.println("4");
				return false;
			}
			
			//5. Redis�� ������Ʈ�� �����ϴ� �׼��� ��ū�� ��쿡�� ���������� �α׾ƿ� ó���� ��ū�̹Ƿ� UNAUTHORIZED ����.
			if(redisUtil.getData(user_accesstoken)!=null) {
				setErrorMessage(response,401,"�ش� �׼��� ��ū�� �̹� �α׾ƿ� ó���Ǿ����ϴ�.");
				System.out.println("5");
				return false;
			}
			
			//6. �׼��� ��ū�� �������� �ʾҰ�, ���� ��ȿ�Ⱓ�� �����ִ� ��� �������� ��ū�� ��ȿ���� �˻���.
			try {
				jwtUtil.validateToken(user_accesstoken);
			}catch(ExpiredJwtException e) {

			//7. �׼��� ��ū�� ������ ��� UNAUTHORIZED ����.
			}catch(Exception e) {
				setErrorMessage(response,401,"�ش� �׼��� ��ū�� �����Ǿ����ϴ�.");
				System.out.println("6");
				return false;
			}
			
			//8. �������� ��ū�� �������� �ʾҰ�, ���� ��ȿ�Ⱓ�� �����ִ� ��� ��Ʈ�ѷ��� �ش� ��û�� ������.
			try {
				jwtUtil.validateToken(user_refreshtoken);
				
				//9. �׼��� ��ū�� �������� ��ū�� ������ ��ġ�ϴ��� Ȯ����.
				String access_user_id = (String)jwtUtil.getData(user_accesstoken, "user_id");
				String refresh_user_id = (String)jwtUtil.getData(user_refreshtoken, "user_id");

				if(!access_user_id.equals(refresh_user_id)) {
					setErrorMessage(response,401,"�׼��� ��ū �� �������� ��ū �߱��� ������ ��ġ���� �ʽ��ϴ�.");
					System.out.println("7");
					return false;
				}
				
				//10. ���޹��� �׼���, �������� ��ū�� ���� ����ڰ� ������� �׼���, �������� ��ū���� Ȯ����.
				HashMap param = new HashMap();
				param.put("user_id", access_user_id);
				HashMap tokens = tokenDAO.getUserTokensByUserId(param);
				
				//11. �ش� ����ڰ� ����ϴ� �׼���, �������� ��ū�� �ƴѰ�� UNAUTHORIZED ����.
				if(!user_accesstoken.equals(tokens.get("user_accesstoken"))||!user_refreshtoken.equals(tokens.get("user_refreshtoken"))) {
					setErrorMessage(response,401,"�׼��� ��ū �Ǵ� �������� ��ū�� �ش� ����ڰ� ���� ������� ���� �ƴմϴ�.");
					System.out.println("8");
					return false;
				}
				System.out.println("9");
				return true;
			//12. �������� ��ū�� ��ȿ�Ⱓ�� ���� ��� UNAUTHORIZED ����.
			}catch(ExpiredJwtException e) {
				setErrorMessage(response,401,"�ش� �������� ��ū�� ������ ����Ǿ����ϴ�.");
				System.out.println("10");
				return false;
			//13. �������� ��ū�� ������ ��� UNAUTHORIZED ����.
			}catch(Exception e) {
				setErrorMessage(response,401,"�ش� �������� ��ū�� �����Ǿ����ϴ�.");
				System.out.println("11");
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