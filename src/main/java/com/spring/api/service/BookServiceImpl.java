package com.spring.api.service;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartRequest;

import com.spring.api.code.ErrorCode;
import com.spring.api.dao.BookDAO;
import com.spring.api.exception.CustomException;
import com.spring.api.util.JwtUtil;
import com.spring.api.util.RegexUtil;

@Transactional(rollbackFor= {
		CustomException.class,
		RuntimeException.class,
		Exception.class
})
@Service("bookService")
public class BookServiceImpl implements BookService{
	@Autowired
	private JwtUtil jwtUtil;
	@Autowired
	private BookDAO bookDAO;
	private final static String BOOK_IMAGES_BASE_PATH = "c:"+File.separator+"api"+File.separator+"book_images"+File.separator;
	@Override
	public void createNewBookInfo(MultipartRequest mRequest, HttpServletRequest request){	
		//1. 해당 사용자가 권한이 있는지 확인
		String user_accesstoken = jwtUtil.getAccesstoken(mRequest);
		String user_id = (String) jwtUtil.getData(user_accesstoken, "user_id");
		Integer user_type_id = (Integer) jwtUtil.getData(user_accesstoken, "user_type_id");
		HashMap param = new HashMap();
		param.put("user_id", user_id);
		HashMap user = bookDAO.findUserInfoByUserId(param);
		
		if(user == null) {
			throw new CustomException(ErrorCode.NOT_FOUND_USER);
		}
		
		if(user_type_id != 0) {
			throw new CustomException(ErrorCode.NOT_AUTHORIZED);
		}
		
		//2. ISBN 코드의 유효성을 판단함
		String book_isbn = request.getParameter("book_isbn");
		if(!RegexUtil.checkRegex(book_isbn, RegexUtil.BOOK_ISBN_REGEX)) {
			throw new CustomException(ErrorCode.BOOK_ISBN_NOT_MATCHED_TO_REGEX);
		}else {
			param.put("book_isbn", book_isbn);
			HashMap book = bookDAO.findBookInfo(param);
			if(book != null) {
				throw new CustomException(ErrorCode.DUPLICATE_BOOK_ISBN);
			}
		}
		
		//3. 도서 제목의 유효성을 판단함.
		String book_name = request.getParameter("book_name");
		if(!RegexUtil.checkBytes(book_name, RegexUtil.BOOK_NAME_MAXBYTES)) {
			throw new CustomException(ErrorCode.BOOK_NAME_EXCEEDED_LIMIT_ON_MAXBYTES);
		}
		
		//4. 출판사 이름의 유효성을 판단함.
		String book_publisher = request.getParameter("book_publisher");
		if(!RegexUtil.checkRegex(book_publisher, RegexUtil.BOOK_PUBLISHER_NAME_REGEX)) {
			throw new CustomException(ErrorCode.PUBLISHER_NAME_NOT_MATCHED_TO_REGEX);
		}
		
		//6. 재고의 유효성을 판단함.
		Integer book_quantity;
		try{
			book_quantity = Integer.parseInt(request.getParameter("book_quantity"));
			if(book_quantity<0) {
				throw new CustomException(ErrorCode.TOO_FEW_BOOK_QUANTITY);
			}
		}catch(Exception e) {
			throw new CustomException(ErrorCode.BOOK_QUANTITY_NOT_MATCHED_TO_REGEX);
		}
		
		//7. 도서 장르의 유효성을 판단함.
		String book_type_id = request.getParameter("book_type_id");
		if(!RegexUtil.checkRegex(book_type_id, RegexUtil.UUID_REGEX)) {
			throw new CustomException(ErrorCode.UUID_NOT_MATCHED_TO_REGEX);
		}
		
		param.put("book_type_id", book_type_id);
		HashMap bookType = bookDAO.findBookType(param);
		
		if(bookType == null) {
			throw new CustomException(ErrorCode.NOT_FOUND_BOOK_TYPE);
		}
		
		//4. 저자 이름의 유효성을 판단함.
		String[] authors = request.getParameterValues("book_authors");
				
		if(authors == null||authors.length==0) {
			throw new CustomException(ErrorCode.TOO_FEW_AUTHORS);
		}else if(authors.length>10) {
			throw new CustomException(ErrorCode.TOO_MANY_AUTHORS);
		}
		
		String book_authors = "";

		for(int i=0; i<authors.length; i++) {
			if(!RegexUtil.checkRegex(authors[i], RegexUtil.BOOK_AUTHOR_NAME_REGEX)) {
				throw new CustomException(ErrorCode.AUTHOR_NAME_NOT_MATCHED_TO_REGEX);
			}
			if(i!=authors.length-1) {
				book_authors+=authors[i]+" ";
			}else {
				book_authors+=authors[i];
			}
		}
		
		//5. 번역자 이름의 유효성을 판단함.
		String[] translators = request.getParameterValues("book_translators");
		String book_translators = "";
		
		if(translators != null) {
			for(int i=0; i<translators.length; i++) {
				if(!RegexUtil.checkRegex(translators[i], RegexUtil.BOOK_TRANSLATOR_NAME_REGEX)) {
					throw new CustomException(ErrorCode.TRANSLATOR_NAME_NOT_MATCHED_TO_REGEX);
				}
				if(i!=authors.length-1) {
					book_translators+=translators[i]+" ";
				}else {
					book_translators+=translators[i];
				}
			}
		}

		//8. 도서 정보를 추가함.
		param = new HashMap();
		param.put("book_isbn", book_isbn);
		param.put("book_name", book_name);
		param.put("book_publisher", book_publisher);
		param.put("book_type_id", book_type_id);
		param.put("book_quantity", book_quantity);
		param.put("book_authors", book_authors);
		
		if(translators!=null) {
			param.put("book_translators", book_translators);
		}else {
			param.put("book_translators", null);
		}
		
		String book_date = request.getParameter("book_date");
		
		param.put("book_date", book_date);
		
		int row = bookDAO.createNewBookInfo(param);
		
		if(row!=1) {
			System.out.println(row+"로우");
			throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
		}

		//9. 도서 이미지 정보를 추가함.
		List<MultipartFile> mfiles = mRequest.getFiles("book_images");
		List<HashMap> list = new LinkedList<HashMap>();
		
		Iterator<MultipartFile> itor = mfiles.iterator();
		
		while(itor.hasNext()) {
			MultipartFile mfile = itor.next();
			String book_extension = StringUtils.getFilenameExtension(mfile.getOriginalFilename());
			
			HashMap hm = new HashMap();
			hm.put("book_isbn", book_isbn);
			hm.put("book_image_id", UUID.randomUUID().toString());
			hm.put("book_image_extension", book_extension);
			hm.put("book_image_file", mfile);
			
			list.add(hm);			
		}
		
		if(list.size()>0) {
			param = new HashMap();
			param.put("list", list);
			
			row = bookDAO.createNewBookImageInfo(param);
		}
		
		//10. 실제로 이미지 파일을 저장함.
		Iterator<HashMap> itr = list.iterator();
		
		try {
			while(itr.hasNext()) {
				HashMap hm = itr.next();
				MultipartFile mfile = (MultipartFile) hm.get("book_image_file");
				File file = new File(BOOK_IMAGES_BASE_PATH+hm.get("book_isbn")+File.separator+hm.get("book_image_id")+"."+hm.get("book_image_extension"));
				if(!file.exists()) {
					file.mkdirs();
				}
				mfile.transferTo(file);
			}
		}catch(Exception e) {
			e.printStackTrace();
			throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
		}
	}
	
	@Override
	public List readBookTypes() {
		return bookDAO.readBookTypes();
	}
	
	@Override
	public void createNewBookTypes(HttpServletRequest request, HashMap param) {
		//1. 해당 사용자가 권한이 있는지 확인
		String user_accesstoken = jwtUtil.getAccesstoken(request);
		String user_id = (String) jwtUtil.getData(user_accesstoken, "user_id");
		Integer user_type_id = (Integer) jwtUtil.getData(user_accesstoken, "user_type_id");
		param.put("user_id", user_id);
		
		HashMap user = bookDAO.findUserInfoByUserId(param);
		
		if(user == null) {
			throw new CustomException(ErrorCode.NOT_FOUND_USER);
		}
				
		if(user_type_id != 0) {
			throw new CustomException(ErrorCode.NOT_AUTHORIZED);
		}
		
		//2. 도서 장르 이름의 유효성을 판단함.
		String book_type_content = (String) param.get("book_type_content");
		if(!RegexUtil.checkBytes(book_type_content,RegexUtil.BOOK_TYPE_CONTENT_MAXBYTES)) {
			throw new CustomException(ErrorCode.BOOK_CONTENT_TYPE_EXCEEDED_LIMIT_IN_MAXBYTES);
		}
		
		//3. 도서 장르를 새로 추가함.
		param.put("book_type_id", UUID.randomUUID().toString());
		
		if(bookDAO.createNewBookTypes(param)!=1) {
			throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public void updateBookTypes(HttpServletRequest request, HashMap param) {
		//1. 해당 사용자가 권한이 있는지 확인
		String user_accesstoken = jwtUtil.getAccesstoken(request);
		String user_id = (String) jwtUtil.getData(user_accesstoken, "user_id");
		Integer user_type_id = (Integer) jwtUtil.getData(user_accesstoken, "user_type_id");
		param.put("user_id", user_id);
				
		HashMap user = bookDAO.findUserInfoByUserId(param);
				
		if(user == null) {
			throw new CustomException(ErrorCode.NOT_FOUND_USER);
		}
						
		if(user_type_id != 0) {
			throw new CustomException(ErrorCode.NOT_AUTHORIZED);
		}
		
		//2. 도서 장르 이름의 유효성을 판단함.
		String book_type_content = (String) param.get("book_type_content");
		if(!RegexUtil.checkBytes(book_type_content,RegexUtil.BOOK_TYPE_CONTENT_MAXBYTES)) {
			throw new CustomException(ErrorCode.BOOK_CONTENT_TYPE_EXCEEDED_LIMIT_IN_MAXBYTES);
		}
		
		//3. 도서 장르가 존재하는지 판단함.
		HashMap book_type = bookDAO.findBookType(param);
		if(book_type==null) {
			throw new CustomException(ErrorCode.NOT_FOUND_BOOK_TYPE);
		}
		
		//4. 도서 장르를 수정함.
		if(bookDAO.updateBookTypes(param)!=1) {
			throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public void deleteBookTypes(HttpServletRequest request, HashMap param) {
		//1. 해당 사용자가 권한이 있는지 확인
		String user_accesstoken = jwtUtil.getAccesstoken(request);
		String user_id = (String) jwtUtil.getData(user_accesstoken, "user_id");
		Integer user_type_id = (Integer) jwtUtil.getData(user_accesstoken, "user_type_id");
		param.put("user_id", user_id);
						
		HashMap user = bookDAO.findUserInfoByUserId(param);
						
		if(user == null) {
			throw new CustomException(ErrorCode.NOT_FOUND_USER);
		}
								
		if(user_type_id != 0) {
			throw new CustomException(ErrorCode.NOT_AUTHORIZED);
		}
		
		//2. 도서 장르가 존재하는지 판단함.
		HashMap book_type = bookDAO.findBookType(param);
		if(book_type==null) {
			throw new CustomException(ErrorCode.NOT_FOUND_BOOK_TYPE);
		}

		//3. 도서 장르를 삭제함.
		if(bookDAO.deleteBookTypes(param)!=1) {
			throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
		}
	}
}