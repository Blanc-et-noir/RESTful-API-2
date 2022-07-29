package com.spring.api.util;

//Á¤±Ô½Ä¿¡ ºÎÇÕÇÏ´ÂÁö ÆÇ´ÜÇÒ¶§ »ç¿ëÇÒ À¯Æ¿ Å¬·¡½º
public class RegexUtil {
	//»ç¿ëÀÚ ¾ÆÀÌµğ´Â 8~16ÀÚÀÇ ¿µ¾î ¶Ç´Â ¼ıÀÚ·Î ±¸¼ºÇØ¾ßÇÔ
	public final static String USER_ID_REGEX = "^[a-zA-Z]{1}[a-zA-Z0-9_]{7,15}$";
	//»ç¿ëÀÚ ºñ¹Ğ¹øÈ£´Â ¿µ¾î ¶Ç´Â ¼ıÀÚ·Î 8~16ÀÚ·Î ±¸¼ºÇØ¾ßÇÔ
	public final static String USER_PW_REGEX = "[a-zA-Z0-9_]{8,16}$";
	//»ç¿ëÀÚ ÀüÈ­¹øÈ£ Çü½ÄÀº 000-0000-0000ÀÌ¾î¾ßÇÔ
	public final static String USER_PHONE_REGEX = "\\d{3}-\\d{4}-\\d{4}";
	//UUIDÇü½ÄÀº 8-4-4-4-12 ÀÚ¸®ÀÇ ¿µ¾î ¶Ç´Â ¼ıÀÚ·Î ±¸¼ºµÇ¾î¾ßÇÔ
	public final static String UUID_REGEX = "[0-9a-fA-F]{8}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{12}";
	//»ç¿ëÀÚ ÀÌ¸§Àº 2~5ÀÚ¸®ÀÇ ÇÑ±ÛÀÌ¾î¾ßÇÔ
	public final static String USER_NAME_REGEX = "^[°¡-ÆR]{2,5}$";
	//¹ø¿ªÀÚÀÇ ÀÌ¸§Àº ¿µ¾î ¶Ç´Â ÇÑ±Û·Î 1~20ÀÚ¿©¾ßÇÔ
	public final static String BOOK_TRANSLATOR_NAME_REGEX = "^[a-zA-Z°¡-ÆR]{1,20}";
	//ÀÛ°¡ ÀÌ¸§Àº ¿µ¾î ¶Ç´Â ÇÑ±Û·Î 1~20ÀÚ¿©¾ßÇÔ
	public final static String BOOK_AUTHOR_NAME_REGEX = "^[a-zA-Z°¡-ÆR]{1,20}";
	//µµ¼­ ISBNÄÚµå´Â -¾øÀÌ ¼ıÀÚ·Î¸¸ 13ÀÚ¸®¿©¾ßÇÔ
	public final static String BOOK_ISBN_REGEX = "^[0-9]{13,13}";
	//ÃâÆÇ»ç ÀÌ¸§Àº 1~20ÀÚ¸®ÀÇ ¿µ¾î ¶Ç´Â ÇÑ±ÛÀÌ¾î¾ßÇÔ
	public final static String BOOK_PUBLISHER_NAME_REGEX = "^[a-zA-Z°¡-ÆR]{1,20}";
	//³¯Â¥Çü½ÄÀº YYYY-MM-DDÇüÅÂ¿©¾ßÇÔ
	public final static String DATE_REGEX = "^([12]\\d{3}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01]))$";
	
	//ºñ¹Ğ¹øÈ£ Ã£±â Áú¹®ÀÇ ´äÀº 512¹ÙÀÌÆ®ÀÌÇÏ¿©¾ßÇÔ
	public final static int QUESTION_ANSWER_MAXBYTES = 512;
	//µµ¼­ ÀÌ¸§Àº 120¹ÙÀÌÆ® ÀÌÇÏ¿©¾ßÇÔ
	public final static int BOOK_NAME_MAXBYTES = 120;
	//µµ¼­ Àå¸£¸íÀº 60¹ÙÀÌÆ® ÀÌÇÏ¿©¾ßÇÔ
	public final static int BOOK_TYPE_CONTENT_MAXBYTES = 60;
	//¸Ş¼¼Áö ³»¿ëÀº 900¹ÙÀÌÆ® ÀÌÇÏ¿©¾ßÇÔ
	public final static int MESSAGE_CONTENT_MAXBYTES = 900;
	//¸Ş¼¼Áö Á¦¸ñÀº 300¹ÙÀÌÆ® ÀÌÇÏ¿©¾ßÇÔ
	public final static int MESSAGE_TITLE_MAXBYTES = 300;
	
	//ÇØ´ç ¹®ÀÚ¿­ÀÌ Æ¯Á¤ ¼öÄ¡ ÀÌÇÏÀÇ ¹ÙÀÌÆ® Å©±â¸¦ °®´ÂÁö ÆÇ´ÜÇÏ´Â ¸Ş¼Òµå
	//nullÀÌ°Å³ª Æ¯Á¤ ¹ÙÀÌÆ® ÀÌ»óÀÌ¶ó¸é false ¾Æ´Ï¶ó¸é trueÀÓ
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
	
	//ÇØ´ç ¹®ÀÚ¿­ÀÌ Á¤±Ô½Ä¿¡ ºÎÇÕÇÏ´ÂÁö ÆÇ´ÜÇÏ´Â ¸Ş¼Òµå
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