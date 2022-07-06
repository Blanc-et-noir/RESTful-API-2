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
	
	//공개키 및 비밀키를 생성하고, 공개키는 클라이언트에게 반환, 비밀키는 Redis에 일정시간동안 저장하는 로직.
	@Override
	public String createNewUserKeys() {
		HashMap<String,String> keyPair = RSA2048.createKeys();
		
		String user_publickey = keyPair.get("user_publickey");
		String user_privatekey = keyPair.get("user_privatekey");

		redisUtil.setData(user_publickey, user_privatekey,redisUtil.PRIVATEKEY_MAXAGE);
		System.out.println(redisUtil.getData(user_publickey));
		return user_publickey;
	}
	
	//회원가입 요청을 처리하는 로직.
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
		
		//1. 공개키가 유효하지 않으면 공개키 유효성 불충족 예외가 발생함.
		//   비밀키가 Redis에 저장될 수 있는 시간이 지났거나, 공개키 자체가 유효하지 않을때 발생함.
		if((user_privatekey = (String) redisUtil.getData(user_publickey))==null) {
			throw new CustomException(ErrorCode.INVALID_PUBLICKEY);
		}

		//2. ID가 전달되지 않았거나, 정규식을 만족하지 않으면 예외가 발생함.
		if(!RegexUtil.checkRegex(user_id,RegexUtil.USER_ID_REGEX)) {
			throw new CustomException(ErrorCode.USER_ID_NOT_MATCHED_TO_REGEX);
		//3. 해당 ID로 이미 가입한 사용자 정보가 있다면 예외가 발생함. 
		}else {
			param = new HashMap();
			param.put("user_id", user_id);
			
			HashMap user = userDAO.findUserInfoByUserId(param);
			
			if(user!=null) {
				throw new CustomException(ErrorCode.DUPLICATE_USER_ID);
			}
		}
		
		//4. 비밀번호가 정규식에 부합하지않으면 예외가 발생함.
		user_pw = RSA2048.decrypt(user_pw, user_privatekey);
		if(!RegexUtil.checkRegex(user_pw,RegexUtil.USER_PW_REGEX)) {
			throw new CustomException(ErrorCode.USER_PW_NOT_MATCHED_TO_REGEX);
		}
		
		//5. 사용자 이름이 정규식에 부합하지 않으면 예외가 발생함.
		if(!RegexUtil.checkRegex(user_name,RegexUtil.USER_NAME_REGEX)){
			throw new CustomException(ErrorCode.USER_NAME_NOT_MATCHED_TO_REGEX);
		}
		
		//6. 사용자 전화번호가 정규식에 부합하지 않으면 예외가 발생함.
		if(!RegexUtil.checkRegex(user_phone,RegexUtil.USER_PHONE_REGEX)) {
			throw new CustomException(ErrorCode.USER_PHONE_NOT_MATCHED_TO_REGEX);
		//7. 사용자 전화번호로 회원가입한 사용자 정보가 이미 존재한다면 예외가 발생함.
		}else {
			param = new HashMap();
			param.put("user_phone", user_phone);
			
			HashMap user = userDAO.findUserInfoByUserPhone(param);
			
			if(user!=null) {
				throw new CustomException(ErrorCode.DUPLICATE_USER_PHONE);
			}
		}
		
		//8. UUID가 정규식에 부합하지 않으면 예외가 발생함.
		if(!RegexUtil.checkRegex(question_id,RegexUtil.UUID_REGEX)) {
			throw new CustomException(ErrorCode.UUID_NOT_MATCHED_TO_REGEX);
		}
		
		//9. 비밀번호 찾기 질문에 대한 답이 특정 바이트이상의 크기를 갖는다면 예외가 발생함. 
		question_answer = RSA2048.decrypt(question_answer, user_privatekey).replaceAll(" ", "");
		if(!RegexUtil.checkBytes(question_answer,RegexUtil.QUESTION_ANSWER_MAXBYTES)) {
			throw new CustomException(ErrorCode.QUESTION_ANSWER_EXCEEDED_LIMIT_ON_MAXBYTES);
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
			throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
		}else {
			return;
		}
	}

	//회원정보를 조회하는 로직, 관리자는 모두에 대한 정보, 일반 사용자는 자기 자신만의 정보를 조회할 수 있음.
	@Override
	public HashMap readUserInfo(HttpServletRequest request, String target_user_id){
		//1. 전달받은 토큰으로 해당 사용자에 대한 회원 정보조회가 가능한지 여부를 확인함.
		String user_accesstoken = jwtUtil.getAccesstoken(request);
		String jwt_user_id = (String)jwtUtil.getData(user_accesstoken, "user_id");

		int user_type_id = (Integer)jwtUtil.getData(user_accesstoken, "user_type_id");
		
		if(user_type_id!=0&&!target_user_id.equals(jwt_user_id)) {
			throw new CustomException(ErrorCode.NOT_AUTHORIZED);
		}
		
		//2. 조회가 가능하므로 실제로 해당 사용자에 대한 정보를 조회함.
		HashMap param = new HashMap();
		param.put("user_id", target_user_id);
		
		HashMap user = userDAO.readUserInfo(param);
		
		//3. 해당 사용자 정보가 존재하지 않으면 예외처리함.
		if(user==null) {
			throw new CustomException(ErrorCode.NOT_FOUND_USER);
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
	public void updateUserInfo(HttpServletRequest request, HashMap<String,String> param){
		//1. 해당 회원정보가 DB에 실제로 존재하는지 확인함.
		String user_accesstoken = jwtUtil.getAccesstoken(request);
		HashMap user = userDAO.readUserInfo(param);
		
		if(user==null) {
			throw new CustomException(ErrorCode.NOT_FOUND_USER);
		}
		
		//2. 회원정보를 변경할 권한이 있는지 판단함.
		String target_user_id = param.get("user_id");
		String jwt_user_id = (String) jwtUtil.getData(user_accesstoken, "user_id");
		
		if(!jwt_user_id.equals(target_user_id)) {
			throw new CustomException(ErrorCode.NOT_AUTHORIZED);
		}
		
		//3. 비밀번호 변경 정답이 일치하는지 확인함.
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
		
		//4. 변경할 회원 정보들에 대한 유효성을 검사함.
		//   이 부분에서는 이름이 정규식에 부합하는지 검사함.
		String new_user_name = (String) param.get("new_user_name");
		
		if(new_user_name!=null) {
			if(!RegexUtil.checkRegex(new_user_name, RegexUtil.USER_NAME_REGEX)) {
				throw new CustomException(ErrorCode.USER_NAME_NOT_MATCHED_TO_REGEX);
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
				throw new CustomException(ErrorCode.USER_PW_NOT_MATCHED_TO_REGEX);
			}
		}
		
		//6. 비밀번호 찾기 질문변경시에는 UUID가 정규식에 부합해야함.
		String new_question_id = (String) param.get("new_question_id");
		if(new_question_id!=null) {
			if(!RegexUtil.checkRegex(new_question_id, RegexUtil.UUID_REGEX)) {
				throw new CustomException(ErrorCode.UUID_NOT_MATCHED_TO_REGEX);
			}else {
				flag2=true;
			}
		}
		
		
		//7. 비밀번호 찾기 질문의 정답 변경시에는 해당 답이 특정 바이트 이하의 크기여야함.
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
		
		//8. 비밀번호, 비밀번호 찾기 질문, 비밀번호 찾기 질문에 대한 답중 어느 하나라도 수정되면
		//   나머지 모두 동시에 수정되어야만, user_salt를 변경할 수 있음.
		if(flag1!=flag2||flag2!=flag3||flag1!=flag3) {
			throw new CustomException(ErrorCode.NOT_CHANGEABLE_USER_SALT);
		}
		
		//9. 전화번호 변경시에는 정규식에 부합해야하며, 다른사람이 사용하지 않는 고유의 전화번호여야함.
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
		//1. 해당 회원정보가 DB에 실제로 존재하는지 확인함.
		HashMap user = userDAO.readUserInfo(param);
				
		if(user==null) {
			throw new CustomException(ErrorCode.NOT_FOUND_USER);
		}
		
		//2. 대출 요청을 수행할 권한이 있는지 판단함.
		String user_accesstoken = jwtUtil.getAccesstoken(request);
		Integer user_type_id = (Integer) jwtUtil.getData(user_accesstoken, "user_type_id");
						
		if(user_type_id!=0) {
			throw new CustomException(ErrorCode.NOT_AUTHORIZED);
		}
		
		//3. 대출가능 시각이후에 대출하는 것인지 판단함.
		Timestamp checkout_date = Timestamp.valueOf((String)user.get("checkout_date"));
		Timestamp now = new Timestamp(System.currentTimeMillis());
		
		if(checkout_date.before(checkout_date)) {
			throw new CustomException(ErrorCode.NOT_ABLE_TO_CHECK_OUT_DUE_TO_DATE);
		}
		
		//4. 현재 대출중인 도서의 수가 3미만인지 확인함.
		List<HashMap> list = userDAO.readCheckOutInfosByUserId(param);
		if(list.size()>=3) {
			throw new CustomException(ErrorCode.NOT_ABLE_TO_CHECK_OUT_DUE_TO_LIMIT);
		}
		
		//5. 자신이 대출하고있는 도서를 대출하려는 것인지 판단함.
		for(HashMap checkout : list) {
			if(((String)checkout.get("book_isbn")).equals((String)param.get("book_isbn"))) {
				throw new CustomException(ErrorCode.NOT_ABLE_TO_CHECK_OUT_DUE_TO_ALREADY_CHECKED_OUT); 
			}
		}
		
		//6. 연체한 대출정보가 존재하는지 판단함.
		for(HashMap hm : list) {
			Timestamp time = Timestamp.valueOf((String)hm.get("checkout_end_date"));
			if(time.before(now)) {
				throw new CustomException(ErrorCode.NOT_ABLE_TO_CHECK_OUT_DUE_TO_OVERDUE);
			}
		}
		
		//7. ISBN 코드가 형식에 맞는지 확인함.
		String book_isbn = (String) param.get("book_isbn");
		if(!RegexUtil.checkRegex(book_isbn, RegexUtil.BOOK_ISBN_REGEX)) {
			throw new CustomException(ErrorCode.BOOK_ISBN_NOT_MATCHED_TO_REGEX);
		}
		
		//8. 해당 도서가 존재하는지 확인함.
		HashMap book = bookDAO.readBookInfo(param);
		if(book==null) {
			throw new CustomException(ErrorCode.NOT_FOUND_BOOK);
		}
		
		//9. 해당 도서의 재고가 유효한지 확인함.
		Integer book_quantity = (Integer) book.get("book_quantity");
		if(book_quantity<=0) {
			throw new CustomException(ErrorCode.TOO_FEW_BOOK_QUANTITY);
		}
		
		//10. 해당 도서가 예약했던 도서인지 아닌지 판단함.
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

		//11. 예약했던 도서라면 예약 우선순위에 따라 도서 대출을 처리함.
		if(isReserved) {
			//12. 예약 우선순위가 높다면 예약을 제거하고 도서 대출을 처리함.
			if(cnt<book_quantity) {			
				if(userDAO.deleteReservation(param)!=1) {
					throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
				}
			//13. 예약하긴 했으나 우선순위가 낮아 대출할 수 없으면 대출을 처리하지 않음.
			}else {
				throw new CustomException(ErrorCode.NOT_ABLE_TO_CHECK_OUT_DUE_TO_PRIORITY_OF_RESERVATIONS);
			}
		//14. 예약했던 도서가 아니라면 해당 도서의 예약정보를 가져온 후 남은 재고가 예약갯수보다 많은지 확인함.
		}else {
			if(book_quantity <= reservations.size()) {
				throw new CustomException(ErrorCode.NOT_ABLE_TO_CHECK_OUT_DUE_TO_TOO_MANY_RESERVATIONS);
			}				
		}
		
		//15. 대출 기록을 추가함.
		String checkout_id = UUID.randomUUID().toString();
		param.put("checkout_id", checkout_id);
		userDAO.createCheckoutInfo(param);
		
		//16. 해당 도서의 재고를 1감소시킴
		bookDAO.decreaseBookQuantity(param);
	}

	@Override
	public void deleteCheckoutInfo(HttpServletRequest request, HashMap param) {
		//1. 해당 회원정보가 DB에 실제로 존재하는지 확인함.
		HashMap user = userDAO.readUserInfo(param);
						
		if(user==null) {
			throw new CustomException(ErrorCode.NOT_FOUND_USER);
		}
		
		//2. 대출 요청을 수행할 권한이 있는지 판단함.
		String user_accesstoken = jwtUtil.getAccesstoken(request);
		Integer user_type_id = (Integer) jwtUtil.getData(user_accesstoken, "user_type_id");
						
		if(user_type_id!=0) {
			throw new CustomException(ErrorCode.NOT_AUTHORIZED);
		}
		
		//3. 반납하지 않은 해당 대출의 기록이 있는지 확인함.
		HashMap checkout = userDAO.readCheckOutInfoByUserId(param);
		if(checkout==null) {
			throw new CustomException(ErrorCode.NOT_FOUND_CHECKOUT);
		}
		
		Timestamp begin = Timestamp.valueOf((String) checkout.get("checkout_begin_date"));
		Timestamp end = Timestamp.valueOf((String) checkout.get("checkout_end_date"));
		Timestamp now = new Timestamp(System.currentTimeMillis());
		
		//4. 연체여부를 확인함.
		final long MILLIS_PER_DAY = 1000*60*60*24;
		
		if(now.after(end)) {
			long dif = now.getTime() - end.getTime();
			int overdue = (int) Math.ceil(dif*1.0/MILLIS_PER_DAY);
			
			//5. 연체했다면 현재 대출가능한 시각과 checkout_end_date중 더 나중 시간에 + overdue를 하여 새로운 대출가능한 시각으로 교체함.
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
			
			//6. 대출 가능 시각 업데이트
			int row = userDAO.updateUserCheckOutDate(param);
			if(row!=1) {
				throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
			}
		}else {
			//7. 7일이상 대출한 책에 대해서는 포인트를 지급함
			long dif = (now.getTime() - begin.getTime())/MILLIS_PER_DAY;
			if(dif>=7) {
				param.put("point_code", PointCode.RETURN_BOOK_WITHIN_7DAYS.getPOINT_CODE());
				param.put("point_amount", PointCode.RETURN_BOOK_WITHIN_7DAYS.getPOINT_AMOUNT());
				param.put("point_id", UUID.randomUUID().toString());
				param.put("point_content", PointCode.RETURN_BOOK_WITHIN_7DAYS.getPOINT_MESSAGE());
				
				//8. 포인트 이력 추가
				if(userDAO.createNewPointInfo(param)!=1) {
					throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
				}
				
				//9. 사용자 포인트 업데이트
				if(userDAO.increaseUserPoint(param)!=1) {
					throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
				}
			}
		}
		
		//10. 반납을 처리함.
		int row = userDAO.returnCheckOut(param);
		if(row!=1) {
			throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
		}
		
		//11. 해당 도서의 재고를 1 증가시킴
		param.put("book_isbn", checkout.get("book_isbn"));
		row = bookDAO.increaseBookQuantity(param);
		if(row!=1) {
			throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public void createNewReservationInfo(HttpServletRequest request, HashMap param) {
		//1. 해당 회원정보가 DB에 실제로 존재하는지 확인함.
		HashMap user = userDAO.readUserInfo(param);
								
		if(user==null) {
			throw new CustomException(ErrorCode.NOT_FOUND_USER);
		}
		
		//2. 대출 가능한 시각이 지났는지 판단함.
		Timestamp now = new Timestamp(System.currentTimeMillis());
		Timestamp checkout_date = Timestamp.valueOf((String) user.get("checkout_date"));
		if(now.before(checkout_date)) {
			throw new CustomException(ErrorCode.NOT_ABLE_TO_RESERVE_DUE_TO_DATE);
		}
		
		//2. 해당 토큰으로 예약할 수 있는지 확인함.
		String user_accesstoken = jwtUtil.getAccesstoken(request);
		String jwt_user_id = (String) jwtUtil.getData(user_accesstoken, "user_id");
		String user_id = (String) param.get("user_id");
		
		if(!(jwt_user_id!=null&&jwt_user_id.equals(user_id))) {
			throw new CustomException(ErrorCode.NOT_AUTHORIZED);
		}
		
		//3. 반납하지 않은 해당 대출중에서 연체된 대출 정보가 존재하는지 판단함.
		List<HashMap> checkouts = userDAO.readCheckOutInfosByUserId(param);
		for(HashMap checkout : checkouts) {
			Timestamp begin = Timestamp.valueOf((String) checkout.get("checkout_begin_date"));
			Timestamp end = Timestamp.valueOf((String) checkout.get("checkout_end_date"));
			if(now.after(end)) {
				throw new CustomException(ErrorCode.NOT_ABLE_TO_RESERVE_DUE_TO_OVERDUE);
			}
		}
		
		//4. 자신이 대출하고있는 도서를 예약하려는 것인지 판단함.
		for(HashMap checkout : checkouts) {
			if(((String)checkout.get("book_isbn")).equals((String)param.get("book_isbn"))) {
				throw new CustomException(ErrorCode.NOT_ABLE_TO_RESERVE_DUE_TO_ALREADY_CHECKED_OUT); 
			}
		}
		
		//5. 해당 도서를 예약한 사람이 5명 미만인지 판단함.
		List<HashMap> list = userDAO.readReservationInfosByBookIsbn(param);
		if(list.size()>=5) {
			throw new CustomException(ErrorCode.NOT_ABLE_TO_RESERVE_DUE_TO_FULL);
		}
		
		//6. 자신의 예약횟수가 3미만인지 판단함.
		list = userDAO.readReservationInfosByUserId(param);
		if(list.size()>=3) {
			throw new CustomException(ErrorCode.NOT_ABLE_TO_RESERVE_DUE_TO_MANY);
		}
		
		//7. 자신의 예약중 해당 도서가 이미 있는지 판단함.
		for(HashMap reservation : list) {
			String book_isbn = (String) reservation.get("book_isbn");
			if(book_isbn.equals((String)param.get("book_isbn"))){
				throw new CustomException(ErrorCode.DUPLICATE_BOOK_ISBN_OF_RESERVATION);
			}
		}
		
		//8. 도서 예약을 처리함.
		param.put("reservation_id", UUID.randomUUID().toString());
		if(userDAO.createNewReservationInfo(param)!=1) {
			throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public void deleteReservationInfo(HttpServletRequest request, HashMap param) {
		//1. 해당 회원정보가 DB에 실제로 존재하는지 확인함.
		HashMap user = userDAO.readUserInfo(param);
										
		if(user==null) {
			throw new CustomException(ErrorCode.NOT_FOUND_USER);
		}
		
		//2. 해당 토큰으로 예약할 수 있는지 확인함.
		String user_accesstoken = jwtUtil.getAccesstoken(request);
		String jwt_user_id = (String) jwtUtil.getData(user_accesstoken, "user_id");
		String user_id = (String) param.get("user_id");
				
		if(!(jwt_user_id!=null&&jwt_user_id.equals(user_id))) {
			throw new CustomException(ErrorCode.NOT_AUTHORIZED);
		}
		
		//3. 해당 예약 정보가 실제로 존재하는지 확인함.
		HashMap reservation = userDAO.readReservationInfoByReservationId(param);
		if(reservation==null) {
			throw new CustomException(ErrorCode.NOT_FOUND_RESERVATION);
		}
		
		//4. 해당 예약정보를 삭제함.
		if(userDAO.deleteReservation(param)!=1) {
			throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public List<HashMap> readCheckoutInfo(HttpServletRequest request, HashMap param) {
		//1. 해당 회원정보가 DB에 실제로 존재하는지 확인함.
		HashMap user = userDAO.readUserInfo(param);								
		if(user==null) {
			throw new CustomException(ErrorCode.NOT_FOUND_USER);
		}
		
		//2. 해당 토큰으로 대출정보를 조회할 수 있는지 확인함.
		String user_accesstoken = jwtUtil.getAccesstoken(request);
		String jwt_user_id = (String)jwtUtil.getData(user_accesstoken, "user_id");
		String user_id = (String) param.get("user_id");
		int user_type_id = (Integer)jwtUtil.getData(user_accesstoken, "user_type_id");
		
		if(user_type_id!=0&&!user_id.equals(jwt_user_id)) {
			throw new CustomException(ErrorCode.NOT_AUTHORIZED);
		}
		
		//3. 대출 정보를 조회함.
		List<HashMap> checkouts = userDAO.readCheckOutInfosWithOptions(param);
		List<HashMap> list = new LinkedList<HashMap>();
		
		//4. 조회한 대출 정보를 가공함.
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