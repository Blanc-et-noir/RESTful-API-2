package com.spring.api.service;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.spring.api.code.ErrorCode;
import com.spring.api.dao.MessageDAO;
import com.spring.api.dao.UserDAO;
import com.spring.api.exception.CustomException;
import com.spring.api.util.JwtUtil;
import com.spring.api.util.RegexUtil;

@Transactional(rollbackFor= {
		CustomException.class,
		RuntimeException.class,
		Exception.class
})
@Service("messageService")
public class MessageServiceImpl implements MessageService{
	@Autowired
	private JwtUtil jwtUtil;
	@Autowired
	private UserDAO userDAO;
	@Autowired
	private MessageDAO messageDAO;
	
	@Override
	public void createNewMessage(HttpServletRequest request, HashMap<String, String> param) {
		//1. 송신자 회원정보가 DB에 실제로 존재하는지 확인함.
		String user_accesstoken = jwtUtil.getAccesstoken(request);
		String sender_id = (String) param.get("user_id");
		String receiver_id = (String) param.get("receiver_id");
		
		HashMap user = userDAO.readUserInfo(param);
		
		if(user==null) {
			throw new CustomException(ErrorCode.NOT_FOUND_USER);
		}
		
		//2. 해당 회원으로서 메세지를 송신할 권한이 있는지 판단함.
		String target_user_id = param.get("user_id");
		String jwt_user_id = (String) jwtUtil.getData(user_accesstoken, "user_id");
						
		if(!jwt_user_id.equals(target_user_id)) {
			throw new CustomException(ErrorCode.NOT_AUTHORIZED);
		}
		
		//3. 수신자 회원정보가 정규식에 부합하는지 판단함.
		if(!RegexUtil.checkRegex(receiver_id, RegexUtil.USER_ID_REGEX)) {
			throw new CustomException(ErrorCode.USER_ID_NOT_MATCHED_TO_REGEX);
		}
		
		//4. 수신자 회원정보가 DB에 실제로 존재하는지 확인함.
		param.put("user_id", receiver_id);
		user = userDAO.readUserInfo(param);
		if(user==null) {
			throw new CustomException(ErrorCode.NOT_FOUND_USER);
		}
		
		param.put("sender_id", sender_id);
		param.put("receiver_id", receiver_id);
		
		//5. 메세지 제목이 일정 바이트 이하의 크기인지 판단함.
		String message_title = (String) param.get("message_title");
		if(!RegexUtil.checkBytes(message_title, RegexUtil.MESSAGE_TITLE_MAXBYTES)) {
			throw new CustomException(ErrorCode.MESSAGE_TITLE_EXCEEDED_LIMIT_IN_MAXBYTES);
		}
		
		//6. 메세지 내용이 일정 바이트 이하의 크기인지 판단함.
		String message_content = (String) param.get("message_content");
		if(!RegexUtil.checkBytes(message_content, RegexUtil.MESSAGE_CONTENT_MAXBYTES)) {
			throw new CustomException(ErrorCode.MESSAGE_CONTENT_EXCEEDED_LIMIT_IN_MAXBYTES);
		}
		
		//7. 메세지 송신을 처리함.
		param.put("message_id", UUID.randomUUID().toString());
		param.put("message_sender_id", sender_id);
		param.put("message_receiver_id", receiver_id);
		
		if(messageDAO.createNewMessage(param)!=1) {
			throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR); 
		}
	}

	@Override
	public List<HashMap> readMessages(HttpServletRequest request, HashMap param) {
		//1. 해당 회원정보가 DB에 실제로 존재하는지 확인함.
		HashMap user = userDAO.readUserInfo(param);								
		if(user==null) {
			throw new CustomException(ErrorCode.NOT_FOUND_USER);
		}
				
		//2. 해당 토큰으로 메세지를 조회할 수 있는지 확인함.
		String user_accesstoken = jwtUtil.getAccesstoken(request);
		String jwt_user_id = (String) jwtUtil.getData(user_accesstoken, "user_id");
		String user_id = (String) param.get("user_id");
								
		if(!(jwt_user_id!=null&&jwt_user_id.equals(user_id))) {
			throw new CustomException(ErrorCode.NOT_AUTHORIZED);
		}
		
		//3. 조회하려는 메세지가 송, 수신 메세지중 어떤 메세지인지 판단함.
		String message_type = (String) param.get("message_type");
		if(message_type==null||!(message_type.equals("receive")||message_type.equals("send"))) {
			message_type = "receive";
		}
		
		//4. 메세지 페이징이 유효하지 않으면 0으로 설정함.
		Integer offset = null;
		
		try {
			offset = Integer.valueOf((String) param.get("offset"));
		}catch(Exception e) {
			offset = 0;
		}
		
		if(offset<0) {
			throw new CustomException(ErrorCode.TOO_LOW_MESSAGE_OFFSET);
		}else {
			offset = offset * 10;
			param.put("offset", offset);
		}
		
		//5. 메세지 탐색 시작 날짜의 유효성을 판단함.
		String search_begin_date = (String) param.get("search_begin_date");
		if(search_begin_date!=null&&!RegexUtil.checkRegex(search_begin_date, RegexUtil.DATE_REGEX)) {
			throw new CustomException(ErrorCode.DATE_NOT_MATCHED_TO_REGEX);
		}
		
		//6. 메세지 탐색 종료 날짜의 유효성을 판단함.
		String search_end_date = (String) param.get("search_end_date");
		if(search_end_date!=null&&!RegexUtil.checkRegex(search_end_date, RegexUtil.DATE_REGEX)) {
			throw new CustomException(ErrorCode.DATE_NOT_MATCHED_TO_REGEX);
		}
		
		//7. 메세지를 조회함.
		List<HashMap> list = messageDAO.readMessages(param);
		
		return list;
	}

	@Override
	public void deleteMessages(HttpServletRequest request, HashMap param) {
		//1. 해당 회원정보가 DB에 실제로 존재하는지 확인함.
		HashMap user = userDAO.readUserInfo(param);								
		if(user==null) {
			throw new CustomException(ErrorCode.NOT_FOUND_USER);
		}
						
		//2. 해당 토큰으로 메세지를 삭제할 수 있는지 확인함.
		String user_accesstoken = jwtUtil.getAccesstoken(request);
		String jwt_user_id = (String) jwtUtil.getData(user_accesstoken, "user_id");
		String user_id = (String) param.get("user_id");
										
		if(!(jwt_user_id!=null&&jwt_user_id.equals(user_id))) {
			throw new CustomException(ErrorCode.NOT_AUTHORIZED);
		}
		
		//3. 해당 메세지가 존재하는지 확인함.
		HashMap message = messageDAO.readMessageByMessageId(param);
		if(message==null) {
			throw new CustomException(ErrorCode.NOT_FOUND_MESSAGE);
		}
		
		//4. 해당메세지에서 자신이 송신자인지, 수신자인지를 판단함.
		String message_sender_id = (String) message.get("message_sender_id");
		if(message_sender_id!=null&&message_sender_id.equals(user_id)) {
			param.put("remove_from_send", true);
		}
		
		String message_receiver_id = (String) message.get("message_receiver_id");
		if(message_receiver_id!=null&&message_receiver_id.equals(user_id)) {
			param.put("remove_from_receive", true);
		}
		
		//4. 메세지를 삭제함.
		if(messageDAO.deleteMessage(param)!=1) {
			throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
		}
	}
}