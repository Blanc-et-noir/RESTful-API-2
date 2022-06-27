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
	private TokenDAO tokenDAO;
	
	//리프레쉬 토큰이 필요한 기능을 요청할때, 해당 리프레쉬 토큰이 유효하지 않은경우 에러 메세지를 전달함.
	private static void setErrorMessage(HttpServletResponse response, int errorcode, String message){
		//1. JSON의 형태로 요청 성공 여부 및 메세지를 전달함.
		try {
			JSONObject json = new JSONObject();
			json.put("flag", false);
			json.put("content", message);
			
			response.setStatus(401);
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			response.getWriter().write(json.toString());
		//2. 기타 예외가 발생한 경우.
		} catch (IOException e) {
			
		}
	}
	
	@Override
	//리프레쉬 토큰이 필요한 기능을 호출하면, 해당 리프레쉬 토큰이 유효성 여부를 판단하여 해당 요청을 컨트롤러로 전송할지 여부를 결정함.
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {	
		String user_accesstoken = JwtUtil.getAccesstoken(request);
		String user_refreshtoken = JwtUtil.getRefreshtoken(request);
		String uri = request.getRequestURI();
		String method = request.getMethod();
		
		if(method.equalsIgnoreCase("OPTIONS")) {
			return true;
		}
		
		if(uri.endsWith("/")) {
			uri = uri.substring(0,uri.length()-1);
		}
		
		//1. 로그인을 시도하여 액세스, 리프레쉬 토큰을 새로 발급받으려 요청하는 경우에는 리프레쉬 토큰이 필요없음.
		if(uri.equals("/api/tokens")&&method.equals("POST")) {
			return true;
		}
		
		//2. 리프레쉬 토큰이 필요하나 리프레쉬 토큰이 없는 경우에는, UNAUTHORIZED 응답.
		if(user_refreshtoken == null) {
			setErrorMessage(response,401,"리프레쉬 토큰이 존재하지 않습니다.");
			return false;
		//3. 액세스 토큰이 필요하나 액세스 토큰이 없는 경우에는, UNAUTHORIZED 응답.
		}else if(user_accesstoken==null) {
			setErrorMessage(response,401,"액세스 토큰이 존재하지 않습니다.");
			return false;
		}else {
			//4. Redis의 블랙리스트에 존재하는 리프레쉬 토큰일 경우에는 정상적으로 로그아웃 처리된 토큰이므로 UNAUTHORIZED 응답.
			if(RedisUtil.getData(user_refreshtoken)!=null) {
				setErrorMessage(response,401,"해당 리프레쉬 토큰은 이미 로그아웃 처리되었습니다.");
				return false;
			}
			
			//5. Redis의 블랙리스트에 존재하는 액세스 토큰일 경우에는 정상적으로 로그아웃 처리된 토큰이므로 UNAUTHORIZED 응답.
			if(RedisUtil.getData(user_accesstoken)!=null) {
				setErrorMessage(response,401,"해당 액세스 토큰은 이미 로그아웃 처리되었습니다.");
				return false;
			}
			
			//6. 액세스 토큰이 위조되지 않았고, 아직 유효기간이 남아있는 경우 리프레쉬 토큰의 유효성을 검사함.
			try {
				JwtUtil.validateToken(user_accesstoken);
			}catch(ExpiredJwtException e) {

			//7. 액세스 토큰이 위조된 경우 UNAUTHORIZED 응답.
			}catch(Exception e) {
				setErrorMessage(response,401,"해당 액세스 토큰은 위조되었습니다.");
				return false;
			}
			
			//8. 리프레쉬 토큰이 위조되지 않았고, 아직 유효기간이 남아있는 경우 컨트롤러로 해당 요청을 전달함.
			try {
				JwtUtil.validateToken(user_refreshtoken);
				
				//9. 액세스 토큰과 리프레쉬 토큰의 주인이 일치하는지 확인함.
				String access_user_id = (String)JwtUtil.getData(user_accesstoken, "user_id");
				String refresh_user_id = (String)JwtUtil.getData(user_refreshtoken, "user_id");

				if(!access_user_id.equals(refresh_user_id)) {
					setErrorMessage(response,401,"액세스 토큰 및 리프레쉬 토큰 발급자 정보가 일치하지 않습니다.");
					return false;
				}
				
				//10. 전달받은 액세스, 리프레쉬 토큰이 현재 사용자가 사용중인 액세스, 리프레쉬 토큰인지 확인함.
				HashMap param = new HashMap();
				param.put("user_id", access_user_id);
				HashMap tokens = tokenDAO.getUserTokensByUserId(param);
				
				//11. 해당 사용자가 사용하던 액세스, 리프레쉬 토큰이 아닌경우 UNAUTHORIZED 응답.
				if(!user_accesstoken.equals(tokens.get("user_accesstoken"))||!user_refreshtoken.equals(tokens.get("user_refreshtoken"))) {
					setErrorMessage(response,401,"액세스 토큰 또는 리프레쉬 토큰은 해당 사용자가 현재 사용중인 것이 아닙니다.");
					return false;
				}
				return true;
			//12. 리프레쉬 토큰의 유효기간이 지난 경우 UNAUTHORIZED 응답.
			}catch(ExpiredJwtException e) {
				setErrorMessage(response,401,"해당 리프레쉬 토큰은 기한이 만료되었습니다.");
				return false;
			//13. 리프레쉬 토큰이 위조된 경우 UNAUTHORIZED 응답.
			}catch(Exception e) {
				setErrorMessage(response,401,"해당 리프레쉬 토큰은 위조되었습니다.");
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