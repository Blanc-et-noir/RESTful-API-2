package com.spring.api.dao;

import java.util.HashMap;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository("bookDAO")
public class BookDAO {
	@Autowired
	private SqlSession sqlSession;

	public int createNewBookInfo(HashMap param) {
		return sqlSession.insert("books.createNewBookInfo", param);		
	}

	public int createNewBookImageInfo(HashMap param) {
		return sqlSession.insert("books.createNewBookImageInfo", param);
	}

	public HashMap findUserInfoByUserId(HashMap param) {
		return sqlSession.selectOne("books.findUserInfoByUserId", param);
	}

	public HashMap findBookType(HashMap param) {
		return sqlSession.selectOne("books.findBookType", param);
	}

	public int createNewBookAuthorInfo(HashMap param) {
		return sqlSession.insert("books.createNewBookAuthorInfo", param);
	}

	public int createNewBookTranslatorInfo(HashMap param) {
		return sqlSession.insert("books.createNewBookTranslatorInfo", param);
	}

	public HashMap findBookInfo(HashMap param) {
		return sqlSession.selectOne("books.findBookInfo", param);
	}
}