package com.spring.api.service;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.spring.api.dao.TokenDAO;
import com.spring.api.encrypt.RSA2048;
import com.spring.api.encrypt.SHA;
import com.spring.api.exception.users.InvalidPublicKeyException;
import com.spring.api.exception.users.NotFoundUserException;
import com.spring.api.exception.users.UserIdNotMatchedToRegexException;
import com.spring.api.exception.users.UserPwNotMatchedException;
import com.spring.api.exception.users.UserPwNotMatchedToRegexException;
import com.spring.api.util.JwtUtil;
import com.spring.api.util.RedisUtil;
import com.spring.api.util.RegexUtil;
import com.spring.api.vo.UserVO;

@Transactional(rollbackFor= {
		InvalidPublicKeyException.class,
		NotFoundUserException.class,
		UserIdNotMatchedToRegexException.class,
		UserPwNotMatchedToRegexException.class,
		UserPwNotMatchedException.class,
		Exception.class
})
@Service("tokenService")
public class TokenServiceImpl implements TokenService{
	@Autowired
	private TokenDAO tokenDAO;

	public HashMap createNewTokens(HashMap<String,String> param) throws 
	
	InvalidPublicKeyException, 
	NotFoundUserException, 
	UserIdNotMatchedToRegexException, 
	UserPwNotMatchedToRegexException, 
	UserPwNotMatchedException, 
	Exception {
		
		String user_id = param.get("user_id");
		String user_pw = param.get("user_pw");
		String user_publickey = param.get("user_publickey");
		String user_privatekey = null;
		
		//����Ű�� ��ȿ���� ������ ����Ű ��ȿ�� ������ ���� �߻�
		if((user_privatekey = (String) RedisUtil.getData(user_publickey))==null) {
			throw new InvalidPublicKeyException();
		}
		
		user_pw = RSA2048.decrypt(user_pw, user_privatekey);
		HashMap<String,Object> user = null;
		
		//1. ���̵� ���ԽĿ� �����ϴٸ� ������ �����ϴ� ���̵����� Ȯ��, ������ ����
		if(RegexUtil.checkRegex(user_id, RegexUtil.USER_ID_REGEX)) {
			user = tokenDAO.getUserInfoByUserId(param);
			if(user==null) {
				throw new NotFoundUserException();
			}
		}else {
			throw new UserIdNotMatchedToRegexException();
		}
		
		//2. ��й�ȣ�� ���ԽĿ� �������� �Ǵ�, �����ϴٸ� ��й�ȣ�� �ùٸ��� Ȯ��, �ƴϸ� ����
		if(!RegexUtil.checkRegex(user_pw, RegexUtil.USER_PW_REGEX)) {
			throw new UserPwNotMatchedToRegexException();
		}else {
			user_pw = SHA.DSHA512(user_pw, (String)user.get("user_salt"));
			
			if(!user_pw.equals(user.get("user_pw"))) {
				throw new UserPwNotMatchedException();
			}
		}

		//3. �����ϴ� ���̵�, �ùٸ� ��й�ȣ��� ��ū�� ������ ��ȯ
		UserVO userVo = new UserVO();
		userVo.setUser_id((String)user.get("user_id"));
		userVo.setUser_type_id((Integer)user.get("user_type_id"));
		userVo.setUser_type_content((String)user.get("user_type_content"));
		
		String user_accesstoken = JwtUtil.createToken(userVo, JwtUtil.ACCESSTOKEN_MAXAGE);
		String user_refreshtoken = JwtUtil.createToken(userVo, JwtUtil.REFRESHTOKEN_MAXAGE);
		
		//4. �ش���ū�� ���� ������� ��ū���� ������Ʈ
		param = new HashMap();
		param.put("user_id", user_id);
		param.put("user_accesstoken", user_accesstoken);
		param.put("user_refreshtoken", user_refreshtoken);
		
		int row = tokenDAO.updateUserTokens(param);
		
		if(row==0) {
			throw new Exception();
		}
		
		HashMap result = new HashMap();
		result.put("user_accesstoken", user_accesstoken);
		result.put("user_refreshtoken", user_refreshtoken);
		
		return result;		
	}

	public HashMap updateTokens(HttpServletRequest request) {
		//1. �ش� ������� �׼���, �������� ��ū�� ����.
		String user_accesstoken = JwtUtil.getAccesstoken(request);
		String user_refreshtoken = JwtUtil.getRefreshtoken(request);

		//2. �ش� ������� ID�� Ȱ���� ���ο� �׼���, �������� ��ū�� �߱���.
		String user_id = (String)JwtUtil.getData(user_accesstoken, "user_id");
		UserVO user = new UserVO();
		user.setUser_id(user_id);

		String new_user_accesstoken = JwtUtil.createToken(user, JwtUtil.ACCESSTOKEN_MAXAGE);
		String new_user_refreshtoken = JwtUtil.createToken(user, JwtUtil.REFRESHTOKEN_MAXAGE);
				
		//3. �ش� ������� DB������ ���ο� �׼���, �������� ��ū���� ������. 
		HashMap param = new HashMap();
		param.put("user_id", user_id);
		param.put("user_accesstoken", new_user_accesstoken);
		param.put("user_refreshtoken", new_user_refreshtoken);
				
		tokenDAO.updateUserTokens(param);
				
		//4. ���� �߱��� �׼���, �������� ��ū�� ��Ű�� ��� Ŭ���̾�Ʈ�� ������.
		HashMap result = new HashMap();
		result.put("user_accesstoken", new_user_accesstoken);
		result.put("user_refreshtoken", new_user_refreshtoken);
				
		//5. ������ �߱޹޾Ҵ� �׼���, �������� ��ū�� Redis�� �����Ͽ� �α׾ƿ� ó����.
		RedisUtil.setData(user_accesstoken, "removed", JwtUtil.getExpiration(user_accesstoken));
		RedisUtil.setData(user_refreshtoken, "removed", JwtUtil.getExpiration(user_refreshtoken));
		
		return result;
	}

	public void deleteTokens(HttpServletRequest request) throws NotFoundUserException, Exception{
		String user_accesstoken = JwtUtil.getAccesstoken(request);
		String user_refreshtoken = JwtUtil.getRefreshtoken(request);
		
		String user_id = (String) JwtUtil.getData(user_accesstoken, "user_id");
		
		HashMap param = new HashMap();
		param.put("user_id", user_id);
		
		HashMap user = tokenDAO.getUserInfoByUserId(param);
		
		if(user==null) {
			throw new NotFoundUserException();
		}
		
		int row = tokenDAO.deleteUserTokens(param);
		
		if(row==0) {
			throw new Exception();
		}
		
		long user_accesstoken_exp = JwtUtil.getExpiration(user_accesstoken);
		long user_refreshtoken_exp = JwtUtil.getExpiration(user_refreshtoken);
		
		RedisUtil.setData(user_accesstoken, "removed", user_accesstoken_exp);
		RedisUtil.setData(user_refreshtoken, "removed", user_refreshtoken_exp);
	}
}