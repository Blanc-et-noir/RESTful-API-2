package com.spring.api.dao;

import java.util.HashMap;
import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository("messageDAO")
public class MessageDAO {
	@Autowired
	SqlSession sqlSession;

	public int createNewMessage(HashMap param) {
		return sqlSession.insert("messages.createNewMessage", param);
	}

	public List<HashMap> readMessages(HashMap param) {
		return sqlSession.selectList("messages.readMessages",param);
	}

	public HashMap readMessageByMessageId(HashMap param) {
		return sqlSession.selectOne("messages.readMessageByMessageId",param);
	}

	public int deleteMessage(HashMap param) {
		return sqlSession.delete("messages.deleteMessage",param);
	}

	public int getMessageTotal(HashMap param) {
		return sqlSession.selectOne("messages.getMessageTotal", param);
	}
}