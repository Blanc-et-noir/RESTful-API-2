package com.spring.api.dao;

import java.util.HashMap;
import java.util.List;

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

	public List readBookTypes() {
		return sqlSession.selectList("books.readBookTypes");
	}

	public HashMap readBookInfo(HashMap param) {
		return sqlSession.selectOne("books.readBookInfo",param);
	}

	public int decreaseBookQuantity(HashMap param) {
		return sqlSession.update("books.decreaseBookQuantity",param);	
	}

	public int increaseBookQuantity(HashMap param) {
		return sqlSession.update("books.increaseBookQuantity",param);	
	}

	public int createNewBookTypes(HashMap param) {
		return sqlSession.insert("books.createNewBookTypes",param);
	}

	public int updateBookTypes(HashMap param) {
		return sqlSession.update("books.updateBookTypes",param);
	}

	public int deleteBookTypes(HashMap param) {
		return sqlSession.delete("books.deleteBookTypes",param);
	}
}