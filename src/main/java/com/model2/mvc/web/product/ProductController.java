package com.model2.mvc.web.product;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUpload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.model2.mvc.common.Page;
import com.model2.mvc.common.Search;
import com.model2.mvc.service.domain.Product;
import com.model2.mvc.service.product.ProductService;



//==> 회원관리 Controller
@Controller
@RequestMapping("/product/*")
public class ProductController {
	
	///Field
	@Autowired
	@Qualifier("productServiceImpl")
	private ProductService productService;
	//setter Method 구현 않음
		
	public ProductController(){
		System.out.println(this.getClass());
	}
	
	//==> classpath:config/common.properties  ,  classpath:config/commonservice.xml 참조 할것
	//==> 아래의 두개를 주석을 풀어 의미를 확인 할것
	@Value("#{commonProperties['pageUnit']}")
	//@Value("#{commonProperties['pageUnit'] ?: 3}")
	int pageUnit;
	
	@Value("#{commonProperties['pageSize']}")
	//@Value("#{commonProperties['pageSize'] ?: 2}")
	int pageSize;
	
	
	@RequestMapping(value="addProductView", method=RequestMethod.GET)
	public String addProductView() throws Exception {

		System.out.println("/addProductView");
		
		return "redirect:/product/addProductView.jsp";
	}
	
	@RequestMapping(value="addProduct", method=RequestMethod.POST)
	public String addProduct( @ModelAttribute("product") Product product,
								Model model,
								HttpServletRequest request, HttpServletResponse response) throws Exception {

		if(FileUpload.isMultipartContent(request)) {
			String temDir = "C:\\workspace\\07.Model2MVCShop(2URI,pattern)\\WebContent\\images\\uploadFiles\\";
			
			DiskFileUpload fileUpload = new DiskFileUpload();
			fileUpload.setRepositoryPath(temDir);
			fileUpload.setSizeMax(1024*1024*10);
			fileUpload.setSizeThreshold(1024*100);
			
			if(request.getContentLength()<fileUpload.getSizeMax()) {
				StringTokenizer token = null;
				
				List fileItemList = fileUpload.parseRequest(request);
				int Size = fileItemList.size();
				for(int i=0;i<Size;i++) {
					FileItem fileItem = (FileItem) fileItemList.get(i);
					if(fileItem.isFormField()) {
						if(fileItem.getFieldName().equals("manuDate")) {
							token = new StringTokenizer(fileItem.getString("euc-kr"), "-");
							String manuDate = token.nextToken()+token.nextToken()+token.nextToken();
							product.setManuDate(manuDate);
						}else if(fileItem.getFieldName().equals("prodName")) {	
							product.setProdName(fileItem.getString("euc-kr"));
						}else if(fileItem.getFieldName().equals("prodDetail")) {	
							product.setProdDetail(fileItem.getString("euc-kr"));
						}else if(fileItem.getFieldName().equals("price")) {	
							product.setPrice(Integer.parseInt(fileItem.getString("euc-kr")));
						}
						
					}else {//파일형식이면
						if(fileItem.getSize()>0) {
							int idx = fileItem.getName().lastIndexOf("\\");
							if(idx == -1) {
								idx = fileItem.getName().lastIndexOf("/");
							}
							String fileName = fileItem.getName().substring(idx+1);
							product.setFileName(fileName);
							try{
								File uploadedFile = new File(temDir, fileName);
								fileItem.write(uploadedFile);
							}catch(IOException e) {
								System.out.println(e);
							}							
						}else {
							product.setFileName("/images/uploadFiles/empty.GIF");
						}
					}//else					 
				}//end for
				
				//Business Logic
				productService.addProduct(product);
				model.addAttribute("vo", product);
				
			}else {//업로드하는 파일이 setSizeMax보다 큰 경우
				int overSize = (request.getContentLength()/1000000);
				System.out.println("<script>alert('파일의 크기는 1MB까지 입니다. 올리신 파일용량은"+overSize+"MB입니다');");
				System.out.println("history.back();</scirpt>");
			}
				
		}else {
			System.out.println("인코딩 타입이 multipart/form-data가 아닙니다..");
		}
	
		//return "redirect:/product/addProductView.jsp";
		return  "forward:/product/addProduct.jsp";
	}
	
	@RequestMapping(value="getProduct", method=RequestMethod.GET)
	public String getProduct( @RequestParam("prodNo") String prodNo ,
							  @RequestParam("menu") String menu ,
							  HttpServletResponse response,
							  Model model) throws Exception {
		
		String cookieArray="";		

		System.out.println("/getProduct?prodNo="+prodNo);
		//Business Logic
		Product vo = productService.getProduct(Integer.parseInt(prodNo));
		// Model 과 View 연결
		model.addAttribute("vo", vo);
		model.addAttribute("menu", menu);
		
		//상품뷰 중복시 추가 안함
		if (cookieArray.indexOf(prodNo) < 0) {
			
			if(cookieArray.length() !=0 && cookieArray != null ) {
				cookieArray+=",";
			}
			cookieArray+= prodNo.trim();
		
			//cookieArray = cookieArray.substring(0,cookieArray.length()-1);
		
			System.out.println("cookieArray:"+cookieArray);
			
			Cookie cookie = new Cookie("history", cookieArray);
			
			cookie.setMaxAge(60*60);
			response.addCookie(cookie);
		
		}
		
		return "forward:/product/getProduct.jsp";
	}
	
	@RequestMapping(value="updateProductView", method=RequestMethod.GET)
	public String updateProductView( @ModelAttribute("product") Product product , @RequestParam("prodNo") String prodNo , Model model ) throws Exception{

		System.out.println("/updateProductView?prodNo="+prodNo);
		//Business Logic

		// Model 과 View 연결
		Product vo = productService.getProduct(Integer.parseInt(prodNo));
		model.addAttribute("product", vo);

		return "forward:/product/updateProductView.jsp";
	}
	
	@RequestMapping(value="updateProduct", method=RequestMethod.POST)
	public String updateProduct( @ModelAttribute("product") Product product ,@RequestParam("prodNo") int prodNo, Model model , HttpSession session) throws Exception{

		System.out.println("/updateProduct");
		//Business Logic
		productService.updateProduct(product);	
		model.addAttribute("product", product);
		
		return "redirect:/product/updateProductView?prodNo="+prodNo;

	}
	
	@RequestMapping(value="listProduct")
	public String listProduct( @ModelAttribute("search") Search search , Model model , HttpServletRequest request) throws Exception{
		
		String menu = "search";
		if(request.getParameter("menu") != null) {
			menu = request.getParameter("menu");
		}
		
		System.out.println("/listProduct?menu="+menu);
		
		if(search.getCurrentPage() ==0 ){
			search.setCurrentPage(1);
		}
		search.setPageSize(pageSize);
				
		// Business logic 수행
		Map<String , Object> map=productService.getProductList(search);
		
		Page resultPage = new Page( search.getCurrentPage(), ((Integer)map.get("totalCount")).intValue(), pageUnit, pageSize);
		System.out.println(resultPage);
		
		// Model 과 View 연결
		model.addAttribute("list", map.get("list"));
		model.addAttribute("resultPage", resultPage);
		model.addAttribute("search", search);
		model.addAttribute("menu", menu);
		
		return "forward:/product/listProduct.jsp";
	}
}