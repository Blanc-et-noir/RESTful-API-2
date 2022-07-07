package com.spring.api.dao;

import java.util.HashMap;
import java.util.List;

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
	
	public List<HashMap> readCheckOutInfosByUserId(HashMap param) {
		return sqlSession.selectList("users.readCheckOutInfosByUserId", param);
	}
	
	public HashMap readCheckOutInfoByUserId(HashMap param) {
		return sqlSession.selectOne("users.readCheckOutInfoByUserId", param);
	}
	
	public int updateUserCheckOutDate(HashMap param) {
		return sqlSession.update("users.updateUserCheckOutDate", param);
	}
	
	public int returnCheckOut(HashMap param) {
		return sqlSession.update("users.returnCheckOut", param);
	}
	
	public int increaseUserPoint(HashMap param) {
		return sqlSession.update("users.increaseUserPoint", param);
	}
	
	public int createNewPointInfo(HashMap param) {
		return sqlSession.insert("users.createNewPointInfo", param);
	}
	
	public HashMap readTodayLoginPoint(HashMap param) {
		return sqlSession.selectOne("users.readTodayLoginPoint",param);
	}
	
	public List<HashMap> readReservationInfosByBookIsbn(HashMap param) {
		return sqlSession.selectList("users.readReservationInfosByBookIsbn", param);
	}
	
	public List<HashMap> readReservationInfosByUserId(HashMap param) {
		return sqlSession.selectList("users.readReservationInfosByUserId", param);
	}
	
	public int createNewReservationInfo(HashMap param) {
		return sqlSession.insert("users.createNewReservationInfo", param);
	}
	
	public int deleteReservation(HashMap param) {
		return sqlSession.delete("users.deleteReservation",param);		
	}
	
	public int createCheckoutInfo(HashMap param) {
		return sqlSession.insert("users.createCheckoutInfo",param);
	}

	public HashMap readReservationInfoByReservationId(HashMap param) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<HashMap> readCheckOutInfosWithOptions(HashMap param) {
		return sqlSession.selectList("users.readCheckOutInfosWithOptions", param);
	}

	public List<HashMap> readReservationInfosWithOptions(HashMap param) {
		return sqlSession.selectList("users.readReservationInfosWithOptions", param);
	}
}