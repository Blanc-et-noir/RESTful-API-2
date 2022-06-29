package com.spring.api.dao;

import java.util.HashMap;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository("tokenDAO")
public class TokenDAO {
	@Autowired
	private SqlSession sqlSession;
	
	public HashMap getUserInfoByUserId(HashMap param) {
		return sqlSession.selectOne("tokens.getUserInfoByUserId", param);
	}
	
	public int updateUserTokens(HashMap param) {
		return sqlSession.update("tokens.updateUserTokens", param);
	}

	public HashMap getUserTokensByUserId(HashMap param) {
		return sqlSession.selectOne("tokens.getUserTokensByUserId", param);
	}

	public int deleteUserTokens(HashMap param) {
		return sqlSession.update("tokens.deleteUserTokens", param);		
	}
}