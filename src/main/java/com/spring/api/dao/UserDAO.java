package com.spring.api.dao;

import java.util.HashMap;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository("userDAO")
public class UserDAO {
	@Autowired
	private SqlSession sqlSession;
	public HashMap findUserInfoByUserId(HashMap param) {
		return sqlSession.selectOne("users.findUserInfoByUserId", param);
	}
	public HashMap findUserInfoByUserPhone(HashMap param) {
		return sqlSession.selectOne("users.findUserInfoByUserPhone", param);
	}
	public int createNewUserInfo(HashMap param) {
		return sqlSession.insert("users.createNewUserInfo", param);
	}
	public HashMap<String, String> readUserInfo(HashMap param) {
		return sqlSession.selectOne("users.readUserInfo", param);
	}
	public String findUserSaltByUserId(HashMap param) {
		return sqlSession.selectOne("users.findUserSaltByUserId", param);
	}
	public int updateUserInfo(HashMap<String, String> param) {
		return sqlSession.update("users.updateUserInfo", param);
	}
}