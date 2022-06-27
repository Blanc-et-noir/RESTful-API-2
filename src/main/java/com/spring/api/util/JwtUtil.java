package com.spring.api.util;

import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.spring.api.vo.UserVO;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Component
public class JwtUtil {
	private static String privatekey;
	
	public static final long refreshtokenMaxAge = 14*24*60*60*1000;
	public static final long accesstokenMaxAge = 2*60*60*1000;
	public static final int privateKeyMaxAge = 3*60*1000;
	
	@Value("${jwt.privatekey}")
	public void setPrivatekey(String privatekey) {
		this.privatekey = privatekey;
	}
	
	public static String getAccesstoken(HttpServletRequest request) {
		return request.getHeader("user_accesstoken");
	};
	
	public static String getRefreshtoken(HttpServletRequest request) {
		return request.getHeader("user_refreshtoken");
	};
	
	public static void setAccesstoken(HttpServletResponse response, String user_accesstoken) {
		response.setHeader("user_accesstoken", user_accesstoken);
	};
	
	public static void setRefreshtoken(HttpServletResponse response, String user_refreshtoken) {
		response.setHeader("user_refreshtoken", user_refreshtoken);
	};
	
	//����� ������ �������� ��ū�� ������.
	public static String createToken(UserVO user, long age) {
		String secretKey = Base64.getEncoder().encodeToString(privatekey.getBytes());
		
		//1. JWT ��ū�� ��� ������ ������.
		Map<String,Object> headers = new HashMap<String,Object>();
		headers.put("typ", "JWT");
		headers.put("alg", "HS256");
		
		//2. ����� ������ �ش� ��ū�� �߰���. ��, �ΰ��� ������ ��ū�� �߰����� �ʵ��� ��
		Map<String,Object> claims = new HashMap<String,Object>();
		claims.put("user_id", user.getUser_id());
		claims.put("user_name", user.getUser_name());
		claims.put("user_type_id", user.getUser_type_id());
		
		//3. ��ū�� ��ȿ�ð��� ������
		Date now = new Date();
		Date exp = new Date(now.getTime()+age);

		//4. ������ ���, Ŭ���� ������ �������� ���ο� JWT ��ū�� ������.
		return Jwts.builder()
				.setHeader(headers)
				.setClaims(claims)
				.setSubject("user-auth")
				.setIssuedAt(now)
				.setExpiration(exp)
				.signWith(SignatureAlgorithm.HS256, secretKey)
				.compact();
	}
	
	//Ŭ���̾�Ʈ�κ��� ���޹��� �ش� ��ū�� �����Ǿ����� �ƴ��� �Ǵ���.
	public static void validateToken(String token){
		String secretKey = Base64.getEncoder().encodeToString(privatekey.getBytes());
		
		//1. �ش� JWT ��ū�� ������ �ִ��� �Ǵ���.
		Claims claims = null;
		claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
	}
	
	//Ŭ���̾�Ʈ�κ��� ���޹��� �ش� ��ū�� ����� ������ ����.
    public static Map<String,Object> getInfo(String token){
    	String secretKey = Base64.getEncoder().encodeToString(privatekey.getBytes());
    	
    	//1. �ش� JWT ��ū���κ��� ����� ������ ����.
    	try {
    		Jws<Claims> claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
    		return claims.getBody();
    	//2. ��Ÿ ���ܰ� �߻��� ���.
    	}catch(Exception e) {
    		return null;
    	}   	
    }
    
    //Ŭ���̾�Ʈ�κ��� ���޹��� �ش� ��ū�� ����� ������ ����.
    public static Object getData(String token, String data){
    	String secretKey = Base64.getEncoder().encodeToString(privatekey.getBytes());
    	
    	//1. �ش� JWT ��ū���κ��� ����� ������ ����.
    	try {
    		Jws<Claims> claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
    		return claims.getBody().get(data);
    	//2. �ش� JWT ��ū�� ��ȿ�Ⱓ�� ����� ����� ������ ����.
    	}catch(ExpiredJwtException e) {
    		return e.getClaims().get(data);
    	//3. ��Ÿ ���ܰ� �߻��� ���.
    	}catch(Exception e) {
    		return null;
    	}
    }
    
    //Ŭ���̾�Ʈ�κ��� ���޹��� �ش� ��ū�� ���� ��ȿ�ð��� ����.
    public static Long getExpiration(String token) {
    	String secretKey = Base64.getEncoder().encodeToString(privatekey.getBytes());
    	
    	//1. �ش� ��ū�� ���� ��ȿ�ð��� ����.
    	try {
            Date expiration = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody().getExpiration();
            Long now = new Date().getTime();
            return (expiration.getTime() - now);
        //2. ��Ÿ ���ܰ� �߻��� ���.
    	}catch(Exception e) {
    		return 1L;
    	}
    }
}