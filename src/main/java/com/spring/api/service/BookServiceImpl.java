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

import com.spring.api.dao.BookDAO;
import com.spring.api.exception.books.AuthorNameNotMatchedToRegexException;
import com.spring.api.exception.books.BookIsbnNotMatchedToRegexException;
import com.spring.api.exception.books.BookNameExceededLimitOnMaxbytesException;
import com.spring.api.exception.books.BookQuantityNotMatchedToRegexException;
import com.spring.api.exception.books.DuplicateBookIsbnException;
import com.spring.api.exception.books.NotFoundBookTypeException;
import com.spring.api.exception.books.PublisherNameNotMatchedToRegexException;
import com.spring.api.exception.books.TooFewAuthorsException;
import com.spring.api.exception.books.TooFewBookQuantityException;
import com.spring.api.exception.books.TranslatorNameNotMatchedToRegexException;
import com.spring.api.exception.users.NotAuthorizedException;
import com.spring.api.exception.users.NotFoundUserException;
import com.spring.api.exception.users.UUIDNotMatchedToRegexException;
import com.spring.api.util.JwtUtil;
import com.spring.api.util.RegexUtil;

@Transactional(rollbackFor= {
		DuplicateBookIsbnException.class,
		TooFewAuthorsException.class,
		AuthorNameNotMatchedToRegexException.class, 
		BookQuantityNotMatchedToRegexException.class, 
		NotAuthorizedException.class, 
		NotFoundUserException.class,
		BookIsbnNotMatchedToRegexException.class, 
		BookNameExceededLimitOnMaxbytesException.class, 
		PublisherNameNotMatchedToRegexException.class, 
		TranslatorNameNotMatchedToRegexException.class,
		UUIDNotMatchedToRegexException.class, 
		NotFoundBookTypeException.class,
		Exception.class
})
@Service("bookService")
public class BookServiceImpl implements BookService{
	@Autowired
	private BookDAO bookDAO;
	private final static String BOOK_IMAGES_BASE_PATH = "c:"+File.separator+"api"+File.separator+"book_images"+File.separator;
	@Override
	public void createNewBookInfo(MultipartRequest mRequest, HttpServletRequest request) throws 
		TooFewAuthorsException, 
		AuthorNameNotMatchedToRegexException, 
		BookQuantityNotMatchedToRegexException, 
		NotAuthorizedException, 
		NotFoundUserException, 
		BookIsbnNotMatchedToRegexException, 
		BookNameExceededLimitOnMaxbytesException, 
		PublisherNameNotMatchedToRegexException, 
		TranslatorNameNotMatchedToRegexException, 
		UUIDNotMatchedToRegexException, 
		NotFoundBookTypeException, 
		Exception
	{	
		//1. 해당 사용자가 권한이 있는지 확인
		String user_accesstoken = JwtUtil.getAccesstoken(mRequest);
		String user_id = (String) JwtUtil.getData(user_accesstoken, "user_id");
		Integer user_type_id = (Integer) JwtUtil.getData(user_accesstoken, "user_type_id");
		HashMap param = new HashMap();
		param.put("user_id", user_id);
		HashMap user = bookDAO.findUserInfoByUserId(param);
		
		if(user == null) {
			throw new NotFoundUserException();
		}
		
		if(user_type_id != 0) {
			throw new NotAuthorizedException();
		}
		
		//2. ISBN 코드의 유효성을 판단함
		String book_isbn = request.getParameter("book_isbn");
		if(!RegexUtil.checkRegex(book_isbn, RegexUtil.BOOK_ISBN_REGEX)) {
			throw new BookIsbnNotMatchedToRegexException();
		}else {
			param.put("book_isbn", book_isbn);
			HashMap book = bookDAO.findBookInfo(param);
			if(book != null) {
				throw new DuplicateBookIsbnException();
			}
		}
		
		//3. 도서 제목의 유효성을 판단함.
		String book_name = request.getParameter("book_name");
		if(!RegexUtil.checkBytes(book_name, RegexUtil.BOOK_NAME_MAXBYTES)) {
			throw new BookNameExceededLimitOnMaxbytesException();
		}
		
		//4. 출판사 이름의 유효성을 판단함.
		String book_publisher = request.getParameter("book_publisher");
		if(!RegexUtil.checkRegex(book_publisher, RegexUtil.BOOK_PUBLISHER_NAME_REGEX)) {
			throw new PublisherNameNotMatchedToRegexException();
		}
		
		//6. 재고의 유효성을 판단함.
		Integer book_quantity;
		try{
			book_quantity = Integer.parseInt(request.getParameter("book_quantity"));
			if(book_quantity<0) {
				throw new TooFewBookQuantityException();
			}
		}catch(Exception e) {
			throw new BookQuantityNotMatchedToRegexException();
		}
		
		//7. 도서 장르의 유효성을 판단함.
		String book_type_id = request.getParameter("book_type_id");
		if(!RegexUtil.checkRegex(book_type_id, RegexUtil.UUID_REGEX)) {
			throw new UUIDNotMatchedToRegexException();
		}
		
		param.put("book_type_id", book_type_id);
		HashMap bookType = bookDAO.findBookType(param);
		
		if(bookType == null) {
			throw new NotFoundBookTypeException();
		}
		
		//8. 도서 정보를 추가함.
		param = new HashMap();
		param.put("book_isbn", book_isbn);
		param.put("book_name", book_name);
		param.put("book_publisher", book_publisher);
		param.put("book_type_id", book_type_id);
		param.put("book_quantity", book_quantity);
		
		String book_date = request.getParameter("book_date");
		
		param.put("book_date", book_date);
		
		int row = bookDAO.createNewBookInfo(param);
		
		if(row!=1) {
			throw new Exception();
		}

		//4. 저자 이름의 유효성을 판단함.
		String[] authors = request.getParameterValues("authors");
		List<HashMap> list = new LinkedList<HashMap>();
				
		if(authors == null||authors.length==0) {
			throw new TooFewAuthorsException();
		}
				
		for(String author : authors) {
			if(!RegexUtil.checkRegex(author, RegexUtil.BOOK_AUTHOR_NAME_REGEX)) {
				throw new AuthorNameNotMatchedToRegexException();
			}
			HashMap hm = new HashMap();
			hm.put("book_isbn", book_isbn);
			hm.put("author_name", author);
			hm.put("author_id", UUID.randomUUID().toString());
			list.add(hm);
		}
				
		if(list.size() > 0) {
			param = new HashMap();
			param.put("list", list);
			bookDAO.createNewBookAuthorInfo(param);
		}

		//5. 번역자 이름의 유효성을 판단함.
		String[] translators = request.getParameterValues("translators");
		list = new LinkedList<HashMap>();
				
		if(translators != null) {
			for(String translator : translators) {
				if(!RegexUtil.checkRegex(translator, RegexUtil.BOOK_TRANSLATOR_NAME_REGEX)) {
					throw new TranslatorNameNotMatchedToRegexException();
				}
				HashMap hm = new HashMap();
				hm.put("book_isbn", book_isbn);
				hm.put("translator_name", translator);
				hm.put("translator_id", UUID.randomUUID().toString());
				list.add(hm);
			}
		}
				
		if(list.size()>0) {
			param = new HashMap();
			param.put("list", list);
			bookDAO.createNewBookTranslatorInfo(param);
		}
		
		//9. 도서 이미지 정보를 추가함.
		List<MultipartFile> mfiles = mRequest.getFiles("book_images");
		list = new LinkedList<HashMap>();
		
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
		
		while(itr.hasNext()) {
			HashMap hm = itr.next();
			MultipartFile mfile = (MultipartFile) hm.get("book_image_file");
			File file = new File(BOOK_IMAGES_BASE_PATH+hm.get("book_isbn")+File.separator+hm.get("book_image_id")+"."+hm.get("book_image_extension"));
			if(!file.exists()) {
				file.mkdirs();
			}
			mfile.transferTo(file);
		}
	}
}