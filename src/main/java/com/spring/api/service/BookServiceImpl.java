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
		//1. �ش� ����ڰ� ������ �ִ��� Ȯ��
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
		
		//2. ISBN �ڵ��� ��ȿ���� �Ǵ���
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
		
		//3. ���� ������ ��ȿ���� �Ǵ���.
		String book_name = request.getParameter("book_name");
		if(!RegexUtil.checkBytes(book_name, RegexUtil.BOOK_NAME_MAXBYTES)) {
			throw new CustomException(ErrorCode.BOOK_NAME_EXCEEDED_LIMIT_ON_MAXBYTES);
		}
		
		//4. ���ǻ� �̸��� ��ȿ���� �Ǵ���.
		String book_publisher = request.getParameter("book_publisher");
		if(!RegexUtil.checkRegex(book_publisher, RegexUtil.BOOK_PUBLISHER_NAME_REGEX)) {
			throw new CustomException(ErrorCode.PUBLISHER_NAME_NOT_MATCHED_TO_REGEX);
		}
		
		//5. ����� ��ȿ���� �Ǵ���.
		Integer book_quantity;
		try{
			book_quantity = Integer.parseInt(request.getParameter("book_quantity"));
			if(book_quantity<0) {
				throw new CustomException(ErrorCode.TOO_FEW_BOOK_QUANTITY);
			}
		}catch(Exception e) {
			throw new CustomException(ErrorCode.BOOK_QUANTITY_NOT_MATCHED_TO_REGEX);
		}
		
		//6. ���� �帣�� ��ȿ���� �Ǵ���.
		String book_type_id = request.getParameter("book_type_id");
		if(!RegexUtil.checkRegex(book_type_id, RegexUtil.UUID_REGEX)) {
			throw new CustomException(ErrorCode.UUID_NOT_MATCHED_TO_REGEX);
		}
		
		param.put("book_type_id", book_type_id);
		HashMap bookType = bookDAO.findBookType(param);
		
		if(bookType == null) {
			throw new CustomException(ErrorCode.NOT_FOUND_BOOK_TYPE);
		}
		
		//7. ���� �̸��� ��ȿ���� �Ǵ���.
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
		
		//8. ������ �̸��� ��ȿ���� �Ǵ���.
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

		//9. ���� ������ �߰���.
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
			System.out.println(row+"�ο�");
			throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
		}

		//10. ���� �̹��� ������ �߰���.
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
		
		//11. ������ �̹��� ������ ������.
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
	//���� �帣 ��ȸ ��û�� ó���ϴ� �޼ҵ�
	public List readBookTypes() {
		return bookDAO.readBookTypes();
	}
	
	@Override
	//���� �帣 ��� ��û�� ó���ϴ� �޼ҵ�
	public void createNewBookTypes(HttpServletRequest request, HashMap param) {
		//1. �ش� ����ڰ� ������ �ִ��� Ȯ��
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
		
		//2. ���� �帣 �̸��� ��ȿ���� �Ǵ���.
		String book_type_content = (String) param.get("book_type_content");
		if(!RegexUtil.checkBytes(book_type_content,RegexUtil.BOOK_TYPE_CONTENT_MAXBYTES)) {
			throw new CustomException(ErrorCode.BOOK_TYPE_CONTENT_EXCEEDED_LIMIT_IN_MAXBYTES);
		}
		
		//3. ���� �帣�� ���� �߰���.
		param.put("book_type_id", UUID.randomUUID().toString());
		
		if(bookDAO.createNewBookTypes(param)!=1) {
			throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	//���� ���� ���� ��û�� ó���ϴ� �޼ҵ�
	public void updateBookTypes(HttpServletRequest request, HashMap param) {
		//1. �ش� ����ڰ� ������ �ִ��� Ȯ��
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
		
		//2. ���� �帣 �̸��� ��ȿ���� �Ǵ���.
		String book_type_content = (String) param.get("book_type_content");
		if(!RegexUtil.checkBytes(book_type_content,RegexUtil.BOOK_TYPE_CONTENT_MAXBYTES)) {
			throw new CustomException(ErrorCode.BOOK_TYPE_CONTENT_EXCEEDED_LIMIT_IN_MAXBYTES);
		}
		
		//3. ���� �帣�� �����ϴ��� �Ǵ���.
		HashMap book_type = bookDAO.findBookType(param);
		if(book_type==null) {
			throw new CustomException(ErrorCode.NOT_FOUND_BOOK_TYPE);
		}
		
		//4. ���� �帣�� ������.
		if(bookDAO.updateBookTypes(param)!=1) {
			throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public void deleteBookTypes(HttpServletRequest request, HashMap param) {
		//1. �ش� ����ڰ� ������ �ִ��� Ȯ��
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
		
		//2. ���� �帣�� �����ϴ��� �Ǵ���.
		HashMap book_type = bookDAO.findBookType(param);
		if(book_type==null) {
			throw new CustomException(ErrorCode.NOT_FOUND_BOOK_TYPE);
		}

		//3. ���� �帣�� ������.
		if(bookDAO.deleteBookTypes(param)!=1) {
			throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public HashMap readBooks(HashMap param) {
		//1. size�� ������� 10 ~ 100���̷�, �⺻ 10
		int size = 10;
		
		if(param.get("size")!=null) {
			try {
				size = Integer.parseInt((String)param.get("size"));
				if(size<10||size>100) {
					throw new CustomException(ErrorCode.SIZE_OUT_OF_RANGE);
				}
			}catch(NumberFormatException e) {
				throw new CustomException(ErrorCode.SIZE_NOT_COUNTABLE);
			}
		}
		param.put("size", size);
		
		//2. page�� 1�� �⺻
		int page = 1;
		
		if(param.get("page")!=null) {
			try {
				page = Integer.parseInt((String)param.get("page"));
			}catch(NumberFormatException e) {
				throw new CustomException(ErrorCode.PAGE_NOT_COUNTABLE);
			}
		}
		
		//3. �˻����� �ʵ尡 ���� ����, ���� ISBN �ڵ�, ���ǻ� �̸�, ���� �̸�, ������ �̸��� �ϳ��� ���ϴ��� �Ǵ���. �⺻�����δ� ���� ������ �˻� ������
		String flag = "book_name";
		if(param.get("flag")!=null) {
			flag = (String) param.get("flag");
		}
		
		if(!(flag.equalsIgnoreCase("book_name")||flag.equalsIgnoreCase("book_isbn")||flag.equalsIgnoreCase("book_publisher"))) {
			flag = "book_name";
		}
		param.put("flag", flag);
		
		//4. �˻����ؿ� ���Ͽ� �˻����� ���ԽĿ� �����ϴ��� Ȯ����
		String search = "";
		if(param.get("search")!=null) {
			search = (String) param.get("search");
		}
		
		if(flag.equalsIgnoreCase("book_name")) {
			if(!RegexUtil.checkBytes(search,RegexUtil.BOOK_NAME_MAXBYTES)) {
				throw new CustomException(ErrorCode.BOOK_NAME_EXCEEDED_LIMIT_ON_MAXBYTES);
			}
		}else if(flag.equalsIgnoreCase("book_isbn")) {
			if(!RegexUtil.checkRegex(search,RegexUtil.BOOK_ISBN_REGEX)) {
				throw new CustomException(ErrorCode.BOOK_ISBN_NOT_MATCHED_TO_REGEX);
			}			
		}else if(flag.equalsIgnoreCase("book_publisher")) {
			if(!RegexUtil.checkRegex(search,RegexUtil.BOOK_PUBLISHER_NAME_REGEX)) {
				throw new CustomException(ErrorCode.PUBLISHER_NAME_NOT_MATCHED_TO_REGEX);
			}
		}else if(flag.equalsIgnoreCase("book_authors")) {
			if(!RegexUtil.checkRegex(search,RegexUtil.BOOK_AUTHOR_NAME_REGEX)) {
				throw new CustomException(ErrorCode.AUTHOR_NAME_NOT_MATCHED_TO_REGEX);
			}
		}else if(flag.equalsIgnoreCase("book_translators")) {
			if(!RegexUtil.checkRegex(search,RegexUtil.BOOK_TRANSLATOR_NAME_REGEX)) {
				throw new CustomException(ErrorCode.TRANSLATOR_NAME_NOT_MATCHED_TO_REGEX);
			}
		}else if(flag.equalsIgnoreCase("book_type_content")) {
			if(!RegexUtil.checkBytes(search,RegexUtil.BOOK_TYPE_CONTENT_MAXBYTES)) {
				throw new CustomException(ErrorCode.BOOK_TYPE_CONTENT_EXCEEDED_LIMIT_IN_MAXBYTES);
			}
		}
		
		
		param.put("search", search);
		
		//5. ���Ĺ���� ��������, ������������ Ȯ����. �⺻������ ������������
		String sort = "ASC";
		if(param.get("sort")!=null) {
			if(sort.equalsIgnoreCase("ASC")||sort.equalsIgnoreCase("DESC")) {
				sort = (String) param.get("sort");
			}else {
				throw new CustomException(ErrorCode.SORT_OUT_OF_RANGE);
			}
		}
		param.put("sort", sort);
		
		//6. ����¡ �ִ� ũ�⸦ ����
		int max_page = (int) Math.ceil(bookDAO.getBookTotal(param)*1.0/size);
		if(max_page==0) {
			max_page=1;
		}
		
		//7. ������ ��ȣ�� ������ ���� ���� �ִ��� Ȯ����
		if(page<=0||page>max_page) {
			throw new CustomException(ErrorCode.PAGE_OUT_OF_RANGE);
		}else {
			param.put("offset", (page-1)*size);
		}
		param.put("page", page);
		
		//8. ���� ����� ��ȸ��
		List<HashMap> books = bookDAO.readBooks(param);
		Iterator<HashMap> itor = books.iterator();
		
		while(itor.hasNext()) {
			HashMap book = itor.next();
			
			String[] author_names = ((String)book.get("book_authors")).split(" ");
			List book_authors = new LinkedList();
			for(String author : author_names) {
				book_authors.add(author);
			}

			String[] translator_names = ((String)book.get("book_translators")).split(" ");
			List book_translators = new LinkedList();
			for(String translator : translator_names) {
				book_translators.add(translator);
			}
			
			book.remove("book_authors");
			book.remove("book_translators");
			book.put("book_authors", book_authors);
			book.put("book_translators", book_translators);
		}
		HashMap result = new HashMap();
		result.put("books", books);
		result.put("max_page", max_page);
		return result;
	}
}