package com.spring.api.service;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.spring.api.dao.UserDAO;
import com.spring.api.encrypt.RSA2048;
import com.spring.api.encrypt.SHA;
import com.spring.api.exception.users.DuplicateUserIdException;
import com.spring.api.exception.users.DuplicateUserPhoneException;
import com.spring.api.exception.users.InvalidPublicKeyException;
import com.spring.api.exception.users.NotAuthorizedException;
import com.spring.api.exception.users.NotFoundUserException;
import com.spring.api.exception.users.QuestionAnswerExceededLimitOnMaxbytesException;
import com.spring.api.exception.users.UUIDNotMatchedToRegexException;
import com.spring.api.exception.users.UserIdNotMatchedToRegexException;
import com.spring.api.exception.users.UserNameNotMatchedToRegexException;
import com.spring.api.exception.users.UserPhoneNotMatchedToRegexException;
import com.spring.api.exception.users.UserPwNotMatchedToRegexException;
import com.spring.api.util.JwtUtil;
import com.spring.api.util.RedisUtil;
import com.spring.api.util.RegexUtil;

@Transactional(rollbackFor= {
		InvalidPublicKeyException.class,
		UserIdNotMatchedToRegexException.class,
		DuplicateUserIdException.class,
		UserPwNotMatchedToRegexException.class,
		UserNameNotMatchedToRegexException.class,
		UserPhoneNotMatchedToRegexException.class,
		DuplicateUserPhoneException.class,
		UUIDNotMatchedToRegexException.class,
		QuestionAnswerExceededLimitOnMaxbytesException.class,
		NotFoundUserException.class,
		Exception.class
	}
)
@Service("userService")
public class UserServiceImpl implements UserService{
	@Autowired
	private UserDAO userDAO;
	
	private final static int PRIVATEKEY_MAXAGE = 180*1000;
	
	@Override
	public String createNewUserKeys() throws Exception {
		HashMap<String,String> keyPair = RSA2048.createKeys();
		
		String user_publickey = keyPair.get("user_publickey");
		String user_privatekey = keyPair.get("user_privatekey");

		RedisUtil.setData(user_publickey, user_privatekey,PRIVATEKEY_MAXAGE);
		
		return user_publickey;
	}
	
	@Override
	public void createNewUserInfo(HashMap<String,String> param) throws
	
		InvalidPublicKeyException,
		UserIdNotMatchedToRegexException,
		DuplicateUserIdException,
		UserPwNotMatchedToRegexException,
		UserNameNotMatchedToRegexException,
		UserPhoneNotMatchedToRegexException,
		DuplicateUserPhoneException,
		UUIDNotMatchedToRegexException,
		QuestionAnswerExceededLimitOnMaxbytesException,
		Exception
	
	{
		String user_id = param.get("user_id");
		String user_pw = param.get("user_pw");
		String user_name = param.get("user_name");
		String user_publickey = param.get("user_publickey");
		String user_privatekey = null;
		String user_phone = param.get("user_phone");
		String question_id = param.get("question_id");
		String question_answer = param.get("question_answer");

		//공개키가 유효하지 않으면 공개키 유효성 불충족 예외 발생
		if((user_privatekey = (String) RedisUtil.getData(user_publickey))==null) {
			throw new InvalidPublicKeyException();
		}
		
		user_pw = RSA2048.decrypt(user_pw, user_privatekey);		
		question_answer = RSA2048.decrypt(question_answer, user_privatekey).replaceAll(" ", "");

		//아이디가 비어있거나, 정규식을 만족하지 않으면 예외 발생
		if(!RegexUtil.checkRegex(user_id,RegexUtil.USER_ID_REGEX)) {
			throw new UserIdNotMatchedToRegexException();			
		//비밀번호가 비어있거나, 정규식을 만족하지 않으면 예외 발생
		}else {
			param = new HashMap();
			param.put("user_id", user_id);
			
			HashMap user = userDAO.findUserInfoByUserId(param);
			
			if(user!=null) {
				throw new DuplicateUserIdException();
			}
		}
		
		if(!RegexUtil.checkRegex(user_pw,RegexUtil.USER_PW_REGEX)) {
			throw new UserPwNotMatchedToRegexException();
		}
		
		if(!RegexUtil.checkRegex(user_name,RegexUtil.USER_NAME_REGEX)){
			throw new UserNameNotMatchedToRegexException();
		}
		
		if(!RegexUtil.checkRegex(user_phone,RegexUtil.USER_PHONE_REGEX)) {
			throw new UserPhoneNotMatchedToRegexException();
		}else {
			param = new HashMap();
			param.put("user_phone", user_phone);
			
			HashMap user = userDAO.findUserInfoByUserPhone(param);
			
			if(user!=null) {
				throw new DuplicateUserPhoneException();
			}
		}
		
		if(!RegexUtil.checkRegex(question_id,RegexUtil.UUID_REGEX)) {
			throw new UUIDNotMatchedToRegexException();
		}
		
		if(!RegexUtil.checkBytes(question_answer,RegexUtil.QUESTION_ANSWER_MAXBYTES)) {
			throw new QuestionAnswerExceededLimitOnMaxbytesException();
		}
		
		//비밀번호, 비밀번호 찾기 질문의 답은 SALT값과 SHA512해시함수로 두 번 해싱하여 저장
		String user_salt = SHA.getSalt();
		user_pw = SHA.DSHA512(user_pw, user_salt);
		question_answer = SHA.DSHA512(question_answer, user_salt);
		
		param = new HashMap();
		param.put("user_id", user_id);
		param.put("user_pw", user_pw);
		param.put("user_name", user_name);
		param.put("user_phone", user_phone);
		param.put("question_id", question_id);
		param.put("question_answer", question_answer);
		param.put("user_salt", user_salt);
		
		int row = userDAO.createNewUserInfo(param);
		
		if(row==0) {
			throw new Exception();
		}else {
			return;
		}
	}

	@Override
	public HashMap readUserInfo(HttpServletRequest request, String target_user_id) throws NotFoundUserException,NotAuthorizedException, Exception {
		String user_accesstoken = JwtUtil.getAccesstoken(request);
		String jwt_user_id = (String)JwtUtil.getData(user_accesstoken, "user_id");
		
		HashMap param = new HashMap();
		param.put("user_id", target_user_id);
		
		HashMap user = userDAO.findUserInfoByUserId(param);
		
		if(user==null) {
			throw new NotFoundUserException();
		}
		
		int user_type_id = (Integer)JwtUtil.getData(user_accesstoken, "user_type_id");
		
		if(user_type_id!=0&&!target_user_id.equals(jwt_user_id)) {
			throw new NotAuthorizedException();
		}
		
		param = new HashMap();
		param.put("user_id", target_user_id);
		
		user = userDAO.readUserInfo(param);
		
		if(user == null) {
			throw new NotFoundUserException();
		}
		
		return user;
	}

	@Override
	public void updateUserInfo(HashMap<String,String> param) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteUserInfo(HashMap<String,String> param) {
		// TODO Auto-generated method stub
		
	}
}