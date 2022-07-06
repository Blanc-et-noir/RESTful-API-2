package com.spring.api.service;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.spring.api.code.ErrorCode;
import com.spring.api.code.PointCode;
import com.spring.api.dao.BookDAO;
import com.spring.api.dao.UserDAO;
import com.spring.api.encrypt.RSA2048;
import com.spring.api.encrypt.SHA;
import com.spring.api.exception.CustomException;
import com.spring.api.util.JwtUtil;
import com.spring.api.util.RedisUtil;
import com.spring.api.util.RegexUtil;

@Transactional(rollbackFor= {
		CustomException.class,
		RuntimeException.class,
		Exception.class
	}
)
@Service("userService")
public class UserServiceImpl implements UserService {
	@Autowired
	private JwtUtil jwtUtil;
	@Autowired
	private RedisUtil redisUtil;
	@Autowired
	private UserDAO userDAO;
	@Autowired
	private BookDAO bookDAO;
	
	//����Ű �� ���Ű�� �����ϰ�, ����Ű�� Ŭ���̾�Ʈ���� ��ȯ, ���Ű�� Redis�� �����ð����� �����ϴ� ����.
	@Override
	public String createNewUserKeys() {
		HashMap<String,String> keyPair = RSA2048.createKeys();
		
		String user_publickey = keyPair.get("user_publickey");
		String user_privatekey = keyPair.get("user_privatekey");

		redisUtil.setData(user_publickey, user_privatekey,redisUtil.PRIVATEKEY_MAXAGE);
		System.out.println(redisUtil.getData(user_publickey));
		return user_publickey;
	}
	
	//ȸ������ ��û�� ó���ϴ� ����.
	@Override
	public void createNewUserInfo(HashMap<String,String> param){
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
		if((user_privatekey = (String) redisUtil.getData(user_publickey))==null) {
			throw new CustomException(ErrorCode.INVALID_PUBLICKEY);
		}

		//2. ID�� ���޵��� �ʾҰų�, ���Խ��� �������� ������ ���ܰ� �߻���.
		if(!RegexUtil.checkRegex(user_id,RegexUtil.USER_ID_REGEX)) {
			throw new CustomException(ErrorCode.USER_ID_NOT_MATCHED_TO_REGEX);
		//3. �ش� ID�� �̹� ������ ����� ������ �ִٸ� ���ܰ� �߻���. 
		}else {
			param = new HashMap();
			param.put("user_id", user_id);
			
			HashMap user = userDAO.findUserInfoByUserId(param);
			
			if(user!=null) {
				throw new CustomException(ErrorCode.DUPLICATE_USER_ID);
			}
		}
		
		//4. ��й�ȣ�� ���ԽĿ� �������������� ���ܰ� �߻���.
		user_pw = RSA2048.decrypt(user_pw, user_privatekey);
		if(!RegexUtil.checkRegex(user_pw,RegexUtil.USER_PW_REGEX)) {
			throw new CustomException(ErrorCode.USER_PW_NOT_MATCHED_TO_REGEX);
		}
		
		//5. ����� �̸��� ���ԽĿ� �������� ������ ���ܰ� �߻���.
		if(!RegexUtil.checkRegex(user_name,RegexUtil.USER_NAME_REGEX)){
			throw new CustomException(ErrorCode.USER_NAME_NOT_MATCHED_TO_REGEX);
		}
		
		//6. ����� ��ȭ��ȣ�� ���ԽĿ� �������� ������ ���ܰ� �߻���.
		if(!RegexUtil.checkRegex(user_phone,RegexUtil.USER_PHONE_REGEX)) {
			throw new CustomException(ErrorCode.USER_PHONE_NOT_MATCHED_TO_REGEX);
		//7. ����� ��ȭ��ȣ�� ȸ�������� ����� ������ �̹� �����Ѵٸ� ���ܰ� �߻���.
		}else {
			param = new HashMap();
			param.put("user_phone", user_phone);
			
			HashMap user = userDAO.findUserInfoByUserPhone(param);
			
			if(user!=null) {
				throw new CustomException(ErrorCode.DUPLICATE_USER_PHONE);
			}
		}
		
		//8. UUID�� ���ԽĿ� �������� ������ ���ܰ� �߻���.
		if(!RegexUtil.checkRegex(question_id,RegexUtil.UUID_REGEX)) {
			throw new CustomException(ErrorCode.UUID_NOT_MATCHED_TO_REGEX);
		}
		
		//9. ��й�ȣ ã�� ������ ���� ���� Ư�� ����Ʈ�̻��� ũ�⸦ ���´ٸ� ���ܰ� �߻���. 
		question_answer = RSA2048.decrypt(question_answer, user_privatekey).replaceAll(" ", "");
		if(!RegexUtil.checkBytes(question_answer,RegexUtil.QUESTION_ANSWER_MAXBYTES)) {
			throw new CustomException(ErrorCode.QUESTION_ANSWER_EXCEEDED_LIMIT_ON_MAXBYTES);
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
			throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
		}else {
			return;
		}
	}

	//ȸ�������� ��ȸ�ϴ� ����, �����ڴ� ��ο� ���� ����, �Ϲ� ����ڴ� �ڱ� �ڽŸ��� ������ ��ȸ�� �� ����.
	@Override
	public HashMap readUserInfo(HttpServletRequest request, String target_user_id){
		//1. ���޹��� ��ū���� �ش� ����ڿ� ���� ȸ�� ������ȸ�� �������� ���θ� Ȯ����.
		String user_accesstoken = jwtUtil.getAccesstoken(request);
		String jwt_user_id = (String)jwtUtil.getData(user_accesstoken, "user_id");

		int user_type_id = (Integer)jwtUtil.getData(user_accesstoken, "user_type_id");
		
		if(user_type_id!=0&&!target_user_id.equals(jwt_user_id)) {
			throw new CustomException(ErrorCode.NOT_AUTHORIZED);
		}
		
		//2. ��ȸ�� �����ϹǷ� ������ �ش� ����ڿ� ���� ������ ��ȸ��.
		HashMap param = new HashMap();
		param.put("user_id", target_user_id);
		
		HashMap user = userDAO.readUserInfo(param);
		
		//3. �ش� ����� ������ �������� ������ ����ó����.
		if(user==null) {
			throw new CustomException(ErrorCode.NOT_FOUND_USER);
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
	public void updateUserInfo(HttpServletRequest request, HashMap<String,String> param){
		//1. �ش� ȸ�������� DB�� ������ �����ϴ��� Ȯ����.
		String user_accesstoken = jwtUtil.getAccesstoken(request);
		HashMap user = userDAO.readUserInfo(param);
		
		if(user==null) {
			throw new CustomException(ErrorCode.NOT_FOUND_USER);
		}
		
		//2. ȸ�������� ������ ������ �ִ��� �Ǵ���.
		String target_user_id = param.get("user_id");
		String jwt_user_id = (String) jwtUtil.getData(user_accesstoken, "user_id");
		
		if(!jwt_user_id.equals(target_user_id)) {
			throw new CustomException(ErrorCode.NOT_AUTHORIZED);
		}
		
		//3. ��й�ȣ ���� ������ ��ġ�ϴ��� Ȯ����.
		String user_salt = (String) user.get("user_salt");
		String question_answer = (String) param.get("question_answer");
		String user_publickey = (String) param.get("user_publickey");
		String user_privatekey = (String) redisUtil.getData(user_publickey);
		
		if(user_privatekey==null) {
			throw new CustomException(ErrorCode.INVALID_PUBLICKEY);
		}
		
		if(question_answer==null) {
			throw new CustomException(ErrorCode.QUESTION_ANSWER_REQUIRED);
		}
		
		question_answer = SHA.DSHA512(RSA2048.decrypt(question_answer, user_privatekey).replaceAll(" ", ""),user_salt);
		
		if(!question_answer.equals((String)user.get("question_answer"))){
			throw new CustomException(ErrorCode.QUESTION_ANSWER_NOT_MATCHED);
		}
		
		//4. ������ ȸ�� �����鿡 ���� ��ȿ���� �˻���.
		//   �� �κп����� �̸��� ���ԽĿ� �����ϴ��� �˻���.
		String new_user_name = (String) param.get("new_user_name");
		
		if(new_user_name!=null) {
			if(!RegexUtil.checkRegex(new_user_name, RegexUtil.USER_NAME_REGEX)) {
				throw new CustomException(ErrorCode.USER_NAME_NOT_MATCHED_TO_REGEX);
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
				throw new CustomException(ErrorCode.USER_PW_NOT_MATCHED_TO_REGEX);
			}
		}
		
		//6. ��й�ȣ ã�� ��������ÿ��� UUID�� ���ԽĿ� �����ؾ���.
		String new_question_id = (String) param.get("new_question_id");
		if(new_question_id!=null) {
			if(!RegexUtil.checkRegex(new_question_id, RegexUtil.UUID_REGEX)) {
				throw new CustomException(ErrorCode.UUID_NOT_MATCHED_TO_REGEX);
			}else {
				flag2=true;
			}
		}
		
		
		//7. ��й�ȣ ã�� ������ ���� ����ÿ��� �ش� ���� Ư�� ����Ʈ ������ ũ�⿩����.
		String new_question_answer = (String) param.get("new_question_answer");
		if(new_question_answer!=null) {
			new_question_answer = RSA2048.decrypt(new_question_answer, user_privatekey).replaceAll(" ", "");
			if(RegexUtil.checkBytes(new_question_answer, RegexUtil.QUESTION_ANSWER_MAXBYTES)) {
				new_question_answer = SHA.DSHA512(new_question_answer, new_user_salt);
				param.put("new_user_salt", new_user_salt);
				param.put("new_question_answer", new_question_answer);
				flag3=true;
			}else {
				throw new CustomException(ErrorCode.QUESTION_ANSWER_EXCEEDED_LIMIT_ON_MAXBYTES);
			}
		}
		
		//8. ��й�ȣ, ��й�ȣ ã�� ����, ��й�ȣ ã�� ������ ���� ���� ��� �ϳ��� �����Ǹ�
		//   ������ ��� ���ÿ� �����Ǿ�߸�, user_salt�� ������ �� ����.
		if(flag1!=flag2||flag2!=flag3||flag1!=flag3) {
			throw new CustomException(ErrorCode.NOT_CHANGEABLE_USER_SALT);
		}
		
		//9. ��ȭ��ȣ ����ÿ��� ���ԽĿ� �����ؾ��ϸ�, �ٸ������ ������� �ʴ� ������ ��ȭ��ȣ������.
		String new_user_phone = (String) param.get("new_user_phone");
		if(new_user_phone!=null) {
			param.put("user_phone", new_user_phone);
			if(!RegexUtil.checkRegex(new_user_phone, RegexUtil.USER_PHONE_REGEX)) {
				throw new CustomException(ErrorCode.USER_PHONE_NOT_MATCHED_TO_REGEX);
			}else if(userDAO.findUserInfoByUserPhone(param)!=null) {
				throw new CustomException(ErrorCode.DUPLICATE_USER_PHONE);
			}
		}
		
		int row = userDAO.updateUserInfo(param);
		
		if(row!=1) {
			throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
		}		
	}

	@Override
	public void deleteUserInfo(HashMap<String,String> param) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void createCheckoutInfo(HttpServletRequest request, HashMap param) {
		//1. �ش� ȸ�������� DB�� ������ �����ϴ��� Ȯ����.
		HashMap user = userDAO.readUserInfo(param);
				
		if(user==null) {
			throw new CustomException(ErrorCode.NOT_FOUND_USER);
		}
		
		//2. ���� ��û�� ������ ������ �ִ��� �Ǵ���.
		String user_accesstoken = jwtUtil.getAccesstoken(request);
		Integer user_type_id = (Integer) jwtUtil.getData(user_accesstoken, "user_type_id");
						
		if(user_type_id!=0) {
			throw new CustomException(ErrorCode.NOT_AUTHORIZED);
		}
		
		//3. ���Ⱑ�� �ð����Ŀ� �����ϴ� ������ �Ǵ���.
		Timestamp checkout_date = Timestamp.valueOf((String)user.get("checkout_date"));
		Timestamp now = new Timestamp(System.currentTimeMillis());
		
		if(checkout_date.before(checkout_date)) {
			throw new CustomException(ErrorCode.NOT_ABLE_TO_CHECK_OUT_DUE_TO_DATE);
		}
		
		//4. ���� �������� ������ ���� 3�̸����� Ȯ����.
		List<HashMap> list = userDAO.readCheckOutInfosByUserId(param);
		if(list.size()>=3) {
			throw new CustomException(ErrorCode.NOT_ABLE_TO_CHECK_OUT_DUE_TO_LIMIT);
		}
		
		//5. �ڽ��� �����ϰ��ִ� ������ �����Ϸ��� ������ �Ǵ���.
		for(HashMap checkout : list) {
			if(((String)checkout.get("book_isbn")).equals((String)param.get("book_isbn"))) {
				throw new CustomException(ErrorCode.NOT_ABLE_TO_CHECK_OUT_DUE_TO_ALREADY_CHECKED_OUT); 
			}
		}
		
		//6. ��ü�� ���������� �����ϴ��� �Ǵ���.
		for(HashMap hm : list) {
			Timestamp time = Timestamp.valueOf((String)hm.get("checkout_end_date"));
			if(time.before(now)) {
				throw new CustomException(ErrorCode.NOT_ABLE_TO_CHECK_OUT_DUE_TO_OVERDUE);
			}
		}
		
		//7. ISBN �ڵ尡 ���Ŀ� �´��� Ȯ����.
		String book_isbn = (String) param.get("book_isbn");
		if(!RegexUtil.checkRegex(book_isbn, RegexUtil.BOOK_ISBN_REGEX)) {
			throw new CustomException(ErrorCode.BOOK_ISBN_NOT_MATCHED_TO_REGEX);
		}
		
		//8. �ش� ������ �����ϴ��� Ȯ����.
		HashMap book = bookDAO.readBookInfo(param);
		if(book==null) {
			throw new CustomException(ErrorCode.NOT_FOUND_BOOK);
		}
		
		//9. �ش� ������ ��� ��ȿ���� Ȯ����.
		Integer book_quantity = (Integer) book.get("book_quantity");
		if(book_quantity<=0) {
			throw new CustomException(ErrorCode.TOO_FEW_BOOK_QUANTITY);
		}
		
		//10. �ش� ������ �����ߴ� �������� �ƴ��� �Ǵ���.
		param.put("limit", book_quantity);
		List<HashMap> reservations = userDAO.readReservationInfosByBookIsbn(param);
		
		boolean isReserved = false;
		
		Iterator<HashMap> itor = reservations.iterator();
		int cnt = 0;
		while(itor.hasNext()) {
			HashMap reservation = itor.next();
			if(reservation.get("user_id").equals(param.get("user_id"))) {
				isReserved = true;
				param.put("reservation_id", reservation.get("reservation_id"));
				break;
			}
			cnt++;
		}

		//11. �����ߴ� ������� ���� �켱������ ���� ���� ������ ó����.
		if(isReserved) {
			//12. ���� �켱������ ���ٸ� ������ �����ϰ� ���� ������ ó����.
			if(cnt<book_quantity) {			
				if(userDAO.deleteReservation(param)!=1) {
					throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
				}
			//13. �����ϱ� ������ �켱������ ���� ������ �� ������ ������ ó������ ����.
			}else {
				throw new CustomException(ErrorCode.NOT_ABLE_TO_CHECK_OUT_DUE_TO_PRIORITY_OF_RESERVATIONS);
			}
		//14. �����ߴ� ������ �ƴ϶�� �ش� ������ ���������� ������ �� ���� ��� ���హ������ ������ Ȯ����.
		}else {
			if(book_quantity <= reservations.size()) {
				throw new CustomException(ErrorCode.NOT_ABLE_TO_CHECK_OUT_DUE_TO_TOO_MANY_RESERVATIONS);
			}				
		}
		
		//15. ���� ����� �߰���.
		String checkout_id = UUID.randomUUID().toString();
		param.put("checkout_id", checkout_id);
		userDAO.createCheckoutInfo(param);
		
		//16. �ش� ������ ��� 1���ҽ�Ŵ
		bookDAO.decreaseBookQuantity(param);
	}

	@Override
	public void deleteCheckoutInfo(HttpServletRequest request, HashMap param) {
		//1. �ش� ȸ�������� DB�� ������ �����ϴ��� Ȯ����.
		HashMap user = userDAO.readUserInfo(param);
						
		if(user==null) {
			throw new CustomException(ErrorCode.NOT_FOUND_USER);
		}
		
		//2. ���� ��û�� ������ ������ �ִ��� �Ǵ���.
		String user_accesstoken = jwtUtil.getAccesstoken(request);
		Integer user_type_id = (Integer) jwtUtil.getData(user_accesstoken, "user_type_id");
						
		if(user_type_id!=0) {
			throw new CustomException(ErrorCode.NOT_AUTHORIZED);
		}
		
		//3. �ݳ����� ���� �ش� ������ ����� �ִ��� Ȯ����.
		HashMap checkout = userDAO.readCheckOutInfoByUserId(param);
		if(checkout==null) {
			throw new CustomException(ErrorCode.NOT_FOUND_CHECKOUT);
		}
		
		Timestamp begin = Timestamp.valueOf((String) checkout.get("checkout_begin_date"));
		Timestamp end = Timestamp.valueOf((String) checkout.get("checkout_end_date"));
		Timestamp now = new Timestamp(System.currentTimeMillis());
		
		//4. ��ü���θ� Ȯ����.
		final long MILLIS_PER_DAY = 1000*60*60*24;
		
		if(now.after(end)) {
			long dif = now.getTime() - end.getTime();
			int overdue = (int) Math.ceil(dif*1.0/MILLIS_PER_DAY);
			
			//5. ��ü�ߴٸ� ���� ���Ⱑ���� �ð��� checkout_end_date�� �� ���� �ð��� + overdue�� �Ͽ� ���ο� ���Ⱑ���� �ð����� ��ü��.
			Timestamp checkout_date = Timestamp.valueOf((String)user.get("checkout_date"));
			Timestamp newCheckout_date;
			
			Calendar cal = Calendar.getInstance();
			
			if(checkout_date.after(end)) {
				cal.setTime(checkout_date);
		        cal.add(Calendar.DATE, overdue);
		        newCheckout_date = new Timestamp(cal.getTime().getTime());
			}else {
				cal.setTime(end);
		        cal.add(Calendar.DATE, overdue);
		        newCheckout_date = new Timestamp(cal.getTime().getTime());
			}
			
			param.put("newCheckout_date", newCheckout_date.toString());
			
			//6. ���� ���� �ð� ������Ʈ
			int row = userDAO.updateUserCheckOutDate(param);
			if(row!=1) {
				throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
			}
		}else {
			//7. 7���̻� ������ å�� ���ؼ��� ����Ʈ�� ������
			long dif = (now.getTime() - begin.getTime())/MILLIS_PER_DAY;
			if(dif>=7) {
				param.put("point_code", PointCode.RETURN_BOOK_WITHIN_7DAYS.getPOINT_CODE());
				param.put("point_amount", PointCode.RETURN_BOOK_WITHIN_7DAYS.getPOINT_AMOUNT());
				param.put("point_id", UUID.randomUUID().toString());
				param.put("point_content", PointCode.RETURN_BOOK_WITHIN_7DAYS.getPOINT_MESSAGE());
				
				//8. ����Ʈ �̷� �߰�
				if(userDAO.createNewPointInfo(param)!=1) {
					throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
				}
				
				//9. ����� ����Ʈ ������Ʈ
				if(userDAO.increaseUserPoint(param)!=1) {
					throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
				}
			}
		}
		
		//10. �ݳ��� ó����.
		int row = userDAO.returnCheckOut(param);
		if(row!=1) {
			throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
		}
		
		//11. �ش� ������ ��� 1 ������Ŵ
		param.put("book_isbn", checkout.get("book_isbn"));
		row = bookDAO.increaseBookQuantity(param);
		if(row!=1) {
			throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public void createNewReservationInfo(HttpServletRequest request, HashMap param) {
		//1. �ش� ȸ�������� DB�� ������ �����ϴ��� Ȯ����.
		HashMap user = userDAO.readUserInfo(param);
								
		if(user==null) {
			throw new CustomException(ErrorCode.NOT_FOUND_USER);
		}
		
		//2. ���� ������ �ð��� �������� �Ǵ���.
		Timestamp now = new Timestamp(System.currentTimeMillis());
		Timestamp checkout_date = Timestamp.valueOf((String) user.get("checkout_date"));
		if(now.before(checkout_date)) {
			throw new CustomException(ErrorCode.NOT_ABLE_TO_RESERVE_DUE_TO_DATE);
		}
		
		//2. �ش� ��ū���� ������ �� �ִ��� Ȯ����.
		String user_accesstoken = jwtUtil.getAccesstoken(request);
		String jwt_user_id = (String) jwtUtil.getData(user_accesstoken, "user_id");
		String user_id = (String) param.get("user_id");
		
		if(!(jwt_user_id!=null&&jwt_user_id.equals(user_id))) {
			throw new CustomException(ErrorCode.NOT_AUTHORIZED);
		}
		
		//3. �ݳ����� ���� �ش� �����߿��� ��ü�� ���� ������ �����ϴ��� �Ǵ���.
		List<HashMap> checkouts = userDAO.readCheckOutInfosByUserId(param);
		for(HashMap checkout : checkouts) {
			Timestamp begin = Timestamp.valueOf((String) checkout.get("checkout_begin_date"));
			Timestamp end = Timestamp.valueOf((String) checkout.get("checkout_end_date"));
			if(now.after(end)) {
				throw new CustomException(ErrorCode.NOT_ABLE_TO_RESERVE_DUE_TO_OVERDUE);
			}
		}
		
		//4. �ڽ��� �����ϰ��ִ� ������ �����Ϸ��� ������ �Ǵ���.
		for(HashMap checkout : checkouts) {
			if(((String)checkout.get("book_isbn")).equals((String)param.get("book_isbn"))) {
				throw new CustomException(ErrorCode.NOT_ABLE_TO_RESERVE_DUE_TO_ALREADY_CHECKED_OUT); 
			}
		}
		
		//5. �ش� ������ ������ ����� 5�� �̸����� �Ǵ���.
		List<HashMap> list = userDAO.readReservationInfosByBookIsbn(param);
		if(list.size()>=5) {
			throw new CustomException(ErrorCode.NOT_ABLE_TO_RESERVE_DUE_TO_FULL);
		}
		
		//6. �ڽ��� ����Ƚ���� 3�̸����� �Ǵ���.
		list = userDAO.readReservationInfosByUserId(param);
		if(list.size()>=3) {
			throw new CustomException(ErrorCode.NOT_ABLE_TO_RESERVE_DUE_TO_MANY);
		}
		
		//7. �ڽ��� ������ �ش� ������ �̹� �ִ��� �Ǵ���.
		for(HashMap reservation : list) {
			String book_isbn = (String) reservation.get("book_isbn");
			if(book_isbn.equals((String)param.get("book_isbn"))){
				throw new CustomException(ErrorCode.DUPLICATE_BOOK_ISBN_OF_RESERVATION);
			}
		}
		
		//8. ���� ������ ó����.
		param.put("reservation_id", UUID.randomUUID().toString());
		if(userDAO.createNewReservationInfo(param)!=1) {
			throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public void deleteReservationInfo(HttpServletRequest request, HashMap param) {
		//1. �ش� ȸ�������� DB�� ������ �����ϴ��� Ȯ����.
		HashMap user = userDAO.readUserInfo(param);
										
		if(user==null) {
			throw new CustomException(ErrorCode.NOT_FOUND_USER);
		}
		
		//2. �ش� ��ū���� ������ �� �ִ��� Ȯ����.
		String user_accesstoken = jwtUtil.getAccesstoken(request);
		String jwt_user_id = (String) jwtUtil.getData(user_accesstoken, "user_id");
		String user_id = (String) param.get("user_id");
				
		if(!(jwt_user_id!=null&&jwt_user_id.equals(user_id))) {
			throw new CustomException(ErrorCode.NOT_AUTHORIZED);
		}
		
		//3. �ش� ���� ������ ������ �����ϴ��� Ȯ����.
		HashMap reservation = userDAO.readReservationInfoByReservationId(param);
		if(reservation==null) {
			throw new CustomException(ErrorCode.NOT_FOUND_RESERVATION);
		}
		
		//4. �ش� ���������� ������.
		if(userDAO.deleteReservation(param)!=1) {
			throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public List<HashMap> readCheckoutInfo(HttpServletRequest request, HashMap param) {
		//1. �ش� ȸ�������� DB�� ������ �����ϴ��� Ȯ����.
		HashMap user = userDAO.readUserInfo(param);								
		if(user==null) {
			throw new CustomException(ErrorCode.NOT_FOUND_USER);
		}
		
		//2. �ش� ��ū���� ���������� ��ȸ�� �� �ִ��� Ȯ����.
		String user_accesstoken = jwtUtil.getAccesstoken(request);
		String jwt_user_id = (String)jwtUtil.getData(user_accesstoken, "user_id");
		String user_id = (String) param.get("user_id");
		int user_type_id = (Integer)jwtUtil.getData(user_accesstoken, "user_type_id");
		
		if(user_type_id!=0&&!user_id.equals(jwt_user_id)) {
			throw new CustomException(ErrorCode.NOT_AUTHORIZED);
		}
		
		//3. ���� ������ ��ȸ��.
		List<HashMap> checkouts = userDAO.readCheckOutInfosWithOptions(param);
		List<HashMap> list = new LinkedList<HashMap>();
		
		//4. ��ȸ�� ���� ������ ������.
		for(HashMap temp : checkouts) {
			HashMap book = new HashMap();
			book.put("book_isbn", temp.get("book_isbn"));
			book.put("book_name", temp.get("book_name"));
			book.put("book_type_id", temp.get("book_type_id"));
			book.put("book_type_content", temp.get("book_type_content"));
			book.put("book_publisher", temp.get("book_publisher"));
			
			String[] book_authors = null;
			if(temp.get("book_authors")!=null) {
				List<String> authors = new LinkedList<String>();
				book_authors = ((String)temp.get("book_authors")).split(" ");
				for(String book_author_name : book_authors) {
					authors.add(book_author_name);
				}
				book.put("book_authors", authors);
			}
			
			String[] book_translators = null;
			if(temp.get("book_translators")!=null) {
				List<String> translators = new LinkedList<String>();
				book_translators = ((String)temp.get("book_translators")).split(" ");
				for(String book_translator_name : book_translators) {
					translators.add(book_translator_name);
				}
				book.put("book_translators", translators);
			}
			
			HashMap checkout = new HashMap();
			checkout.put("checkout_id", temp.get("checkout_id"));
			checkout.put("checkout_begin_date", temp.get("checkout_begin_date"));
			checkout.put("checkout_end_date", temp.get("checkout_end_date"));
			checkout.put("checkout_return_date", temp.get("checkout_return_date"));
			checkout.put("checkout_renew_count", temp.get("checkout_renew_count"));
			checkout.put("book", book);
			
			
			list.add(checkout);
		}
		
		return list;
	}
}