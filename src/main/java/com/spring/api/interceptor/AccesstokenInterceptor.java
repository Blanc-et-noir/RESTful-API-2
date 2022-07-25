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
	
	//액세스 토큰이 필요한 기능을 요청할때, 해당 액세스 토큰이 유효하지 않은경우 에러 메세지를 전달함.
	private static void setErrorMessage(HttpServletResponse response, int errorcode, String message){
		//1. JSON의 형태로 요청 성공 여부 및 메세지를 전달함.
		try {
			JSONObject json = new JSONObject();
			json.put("flag", false);
			json.put("content", message);
			
			response.setStatus(errorcode);
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			response.getWriter().print(json);
		//2. 기타 예외가 발생한 경우.
		} catch (IOException e) {
			
		}
	}
	
	@Override
	//액세스 토큰이 필요한 기능을 호출하면, 해당 액세스 토큰이 유효성 여부를 판단하여 해당 요청을 컨트롤러로 전송할지 여부를 결정함.
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
		
		//1. 신규 회원가입 요청시에는 액세스 토큰이 필요없음.
		if(uri.equals("/api/users")&&method.equalsIgnoreCase("POST")){
			System.out.println("1");
			return true;
		//2. 공개키 발급 또는 도서 장르 조회는 액세스 토큰이 필요없음.
		}else if((/*uri.equals("/api/users/publickeys")||*/uri.equals("/api/books/book_types"))&&method.equalsIgnoreCase("GET")) {
			System.out.println("2");
			return true;
		//3. 비밀번호 찾기 질문 조회는 액세스 토큰이 필요없음.
		}else if(uri.equals("/api/users/questions")&&method.equalsIgnoreCase("GET")) {
			System.out.println("3");
			return true;
		}

		//3. 액세스 토큰이 필요하나 액세스 토큰이 없는 경우에는, UNAUTHORIZED 응답.
		if(user_accesstoken==null) {
			setErrorMessage(response,401,"액세스 토큰이 존재하지 않습니다.");
			System.out.println("4");
			return false;
		//4. 액세스 토큰이 필요하고, 액세스 토큰이 있는 경우에는 아래의 처리로직을 수행함.
		}else {
			//5. Redis의 블랙리스트에 존재하는 액세스 토큰일 경우에는 정상적으로 로그아웃 처리된 토큰이므로 UNAUTHORIZED 응답.
			if(redisUtil.getData(user_accesstoken)!=null) {
				setErrorMessage(response,400,"해당 액세스 토큰은 이미 로그아웃 처리되었습니다.");
				System.out.println("5");
				return false;
			}
			
			//6. 액세스 토큰이 위조되지 않았고, 아직 유효기간이 남아있는 경우 컨트롤러로 해당 요청을 전달함.
			try {
				jwtUtil.validateToken(user_accesstoken);
				System.out.println("6");
				return true;
			//7. 액세스 토큰의 유효기간이 지난 경우 UNAUTHORIZED 응답.
			}catch(ExpiredJwtException e) {
				setErrorMessage(response,401,"해당 액세스 토큰은 기한이 만료되었습니다.");
				System.out.println("7");
				return false;
			//8. 액세스 토큰이 위조된 경우 UNAUTHORIZED 응답.
			}catch(Exception e) {
				e.printStackTrace();
				setErrorMessage(response,401,"해당 액세스 토큰은 위조되었습니다.");
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