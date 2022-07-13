package com.spring.api.service;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.spring.api.code.ErrorCode;
import com.spring.api.code.PointCode;
import com.spring.api.dao.TokenDAO;
import com.spring.api.dao.UserDAO;
import com.spring.api.encrypt.RSA2048;
import com.spring.api.encrypt.SHA;
import com.spring.api.exception.CustomException;
import com.spring.api.util.JwtUtil;
import com.spring.api.util.RedisUtil;
import com.spring.api.util.RegexUtil;
import com.spring.api.vo.UserVO;

@Transactional(rollbackFor= {
		CustomException.class,
		RuntimeException.class,
		Exception.class
})
@Service("tokenService")
public class TokenServiceImpl implements TokenService{
	@Autowired
	private JwtUtil jwtUtil;
	@Autowired
	private RedisUtil redisUtil;
	@Autowired
	private TokenDAO tokenDAO;
	@Autowired
	private UserDAO userDAO;
	
	public HashMap createNewTokens(HashMap param) {
		
		String user_id = (String) param.get("user_id");
		String user_pw = (String) param.get("user_pw");
		//String user_publickey = (String) param.get("user_publickey");
		//String user_privatekey = null;
		
		//공개키가 유효하지 않으면 공개키 유효성 불충족 예외 발생
		//if((user_privatekey = (String) redisUtil.getData(user_publickey))==null) {
			//throw new CustomException(ErrorCode.INVALID_PUBLICKEY);
		//}
		
		//user_pw = RSA2048.decrypt(user_pw, user_privatekey);
		HashMap<String,Object> user = null;
		
		//1. 아이디가 정규식에 적합하다면 실제로 존재하는 아이디인지 확인, 없으면 예외
		if(RegexUtil.checkRegex(user_id, RegexUtil.USER_ID_REGEX)) {
			user = tokenDAO.getUserInfoByUserId(param);
			if(user==null) {
				throw new CustomException(ErrorCode.NOT_FOUND_USER);
			}
		}else {
			throw new CustomException(ErrorCode.USER_ID_NOT_MATCHED_TO_REGEX);
		}
		
		//2. 비밀번호가 정규식에 적합한지 판단, 적합하다면 비밀번호가 올바른지 확인, 아니면 예외
		if(!RegexUtil.checkRegex(user_pw, RegexUtil.USER_PW_REGEX)) {
			throw new CustomException(ErrorCode.USER_PW_NOT_MATCHED_TO_REGEX);
		}else {
			user_pw = SHA.DSHA512(user_pw, (String)user.get("user_salt"));
			
			if(!user_pw.equals(user.get("user_pw"))) {
				throw new CustomException(ErrorCode.USER_PW_NOT_MATCHED);
			}
		}

		//3. 존재하는 아이디, 올바른 비밀번호라면 토큰을 생성후 반환
		UserVO userVo = new UserVO();
		userVo.setUser_id((String)user.get("user_id"));
		userVo.setUser_type_id((Integer)user.get("user_type_id"));
		userVo.setUser_type_content((String)user.get("user_type_content"));
		
		String user_accesstoken = jwtUtil.createToken(userVo, jwtUtil.ACCESSTOKEN_MAXAGE);
		String user_refreshtoken = jwtUtil.createToken(userVo, jwtUtil.REFRESHTOKEN_MAXAGE);
		
		//4. 기존에 DB에 저장된 현재 사용중인 토큰을 로그아웃 처리함.
		HashMap<String,String> tokens = tokenDAO.getUserTokensByUserId(param);
		String old_user_accesstoken = tokens.get("user_accesstoken");
		String old_user_refreshtoken = tokens.get("user_refreshtoken");
		
		if(old_user_accesstoken!=null) {
			redisUtil.setData(old_user_accesstoken, "removed", jwtUtil.getExpiration(old_user_accesstoken));
		}
		
		if(old_user_refreshtoken!=null) {
			redisUtil.setData(old_user_refreshtoken, "removed", jwtUtil.getExpiration(old_user_refreshtoken));
		}
		
		//5. 해당토큰을 현재 사용중인 토큰으로 업데이트함.
		param = new HashMap();
		param.put("user_id", user_id);
		param.put("user_accesstoken", user_accesstoken);
		param.put("user_refreshtoken", user_refreshtoken);
		
		if(tokenDAO.updateUserTokens(param)!=1) {
			throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
		}
				
		HashMap result = new HashMap();
		result.put("user_accesstoken", user_accesstoken);
		result.put("user_refreshtoken", user_refreshtoken);
		
		return result;		
	}

	public HashMap updateTokens(HttpServletRequest request) {
		//1. 해당 사용자의 액세스, 리프레쉬 토큰을 얻음.
		String user_accesstoken = jwtUtil.getAccesstoken(request);
		String user_refreshtoken = jwtUtil.getRefreshtoken(request);

		//2. 해당 사용자의 ID를 활용해 새로운 액세스, 리프레쉬 토큰을 발급함.
		String user_id = (String)jwtUtil.getData(user_accesstoken, "user_id");
		UserVO user = new UserVO();
		user.setUser_id(user_id);

		String new_user_accesstoken = jwtUtil.createToken(user, jwtUtil.ACCESSTOKEN_MAXAGE);
		String new_user_refreshtoken = jwtUtil.createToken(user, jwtUtil.REFRESHTOKEN_MAXAGE);
				
		//3. 해당 사용자의 DB정보를 새로운 액세스, 리프레쉬 토큰으로 갱신함. 
		HashMap param = new HashMap();
		param.put("user_id", user_id);
		param.put("user_accesstoken", new_user_accesstoken);
		param.put("user_refreshtoken", new_user_refreshtoken);
				
		tokenDAO.updateUserTokens(param);
				
		//4. 새로 발급한 액세스, 리프레쉬 토큰을 쿠키에 담아 클라이언트로 전달함.
		HashMap result = new HashMap();
		result.put("user_accesstoken", new_user_accesstoken);
		result.put("user_refreshtoken", new_user_refreshtoken);
				
		//5. 기존에 발급받았던 액세스, 리프레쉬 토큰은 Redis에 저장하여 로그아웃 처리함.
		redisUtil.setData(user_accesstoken, "removed", jwtUtil.getExpiration(user_accesstoken));
		redisUtil.setData(user_refreshtoken, "removed", jwtUtil.getExpiration(user_refreshtoken));
		
		return result;
	}

	public void deleteTokens(HttpServletRequest request){
		String user_accesstoken = jwtUtil.getAccesstoken(request);
		String user_refreshtoken = jwtUtil.getRefreshtoken(request);
		
		String user_id = (String) jwtUtil.getData(user_accesstoken, "user_id");
		
		HashMap param = new HashMap();
		param.put("user_id", user_id);
		
		HashMap user = tokenDAO.getUserInfoByUserId(param);
		
		if(user==null) {
			throw new CustomException(ErrorCode.NOT_FOUND_USER);
		}
		
		int row = tokenDAO.deleteUserTokens(param);
		
		if(row==0) {
			throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
		}
		
		long user_accesstoken_exp = jwtUtil.getExpiration(user_accesstoken);
		long user_refreshtoken_exp = jwtUtil.getExpiration(user_refreshtoken);
		
		redisUtil.setData(user_accesstoken, "removed", user_accesstoken_exp);
		redisUtil.setData(user_refreshtoken, "removed", user_refreshtoken_exp);
	}
}