package com.model2.mvc.web.product;

import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

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



//==> ȸ������ Controller
@Controller
@RequestMapping("/product/*")
public class ProductController {
	
	///Field
	@Autowired
	@Qualifier("productServiceImpl")
	private ProductService productService;
	//setter Method ���� ����
		
	public ProductController(){
		System.out.println(this.getClass());
	}
	
	//==> classpath:config/common.properties  ,  classpath:config/commonservice.xml ���� �Ұ�
	//==> �Ʒ��� �ΰ��� �ּ��� Ǯ�� �ǹ̸� Ȯ�� �Ұ�
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
	public String addProduct( @ModelAttribute("product") Product product, Model model) throws Exception {

		System.out.println("/addProduct");
		
		
		//Business Logic
		productService.addProduct(product);
		
		model.addAttribute("vo", product);
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
		// Model �� View ����
		model.addAttribute("vo", vo);
		model.addAttribute("menu", menu);
		
		//��ǰ�� �ߺ��� �߰� ����
		if (cookieArray.indexOf(prodNo) < 0) {
			
			if(cookieArray.length() !=0 && cookieArray != null ) {
				cookieArray+=",";
			}
			cookieArray+= prodNo.trim();
		
			//cookieArray = cookieArray.substring(0,cookieArray.length()-1);
		
			System.out.println(cookieArray);
			
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

		// Model �� View ����
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
	
	@RequestMapping(value="listProduct", method=RequestMethod.GET)
	public String listProduct( @ModelAttribute("search") Search search , Model model , HttpServletRequest request) throws Exception{
		
		System.out.println("/listProduct");
		
		String menu = "search";
		if(request.getParameter("menu") != null) {
			menu = request.getParameter("menu");
		}
		
		System.out.println("/listProduct?menu="+menu);
		
		if(search.getCurrentPage() ==0 ){
			search.setCurrentPage(1);
		}
		search.setPageSize(pageSize);
				
		// Business logic ����
		Map<String , Object> map=productService.getProductList(search);
		
		Page resultPage = new Page( search.getCurrentPage(), ((Integer)map.get("totalCount")).intValue(), pageUnit, pageSize);
		System.out.println(resultPage);
		
		// Model �� View ����
		model.addAttribute("list", map.get("list"));
		model.addAttribute("resultPage", resultPage);
		model.addAttribute("search", search);
		model.addAttribute("menu", menu);
		
		return "forward:/product/listProduct.jsp";
	}
}