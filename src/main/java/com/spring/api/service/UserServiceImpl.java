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
	
	//����Ű �� ���Ű�� �����ϰ�, ����Ű�� Ŭ���̾�Ʈ���� ��ȯ, ���Ű�� Redis�� �����ð����� �����ϴ� ����.
	@Override
	public String createNewUserKeys() throws Exception {
		HashMap<String,String> keyPair = RSA2048.createKeys();
		
		String user_publickey = keyPair.get("user_publickey");
		String user_privatekey = keyPair.get("user_privatekey");

		RedisUtil.setData(user_publickey, user_privatekey,RedisUtil.PRIVATEKEY_MAXAGE);
		
		return user_publickey;
	}
	
	//ȸ������ ��û�� ó���ϴ� ����.
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

		//1. ����Ű�� ��ȿ���� ������ ����Ű ��ȿ�� ������ ���ܰ� �߻���.
		//   ���Ű�� Redis�� ����� �� �ִ� �ð��� �����ų�, ����Ű ��ü�� ��ȿ���� ������ �߻���.
		if((user_privatekey = (String) RedisUtil.getData(user_publickey))==null) {
			throw new InvalidPublicKeyException();
		}

		//2. ID�� ���޵��� �ʾҰų�, ���Խ��� �������� ������ ���ܰ� �߻���.
		if(!RegexUtil.checkRegex(user_id,RegexUtil.USER_ID_REGEX)) {
			throw new UserIdNotMatchedToRegexException();
		//3. �ش� ID�� �̹� ������ ����� ������ �ִٸ� ���ܰ� �߻���. 
		}else {
			param = new HashMap();
			param.put("user_id", user_id);
			
			HashMap user = userDAO.findUserInfoByUserId(param);
			
			if(user!=null) {
				throw new DuplicateUserIdException();
			}
		}
		
		//4. ��й�ȣ�� ���ԽĿ� �������������� ���ܰ� �߻���.
		user_pw = RSA2048.decrypt(user_pw, user_privatekey);
		if(!RegexUtil.checkRegex(user_pw,RegexUtil.USER_PW_REGEX)) {
			throw new UserPwNotMatchedToRegexException();
		}
		
		//5. ����� �̸��� ���ԽĿ� �������� ������ ���ܰ� �߻���.
		if(!RegexUtil.checkRegex(user_name,RegexUtil.USER_NAME_REGEX)){
			throw new UserNameNotMatchedToRegexException();
		}
		
		//6. ����� ��ȭ��ȣ�� ���ԽĿ� �������� ������ ���ܰ� �߻���.
		if(!RegexUtil.checkRegex(user_phone,RegexUtil.USER_PHONE_REGEX)) {
			throw new UserPhoneNotMatchedToRegexException();
		//7. ����� ��ȭ��ȣ�� ȸ�������� ����� ������ �̹� �����Ѵٸ� ���ܰ� �߻���.
		}else {
			param = new HashMap();
			param.put("user_phone", user_phone);
			
			HashMap user = userDAO.findUserInfoByUserPhone(param);
			
			if(user!=null) {
				throw new DuplicateUserPhoneException();
			}
		}
		
		//8. UUID�� ���ԽĿ� �������� ������ ���ܰ� �߻���.
		if(!RegexUtil.checkRegex(question_id,RegexUtil.UUID_REGEX)) {
			throw new UUIDNotMatchedToRegexException();
		}
		
		//9. ��й�ȣ ã�� ������ ���� ���� Ư�� ����Ʈ�̻��� ũ�⸦ ���´ٸ� ���ܰ� �߻���. 
		question_answer = RSA2048.decrypt(question_answer, user_privatekey).replaceAll(" ", "");
		if(!RegexUtil.checkBytes(question_answer,RegexUtil.QUESTION_ANSWER_MAXBYTES)) {
			throw new QuestionAnswerExceededLimitOnMaxbytesException();
		}
		
		//10. ��й�ȣ, ��й�ȣ ã�� ������ ���� ������ SALT���� SHA512�ؽ��Լ��� �� �� �ؽ��Ͽ� ������.
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
		
		//11. �ش� ȸ�������� ȸ�������� ������ ������.
		int row = userDAO.createNewUserInfo(param);
		
		if(row==0) {
			throw new Exception();
		}else {
			return;
		}
	}

	//ȸ�������� ��ȸ�ϴ� ����, �����ڴ� ��ο� ���� ����, �Ϲ� ����ڴ� �ڱ� �ڽŸ��� ������ ��ȸ�� �� ����.
	@Override
	public HashMap readUserInfo(HttpServletRequest request, String target_user_id) throws 
	NotFoundUserException,
	NotAuthorizedException,
	Exception 
	{
		//1. ���޹��� ��ū���� �ش� ����ڿ� ���� ȸ�� ������ȸ�� �������� ���θ� Ȯ����.
		String user_accesstoken = JwtUtil.getAccesstoken(request);
		String jwt_user_id = (String)JwtUtil.getData(user_accesstoken, "user_id");

		int user_type_id = (Integer)JwtUtil.getData(user_accesstoken, "user_type_id");
		
		if(user_type_id!=0&&!target_user_id.equals(jwt_user_id)) {
			throw new NotAuthorizedException();
		}
		
		//2. ��ȸ�� �����ϹǷ� ������ �ش� ����ڿ� ���� ������ ��ȸ��.
		HashMap param = new HashMap();
		param.put("user_id", target_user_id);
		
		HashMap user = userDAO.readUserInfo(param);
		
		//3. �ش� ����� ������ �������� ������ ����ó����.
		if(user==null) {
			throw new NotFoundUserException();
		}
		
		//4. ���ʿ��ϰų�, �ΰ��� ������ ��ȸ��󿡼� ������.
		user.remove("question_answer");
		user.remove("question_id");
		user.remove("user_refreshtoken");
		user.remove("user_accesstoken");
		user.remove("user_pw");
		user.remove("user_salt");
		
		return user;
	}

	//ȸ�������� �����ϴ� ����, �ڱ��ڽŸ��� ȸ�������� ������ ������.
	//���氡���� ������ �̸�, ��ȭ��ȣ, ��й�ȣ, ��й�ȣ ã�� ����, ��й�ȣ ã�� ������ ���� �丸 ���� ������.
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
		//1. �ش� ȸ�������� DB�� ������ �����ϴ��� Ȯ����.
		String user_accesstoken = JwtUtil.getAccesstoken(request);
		HashMap user = userDAO.readUserInfo(param);
		
		if(user==null) {
			throw new NotFoundUserException();
		}
		
		//2. ȸ�������� ������ ������ �ִ��� �Ǵ���.
		String target_user_id = param.get("user_id");
		String jwt_user_id = (String) JwtUtil.getData(user_accesstoken, "user_id");
		
		if(!jwt_user_id.equals(target_user_id)) {
			throw new NotAuthorizedException();
		}
		
		//3. ��й�ȣ ���� ������ ��ġ�ϴ��� Ȯ����.
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
		
		//4. ������ ȸ�� �����鿡 ���� ��ȿ���� �˻���.
		//   �� �κп����� �̸��� ���ԽĿ� �����ϴ��� �˻���.
		String new_user_name = (String) param.get("new_user_name");
		
		if(new_user_name!=null) {
			if(!RegexUtil.checkRegex(new_user_name, RegexUtil.USER_NAME_REGEX)) {
				throw new UserNameNotMatchedToRegexException();
			}
		}
		
		//5. ��й�ȣ ����ÿ��� ���ԽĿ� �����ؾ���.
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
		
		//6. ��й�ȣ ã�� ��������ÿ��� UUID�� ���ԽĿ� �����ؾ���.
		String new_question_id = (String) param.get("new_question_id");
		if(new_question_id!=null) {
			if(!RegexUtil.checkRegex(new_question_id, RegexUtil.UUID_REGEX)) {
				throw new UUIDNotMatchedToRegexException();
			}else {
				flag2=true;
			}
		}
		
		
		//7. ��й�ȣ ã�� ������ ���� ����ÿ��� �ش� ���� Ư�� ����Ʈ ������ ũ�⿩����.
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
		
		//8. ��й�ȣ, ��й�ȣ ã�� ����, ��й�ȣ ã�� ������ ���� ���� ��� �ϳ��� �����Ǹ�
		//   ������ ��� ���ÿ� �����Ǿ�߸�, user_salt�� ������ �� ����.
		if(flag1!=flag2||flag2!=flag3||flag1!=flag3) {
			throw new NotchangeableUserSaltException();
		}
		
		//9. ��ȭ��ȣ ����ÿ��� ���ԽĿ� �����ؾ��ϸ�, �ٸ������ ������� �ʴ� ������ ��ȭ��ȣ������.
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