package com.spring.api.controller;

import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.spring.api.exception.CustomException;
import com.spring.api.service.UserService;

@RestController("userController")
public class UserController {
	@Autowired
	private UserService userService;
	
	//1. 공개키 발급
	@RequestMapping(value="/users/publickeys",method=RequestMethod.GET)
	public ResponseEntity<HashMap> createNewUserKeys() throws CustomException, Exception {
		HashMap result = new HashMap();
		result.put("flag", true);
		result.put("content", "공개키 발급에 성공했습니다.");
		result.put("user_publickey", userService.createNewUserKeys());
		return new ResponseEntity<HashMap>(result,HttpStatus.OK);
	}
	
	//2. 회원가입
	@RequestMapping(value="/users",method=RequestMethod.POST)
	public ResponseEntity<HashMap> createNewUserInfo(@RequestBody HashMap param){
		HashMap result = new HashMap();
		userService.createNewUserInfo(param);
		result.put("flag", true);
		result.put("content", "회원가입에 성공했습니다.");
		return new ResponseEntity<HashMap>(result,HttpStatus.CREATED);
	}
	
	//3. 회원정보 조회
	@RequestMapping(value="/users/{user_id}",method=RequestMethod.GET)
	public ResponseEntity<HashMap> readUserInfo(HttpServletRequest request, @PathVariable(value="user_id") String user_id){
		HashMap result = new HashMap();
		HashMap user = userService.readUserInfo(request, user_id);
		result.put("user", user);
		result.put("flag", true);
		result.put("content", "회원정보 조회에 성공했습니다.");
		return new ResponseEntity<HashMap>(result,HttpStatus.OK);
	}
	
	//4. 회원정보 변경
	@RequestMapping(value="/users/{user_id}",method={RequestMethod.PUT})
	public ResponseEntity<HashMap> updateUserInfo(HttpServletRequest request, @RequestBody HashMap param, @PathVariable("user_id") String target_user_id){
		HashMap result = new HashMap();
		param.put("user_id", target_user_id);
		userService.updateUserInfo(request, param);
		result.put("flag", true);
		result.put("content", "회원정보 변경에 성공했습니다.");
		return new ResponseEntity<HashMap>(result,HttpStatus.OK);
	}
	
	//5. 회원탈퇴(미구현)
	@RequestMapping(value="/users",method={RequestMethod.DELETE})
	public ResponseEntity<HashMap> deleteUserInfo(@RequestBody HashMap param){
		HashMap result = new HashMap();
		result.put("flag", true);
		result.put("content", "회원탈퇴에 성공했습니다.");
		return new ResponseEntity<HashMap>(result,HttpStatus.OK);
	}
	
	//6. 도서대출
	@RequestMapping(value="/users/{user_id}/checkouts",method={RequestMethod.POST})
	public ResponseEntity<HashMap> createCheckoutInfo(HttpServletRequest request, @RequestBody HashMap param, @PathVariable("user_id") String user_id){
		HashMap result = new HashMap();
		param.put("user_id", user_id);
		userService.createCheckoutInfo(request, param);
		result.put("flag", true);
		result.put("content", "도서 대출에 성공했습니다.");
		return new ResponseEntity<HashMap>(result,HttpStatus.CREATED);
	}
	
	//7. 도서반납
	@RequestMapping(value="/users/{user_id}/checkouts/{checkout_id}",method={RequestMethod.DELETE})
	public ResponseEntity<HashMap> deleteCheckoutInfo(HttpServletRequest request, @PathVariable("user_id") String user_id, @PathVariable("checkout_id") String checkout_id){
		HashMap result = new HashMap();
		HashMap param = new HashMap();
		param.put("user_id", user_id);
		param.put("checkout_id", checkout_id);
		userService.deleteCheckoutInfo(request, param);
		result.put("flag", true);
		result.put("content", "도서 반납에 성공했습니다.");
		return new ResponseEntity<HashMap>(result,HttpStatus.OK);
	}
	
	//8. 도서예약
	@RequestMapping(value="/users/{user_id}/reservations",method={RequestMethod.POST})
	public ResponseEntity<HashMap> createNewReservationInfo(HttpServletRequest request, @RequestBody HashMap param, @PathVariable("user_id") String user_id){
		HashMap result = new HashMap();
		param.put("user_id", user_id);
		userService.createNewReservationInfo(request, param);
		result.put("flag", true);
		result.put("content", "도서 예약에 성공했습니다.");
		return new ResponseEntity<HashMap>(result,HttpStatus.OK);
	}
	
	//8. 도서예약
	@RequestMapping(value="/users/{user_id}/reservations/{reservation_id}",method={RequestMethod.DELETE})
	public ResponseEntity<HashMap> deleteReservationInfo(HttpServletRequest request, @PathVariable("user_id") String user_id, @PathVariable("reservation_id") String reservation_id){
		HashMap result = new HashMap();
		HashMap param = new HashMap();
		param.put("user_id", user_id);
		param.put("reservation_id", reservation_id);
		userService.deleteReservationInfo(request, param);
		result.put("flag", true);
		result.put("content", "도서 예약 취소에 성공했습니다.");
		return new ResponseEntity<HashMap>(result,HttpStatus.OK);
	}
	
	//9. 대출현황 조회
	@RequestMapping(value="/users/{user_id}/checkouts",method={RequestMethod.GET})
	public ResponseEntity<HashMap> readCheckoutInfo(HttpServletRequest request,@RequestBody HashMap param, @PathVariable("user_id") String user_id){
		HashMap result = new HashMap();
		param.put("user_id", user_id);
		List<HashMap> list = userService.readCheckoutInfo(request, param);
		result.put("checkouts", list);
		result.put("flag", true);
		result.put("content", "도서 대출 조회에 성공했습니다.");
		return new ResponseEntity<HashMap>(result,HttpStatus.OK);
	}
	
	//10. 예약현황 조회
	@RequestMapping(value="/users/{user_id}/reservations",method={RequestMethod.GET})
	public ResponseEntity<HashMap> readReservationInfo(HttpServletRequest request,@RequestBody HashMap param, @PathVariable("user_id") String user_id){
		HashMap result = new HashMap();
		param.put("user_id", user_id);
		List<HashMap> list = userService.readReservationInfo(request, param);
		result.put("reservations", list);
		result.put("flag", true);
		result.put("content", "도서 예약 조회에 성공했습니다.");
		return new ResponseEntity<HashMap>(result,HttpStatus.OK);
	}
}