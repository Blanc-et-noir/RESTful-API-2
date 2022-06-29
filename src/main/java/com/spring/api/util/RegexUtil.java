package com.spring.api.util;

import org.springframework.stereotype.Component;

@Component
public class RegexUtil {
	public final static String USER_ID_REGEX = "^[a-zA-Z]{1}[a-zA-Z0-9_]{7,15}$";
	public final static String USER_PW_REGEX = "[a-zA-Z0-9_]{8,16}$";
	public final static String USER_PHONE_REGEX = "\\d{3}-\\d{4}-\\d{4}";
	public final static String UUID_REGEX = "[0-9a-fA-F]{8}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{12}";
	public final static String USER_NAME_REGEX = "^[°¡-ÆR]{2,4}$";
	public final static int QUESTION_ANSWER_MAXBYTES = 512;
	
	
	public static boolean checkBytes(String str, final int maxLength) {
		if(str==null) {
			return false;
		}
		if(str.getBytes().length<=maxLength) {
			return true;
		}else {
			return false;
		}
	}
	
	public static boolean checkRegex(String str, String regex) {
		if(str == null) {
			return false;
		}else if(str.matches(regex)) {
			return true;
		}else {
			return false;
		}
	}
}