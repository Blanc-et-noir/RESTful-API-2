package com.spring.api.code;

public enum PointCode {
	//포인트 부여
	RETURN_BOOK_WITHIN_7DAYS(1,2,"7일 이상 대출한 도서 정상 반납 보너스");
	
	private int POINT_CODE;
	private int POINT_AMOUNT;
	private String POINT_MESSAGE;
	
	public int getPOINT_AMOUNT() {
		return POINT_AMOUNT;
	}
	
	public void setPOINT_AMOUNT(int POINT_AMOUNT) {
		this.POINT_AMOUNT = POINT_AMOUNT;
	}
	
	public int getPOINT_CODE() {
		return POINT_CODE;
	}
	
	public void setPOINT_CODE(int POINT_CODE) {
		this.POINT_CODE = POINT_CODE;
	}
	
	public String getPOINT_MESSAGE() {
		return POINT_MESSAGE;
	}
	
	public void setPOINT_MESSAGE(String POINT_MESSAGE) {
		this.POINT_MESSAGE = POINT_MESSAGE;
	}

	PointCode(int POINT_CODE,int POINT_AMOUNT, String POINT_MESSAGE){
		this.POINT_CODE = POINT_CODE;
		this.POINT_AMOUNT = POINT_AMOUNT;
		this.POINT_MESSAGE = POINT_MESSAGE;
	}
}