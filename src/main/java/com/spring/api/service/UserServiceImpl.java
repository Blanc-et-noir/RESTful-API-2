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
import com.spring.api.exception.users.NotchangeableUserSaltException;
import com.spring.api.exception.users.QuestionAnswerExceededLimitOnMaxbytesException;
import com.spring.api.exception.users.UUIDNotMatchedToRegexException;
import com.spring.api.exception.users.UserIdNotMatchedToRegexException;
import com.spring.api.exception.users.UserNameNotMatchedToRegexException;
import com.spring.api.exception.users.UserPhoneNotMatchedToRegexException;
import com.spring.api.exception.users.UserPwNotMatchedToRegexException;
import com.spring.api.exception.users.UserQuestionAnswerNotMatchedException;
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
	
	//공개키 및 비밀키를 생성하고, 공개키는 클라이언트에게 반환, 비밀키는 Redis에 일정시간동안 저장하는 로직.
	@Override
	public String createNewUserKeys() throws Exception {
		HashMap<String,String> keyPair = RSA2048.createKeys();
		
		String user_publickey = keyPair.get("user_publickey");
		String user_privatekey = keyPair.get("user_privatekey");

		RedisUtil.setData(user_publickey, user_privatekey,RedisUtil.PRIVATEKEY_MAXAGE);
		
		return user_publickey;
	}
	
	//회원가입 요청을 처리하는 로직.
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

		//1. 공개키가 유효하지 않으면 공개키 유효성 불충족 예외가 발생함.
		//   비밀키가 Redis에 저장될 수 있는 시간이 지났거나, 공개키 자체가 유효하지 않을때 발생함.
		if((user_privatekey = (String) RedisUtil.getData(user_publickey))==null) {
			throw new InvalidPublicKeyException();
		}

		//2. ID가 전달되지 않았거나, 정규식을 만족하지 않으면 예외가 발생함.
		if(!RegexUtil.checkRegex(user_id,RegexUtil.USER_ID_REGEX)) {
			throw new UserIdNotMatchedToRegexException();
		//3. 해당 ID로 이미 가입한 사용자 정보가 있다면 예외가 발생함. 
		}else {
			param = new HashMap();
			param.put("user_id", user_id);
			
			HashMap user = userDAO.findUserInfoByUserId(param);
			
			if(user!=null) {
				throw new DuplicateUserIdException();
			}
		}
		
		//4. 비밀번호가 정규식에 부합하지않으면 예외가 발생함.
		user_pw = RSA2048.decrypt(user_pw, user_privatekey);
		if(!RegexUtil.checkRegex(user_pw,RegexUtil.USER_PW_REGEX)) {
			throw new UserPwNotMatchedToRegexException();
		}
		
		//5. 사용자 이름이 정규식에 부합하지 않으면 예외가 발생함.
		if(!RegexUtil.checkRegex(user_name,RegexUtil.USER_NAME_REGEX)){
			throw new UserNameNotMatchedToRegexException();
		}
		
		//6. 사용자 전화번호가 정규식에 부합하지 않으면 예외가 발생함.
		if(!RegexUtil.checkRegex(user_phone,RegexUtil.USER_PHONE_REGEX)) {
			throw new UserPhoneNotMatchedToRegexException();
		//7. 사용자 전화번호로 회원가입한 사용자 정보가 이미 존재한다면 예외가 발생함.
		}else {
			param = new HashMap();
			param.put("user_phone", user_phone);
			
			HashMap user = userDAO.findUserInfoByUserPhone(param);
			
			if(user!=null) {
				throw new DuplicateUserPhoneException();
			}
		}
		
		//8. UUID가 정규식에 부합하지 않으면 예외가 발생함.
		if(!RegexUtil.checkRegex(question_id,RegexUtil.UUID_REGEX)) {
			throw new UUIDNotMatchedToRegexException();
		}
		
		//9. 비밀번호 찾기 질문에 대한 답이 특정 바이트이상의 크기를 갖는다면 예외가 발생함. 
		question_answer = RSA2048.decrypt(question_answer, user_privatekey).replaceAll(" ", "");
		if(!RegexUtil.checkBytes(question_answer,RegexUtil.QUESTION_ANSWER_MAXBYTES)) {
			throw new QuestionAnswerExceededLimitOnMaxbytesException();
		}
		
		//10. 비밀번호, 비밀번호 찾기 질문의 답은 무작위 SALT값과 SHA512해시함수로 두 번 해싱하여 저장함.
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
		
		//11. 해당 회원정보로 회원가입을 실제로 진행함.
		int row = userDAO.createNewUserInfo(param);
		
		if(row==0) {
			throw new Exception();
		}else {
			return;
		}
	}

	//회원정보를 조회하는 로직, 관리자는 모두에 대한 정보, 일반 사용자는 자기 자신만의 정보를 조회할 수 있음.
	@Override
	public HashMap readUserInfo(HttpServletRequest request, String target_user_id) throws 
	NotFoundUserException,
	NotAuthorizedException,
	Exception 
	{
		//1. 전달받은 토큰으로 해당 사용자에 대한 회원 정보조회가 가능한지 여부를 확인함.
		String user_accesstoken = JwtUtil.getAccesstoken(request);
		String jwt_user_id = (String)JwtUtil.getData(user_accesstoken, "user_id");

		int user_type_id = (Integer)JwtUtil.getData(user_accesstoken, "user_type_id");
		
		if(user_type_id!=0&&!target_user_id.equals(jwt_user_id)) {
			throw new NotAuthorizedException();
		}
		
		//2. 조회가 가능하므로 실제로 해당 사용자에 대한 정보를 조회함.
		HashMap param = new HashMap();
		param.put("user_id", target_user_id);
		
		HashMap user = userDAO.readUserInfo(param);
		
		//3. 해당 사용자 정보가 존재하지 않으면 예외처리함.
		if(user==null) {
			throw new NotFoundUserException();
		}
		
		//4. 불필요하거나, 민감한 정보는 조회대상에서 제외함.
		user.remove("question_answer");
		user.remove("question_id");
		user.remove("user_refreshtoken");
		user.remove("user_accesstoken");
		user.remove("user_pw");
		user.remove("user_salt");
		
		return user;
	}

	//회원정보를 변경하는 로직, 자기자신만의 회원정보만 변경이 가능함.
	//변경가능한 정보는 이름, 전화번호, 비밀번호, 비밀번호 찾기 질문, 비밀번호 찾기 질문에 대한 답만 수정 가능함.
	@Override
	public void updateUserInfo(HttpServletRequest request, HashMap<String,String> param) throws
		NotchangeableUserSaltException,
		InvalidPublicKeyException,
		NotFoundUserException,
		NotAuthorizedException,
		UserQuestionAnswerNotMatchedException,
		UserNameNotMatchedToRegexException,
		UserPwNotMatchedToRegexException,
		UUIDNotMatchedToRegexException,
		QuestionAnswerExceededLimitOnMaxbytesException,
		DuplicateUserPhoneException,
		UserPhoneNotMatchedToRegexException,
		Exception
	{
		//1. 해당 회원정보가 DB에 실제로 존재하는지 확인함.
		String user_accesstoken = JwtUtil.getAccesstoken(request);
		HashMap user = userDAO.readUserInfo(param);
		
		if(user==null) {
			throw new NotFoundUserException();
		}
		
		//2. 회원정보를 변경할 권한이 있는지 판단함.
		String target_user_id = param.get("user_id");
		String jwt_user_id = (String) JwtUtil.getData(user_accesstoken, "user_id");
		
		if(!jwt_user_id.equals(target_user_id)) {
			throw new NotAuthorizedException();
		}
		
		//3. 비밀번호 변경 정답이 일치하는지 확인함.
		String user_salt = (String) user.get("user_salt");
		String question_answer = (String) param.get("question_answer");
		String user_publickey = (String) param.get("user_publickey");
		String user_privatekey = (String) RedisUtil.getData(user_publickey);
		
		if(user_privatekey==null) {
			throw new InvalidPublicKeyException();
		}
		
		question_answer = SHA.DSHA512(RSA2048.decrypt(question_answer, user_privatekey).replaceAll(" ", ""),user_salt);
		
		if(!question_answer.equals((String)user.get("question_answer"))){
			throw new UserQuestionAnswerNotMatchedException();
		}
		
		//4. 변경할 회원 정보들에 대한 유효성을 검사함.
		//   이 부분에서는 이름이 정규식에 부합하는지 검사함.
		String new_user_name = (String) param.get("new_user_name");
		
		if(new_user_name!=null) {
			if(!RegexUtil.checkRegex(new_user_name, RegexUtil.USER_NAME_REGEX)) {
				throw new UserNameNotMatchedToRegexException();
			}
		}
		
		//5. 비밀번호 변경시에는 정규식에 부합해야함.
		boolean flag1=false, flag2=false, flag3=false;
		String new_user_salt = SHA.getSalt();
		String new_user_pw = (String) param.get("new_user_pw");
		
		if(new_user_pw!=null) {
			new_user_pw = RSA2048.decrypt(new_user_pw, user_privatekey);
			if(RegexUtil.checkRegex(new_user_pw, RegexUtil.USER_PW_REGEX)) {
				new_user_pw = SHA.DSHA512(new_user_pw, new_user_salt);
				param.put("new_user_salt", new_user_salt);
				param.put("new_user_pw", new_user_pw);
				flag1=true;
			}else {
				throw new UserPwNotMatchedToRegexException();
			}
		}
		
		//6. 비밀번호 찾기 질문변경시에는 UUID가 정규식에 부합해야함.
		String new_question_id = (String) param.get("new_question_id");
		if(new_question_id!=null) {
			if(!RegexUtil.checkRegex(new_question_id, RegexUtil.UUID_REGEX)) {
				throw new UUIDNotMatchedToRegexException();
			}else {
				flag2=true;
			}
		}
		
		
		//7. 비밀번호 찾기 질문의 정답 변경시에는 해당 답이 특정 바이트 이하의 크기여야함.
		String new_user_question_answer = (String) param.get("new_user_question_answer");
		if(new_user_question_answer!=null) {
			new_user_question_answer = RSA2048.decrypt(new_user_question_answer, user_privatekey).replaceAll(" ", "");
			if(RegexUtil.checkBytes(new_user_question_answer, RegexUtil.QUESTION_ANSWER_MAXBYTES)) {
				new_user_question_answer = SHA.DSHA512(new_user_question_answer, new_user_salt);
				param.put("new_user_salt", new_user_salt);
				param.put("new_user_question_answer", new_user_question_answer);
				flag3=true;
			}else {
				throw new QuestionAnswerExceededLimitOnMaxbytesException();
			}
		}
		
		//8. 비밀번호, 비밀번호 찾기 질문, 비밀번호 찾기 질문에 대한 답중 어느 하나라도 수정되면
		//   나머지 모두 동시에 수정되어야만, user_salt를 변경할 수 있음.
		if(flag1!=flag2||flag2!=flag3||flag1!=flag3) {
			throw new NotchangeableUserSaltException();
		}
		
		//9. 전화번호 변경시에는 정규식에 부합해야하며, 다른사람이 사용하지 않는 고유의 전화번호여야함.
		String new_user_phone = (String) param.get("new_user_phone");
		if(new_user_phone!=null) {
			param.put("user_phone", new_user_phone);
			if(userDAO.findUserInfoByUserPhone(param)!=null) {
				throw new DuplicateUserPhoneException();
			}else if(!RegexUtil.checkRegex(new_user_phone, RegexUtil.USER_PHONE_REGEX)) {
				throw new UserPhoneNotMatchedToRegexException();
			}
		}
		
		int row = userDAO.updateUserInfo(param);
		
		if(row!=1) {
			throw new Exception();
		}		
	}

	@Override
	public void deleteUserInfo(HashMap<String,String> param) {
		// TODO Auto-generated method stub
		
	}
}