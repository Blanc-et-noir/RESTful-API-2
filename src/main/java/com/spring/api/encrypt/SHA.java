package com.spring.api.encrypt;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class SHA{
	//무작위 솔트값을 생성하는 메소드.
	public static String getSalt() { 
		String salt=""; 
		try { 
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
			byte[] bytes = new byte[16];
			random.nextBytes(bytes);
			salt = new String(Base64.getEncoder().encode(bytes));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace(); 
		}
		return salt; 
	}

	//특정 평문을 솔트값을 추가하여 SHA512 해싱한 결과를 리턴하는 메소드.
	public static String SHA512(String plaintext, String salt) {
		String hash = null;
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-512");
			md.update((plaintext+salt).getBytes());
			hash = String.format("%0128x", new BigInteger(1, md.digest()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return hash;
	}

	//SHA512 해싱을 두 번 적용하는 메소드.
	public static String DSHA512(String plaintext, String salt) {
		return SHA512(SHA512(plaintext,salt),salt);
	}
}