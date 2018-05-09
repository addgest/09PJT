package com.model2.mvc.web.purchase;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
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
import com.model2.mvc.service.domain.Purchase;
import com.model2.mvc.service.domain.User;
import com.model2.mvc.service.product.ProductService;
import com.model2.mvc.service.product.impl.ProductServiceImpl;
import com.model2.mvc.service.purchase.PurchaseService;



//==> 회원관리 Controller
@Controller
@RequestMapping("/purchase/*")
public class PurchaseController {
	
	///Field
	@Autowired
	@Qualifier("purchaseServiceImpl")
	private PurchaseService purchaseService;
	
	///Field
	@Autowired
	@Qualifier("productServiceImpl")
	private ProductService productService;	
	//setter Method 구현 않음
		
	public PurchaseController(){
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
	
	
	@RequestMapping(value="addPurchaseView", method=RequestMethod.GET)
	public String addPurchaseView(@RequestParam("prod_no") String prodNo,
								  Model model) throws Exception {

		System.out.println("/addPurchaseView");


		Product vo=productService.getProduct(Integer.parseInt(prodNo));
		
		model.addAttribute("vo", vo);

		return "forward:/purchase/addPurchaseView.jsp";
	}
	
	@RequestMapping(value="addPurchase", method=RequestMethod.POST)
	public String addPurchase( @ModelAttribute("user") User user,
								@ModelAttribute("product") Product product,
								@ModelAttribute("purchase") Purchase purchase,
								HttpServletRequest request,
								Model model) throws Exception {

		System.out.println("/addPurchase");
		//Business Logic
		user.setUserId(request.getParameter("buyerId"));
		product.setProdNo(Integer.parseInt((String)request.getParameter("prodNo")));		
		purchase.setBuyer(user);
		purchase.setPurchaseProd(product);
		purchase.setPaymentOption(request.getParameter("paymentOption"));
		purchase.setReceiverName(request.getParameter("receiverName"));
		purchase.setReceiverPhone(request.getParameter("receiverPhone"));
		purchase.setDivyAddr(request.getParameter("receiverAddr"));
		purchase.setDivyRequest(request.getParameter("receiverRequest"));
		purchase.setDivyDate(request.getParameter("receiverDate"));
		
		purchaseService.addPurchase(purchase);
		
		model.addAttribute("purchase", purchase);
		return  "forward:/purchase/addPurchase.jsp";
	}
	
	@RequestMapping(value="getPurchase", method=RequestMethod.GET)
	public String getPurchase( @RequestParam("tranNo") int tranNo , Model model) throws Exception {
		
		System.out.println("/getPurchase?prodNo="+tranNo);
		//Business Logic
		Purchase vo = purchaseService.getPurchase(tranNo);
		// Model 과 View 연결
		model.addAttribute("vo", vo);
		
		return "forward:/purchase/getPurchase.jsp";
	}
	
	@RequestMapping(value="updatePurchaseView", method=RequestMethod.GET)
	public String updatePurchaseView( @ModelAttribute("purchase") Purchase purchase , @RequestParam("tranNo") String tranNo , Model model ) throws Exception{

		System.out.println("/updatePurchaseView?prodNo="+tranNo);
		//Business Logic

		// Model 과 View 연결
		purchase = purchaseService.getPurchase(Integer.parseInt(tranNo));
		model.addAttribute("vo", purchase);

		return "forward:/purchase/updatePurchaseView.jsp";
	}
	
	@RequestMapping(value="updatePurchase", method=RequestMethod.POST)
	public String updatePurchase( @ModelAttribute("purchase") Purchase purchase ,
									@RequestParam("tranNo") String tranNo,
									Model model ,
									HttpServletRequest request) throws Exception{

		System.out.println("/updatePurchase");
		//Business Logic
		purchase.setTranNo(Integer.parseInt(tranNo));
		purchase.setPaymentOption(request.getParameter("paymentOption"));
		purchase.setReceiverName(request.getParameter("receiverName"));
		purchase.setReceiverPhone(request.getParameter("receiverPhone"));	
		purchase.setDivyAddr(request.getParameter("receiverAddr"));
		purchase.setDivyRequest(request.getParameter("receiverRequest"));
		purchase.setDivyDate(request.getParameter("divyDate"));	
		
		purchaseService.updatePurchase(purchase);	
		
		model.addAttribute("purchase", purchase);
		model.addAttribute("tranNo", tranNo);

		//return "forward:/getPurchase.do?tranNo="+tranNo;
		return "forward:/purchase/getPurchase";

	}
	
	@RequestMapping(value= {"updateTranCodeByProd","updateTranCode"}, method=RequestMethod.GET)
	public String updateTranCodeByProd( @ModelAttribute("purchase") Purchase purchase ,
										@ModelAttribute("product") Product product ,
										@RequestParam("tranNo") int tranNo,
										@RequestParam("tranCode") String tranCode,
										Model model  ) throws Exception{

		System.out.println("/updateTranCodeByProd");
		//Business Logic
		
		purchase.setTranNo(tranNo);	
		purchase.setTranCode(tranCode);	
		purchase.setPurchaseProd(product);
		
		purchaseService.updateTranCode(purchase);	
		model.addAttribute("purchase", purchase);

		return "forward:/purchase/listPurchase";
	}	
	
	@RequestMapping(value="listPurchase", method=RequestMethod.GET)
	public String listPurchase( @ModelAttribute("user") User user,
								@ModelAttribute("search") Search search,
								Model model,
								HttpServletRequest request,
								HttpSession session) throws Exception{
		
		String menu = "search";
		if(request.getParameter("menu") != null) {
			menu = request.getParameter("menu");
		}
		System.out.println("/listPurchase?menu="+menu);

		if(search.getCurrentPage() ==0 ){
			search.setCurrentPage(1);
		}
		search.setPageSize(pageSize);
		
		user = (User)session.getAttribute("user");
		// Business logic 수행
		Map<String , Object> map=purchaseService.getPurchaseList(search, user);
		Page resultPage = new Page( search.getCurrentPage(), ((Integer)map.get("totalCount")).intValue(), pageUnit, pageSize);
		
		// Model 과 View 연결
		model.addAttribute("list", map.get("list"));
		model.addAttribute("resultPage", resultPage);
		model.addAttribute("search", search);
		model.addAttribute("menu", menu);
		
		return "forward:/purchase/listPurchase.jsp?menu="+menu;	
	}
}