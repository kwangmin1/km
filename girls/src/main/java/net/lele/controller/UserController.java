package net.lele.controller;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import net.lele.domain.Basket;
import net.lele.domain.Board;
import net.lele.domain.Order_details;
import net.lele.domain.Orders;
import net.lele.domain.Review;
import net.lele.repository.Order_detailRepository;
import net.lele.service.BasketService;
import net.lele.service.BoardService;
import net.lele.service.CategoryService;
import net.lele.service.OrderService;
import net.lele.service.Order_detailService;
import net.lele.service.Product_colorService;
import net.lele.service.ReviewService;
import net.lele.service.Review_heightService;
import net.lele.service.Review_sizeService;
import net.lele.service.Review_starService;
import net.lele.service.Review_weightService;
import net.lele.service.UserService;

@Controller
public class UserController {

	@Autowired
	UserService userService;
	@Autowired
	BoardService boardService;
	@Autowired
	CategoryService categoryService;
	@Autowired
	BasketService basketService;
	@Autowired
	OrderService orderService;
	@Autowired
	Order_detailService order_detailService;
	@Autowired
	Product_colorService product_colorService;
	@Autowired
	Review_heightService review_heightService;
	@Autowired
	ReviewService reviewService;
	@Autowired
	Review_weightService review_weightService;
	@Autowired
	Review_sizeService review_sizeService;
	@Autowired
	Order_detailRepository order_detailRepository;
	@Autowired
	Review_starService review_starService;
	
	@RequestMapping("user/index")
	public String index(Model model) throws Exception {
		model.addAttribute("category", categoryService.findAll());
		return "user/index"; // ???????????? ???????????? ?????? ??? ????????? URL
	}

	@RequestMapping(value = "user/board")
	public String board(Model model, Board board) throws Exception {
		model.addAttribute("category", categoryService.findAll());
		model.addAttribute("user", userService.findAll());
		return "user/board";
	}

	@RequestMapping(value = "user/board", method = RequestMethod.POST)
	public String board(Model model, Board board, BindingResult bindingResult) throws Exception {
		model.addAttribute("category", categoryService.findAll());
		if (boardService.hasErrors(board, bindingResult)) {
			model.addAttribute("category", categoryService.findAll());
			model.addAttribute("user", userService.findAll());
			return "user/board";
		}
		boardService.save(board);
		return "redirect:/guest/boardlist";
	}

	@RequestMapping(value = "user/boarddetail/{id}")
	public String boarddetail(@PathVariable("id") int id, Model model) throws Exception {
		model.addAttribute("category", categoryService.findAll());
		List<Board> board = boardService.findBoardById(id);
		model.addAttribute("board", board);
		return "user/boarddetail";
	}

	@RequestMapping(value = "user/info", method = RequestMethod.GET)
	public String info(Model model) throws Exception {
		model.addAttribute("category", categoryService.findAll());
		model.addAttribute("user", userService.findAll());
		return "user/info";
	}

	@RequestMapping(value = "user/delete/{id}")
	public String delete(@PathVariable("id") int id, Model model, Basket basket) throws Exception {
		basketService.delete(id);
		model.addAttribute("category", categoryService.findAll());
		model.addAttribute("basket", basketService.findAll());
		return "redirect:/user/basket";
	}

	@RequestMapping(value = "user/basket")
	public String basket(Model model, Basket basket) throws Exception {
		String userId = SecurityContextHolder.getContext().getAuthentication().getName(); // ????????? jh_o214
		model.addAttribute("category", categoryService.findAll());
		model.addAttribute("count", basketService.countByUserUserId(userId));
		model.addAttribute("basket", basketService.findByUserUserId(userId));
		return "user/basket";
	}

	@ResponseBody
	@RequestMapping(value = "user/deleteA", method = RequestMethod.POST)
	public int deleteA(@RequestParam("chbox[]") List<String> Arr, Basket basket) throws Exception {
		int result = 0;
		for (String i : Arr) {
			basketService.delete(Integer.parseInt(i));
		}
		result = 1;
		return result;
	}

	@RequestMapping(value = "user/allorder")
	public String allorder(Model model, Orders orders) throws Exception {
		String userId = SecurityContextHolder.getContext().getAuthentication().getName();
		model.addAttribute("category", categoryService.findAll());
		model.addAttribute("basket", basketService.findByUserUserId(userId));
		model.addAttribute("user", userService.findAll());
		return "user/allorder";
	}

	@RequestMapping(value = "user/allorder", method = RequestMethod.POST)
	public String allorder(Model model, Orders orders, BindingResult bindingResult) throws Exception {
		Calendar cal = Calendar.getInstance();
		int year = cal.get(Calendar.YEAR);
		String ym = year + new DecimalFormat("00").format(cal.get(Calendar.MONTH) + 1);
		String ymd = ym + new DecimalFormat("00").format(cal.get(Calendar.DATE));
		String subNum = "";
		for (int i = 1; i <= 6; i++) {
			subNum += (int) (Math.random() * 10);
		}
		String orderId = ymd + "_" + subNum; // ???????????? ?????????

		String userId = SecurityContextHolder.getContext().getAuthentication().getName();
		orders.setId(orderId);
		orderService.save(orders);
		List<Basket> b = basketService.findByUserUserId(userId);
		for (int i = 0; i < b.size(); ++i) {
			Order_details o = new Order_details();
			o.setProduct(b.get(i).getProduct());
			o.setColor(b.get(i).getColor());
			o.setCount(b.get(i).getCount());
			o.setOrderid(orderId);
			o.setReview(0);
			/* o.setOrders(orders.getId()); */
			order_detailService.save(o);
		}
		basketService.deleteByUserUserId(userId); // ????????? ?????? ?????????... ??????
		return "redirect:/user/orderlist";
		/* order_detailService.save(order_details); */
		/*
		 * result = 1; return result;
		 */
	}

	@RequestMapping(value = "user/orderlist")
	public String orderlist(Model model) {
		String userId = SecurityContextHolder.getContext().getAuthentication().getName();
		model.addAttribute("category", categoryService.findAll());
		model.addAttribute("ord", orderService.findAllByOrderByRidDesc());
		model.addAttribute("count", orderService.countByUserUserId(userId));
		return "user/orderlist";
	}

	@RequestMapping(value = "user/orderdetail/{id}")
	public String orderdetail(@PathVariable("id") String id, Model model) throws Exception {
		model.addAttribute("category", categoryService.findAll());
		model.addAttribute("order", order_detailService.findOdById(id));
		model.addAttribute("oo", orderService.findById(id));
		return "user/orderdetail";
	}

	@RequestMapping(value = "user/ordercancle/{id}")
	public String ordercancle(@PathVariable("id") int id, Model model, Orders orders) throws Exception {
		orderService.delete(id);
		return "redirect:/user/orderlist";
	}

	@RequestMapping(value = "user/review/{id}")
	public String review(@PathVariable("id") int id, Model model, Review review) throws Exception {
		model.addAttribute("category", categoryService.findAll());
		model.addAttribute("order", order_detailService.findById(id));
		model.addAttribute("height", review_heightService.findAll());
		model.addAttribute("weight", review_weightService.findAll());
		model.addAttribute("size", review_sizeService.findAll());
		model.addAttribute("star", review_starService.findAll());

		return "user/review";
	}
	
	@RequestMapping(value = "user/review/{id}", method = RequestMethod.POST)
	public String review(@PathVariable("id") int id, Model model, Review review, BindingResult bindingResult) throws Exception {
		model.addAttribute("category", categoryService.findAll());
		reviewService.save(review);
		Order_details o = order_detailService.findById(id);
		o.setReview(1);
		order_detailRepository.save(o);
		return "redirect:/user/index";
	}
}

/*
 * @RequestMapping(value = "user/basket", method = RequestMethod.POST) public
 * String basket(Model model, Basket basket, Orders orders, Order_detail
 * order_detail, BindingResult bindingResult) throws Exception { String userId =
 * SecurityContextHolder.getContext().getAuthentication().getName(); // ?????????
 * jh_o214 if (orderService.hasErrors(orders, bindingResult)) {
 * model.addAttribute("category", categoryService.findAll());
 * model.addAttribute("basket", basketService.findByUserUserId(userId)); }
 * orderService.save(orders); basketService.deleteByUserUserId(userId); return
 * "user/basket"; }
 */

/* model.addAttribute("basket", basketService.findAll()); */
/*
 * Object userId =
 * SecurityContextHolder.getContext().getAuthentication().getPrincipal();
 * //userid????????? (jh_o214)
 */

/*
 * @RequestMapping(value = "/user/order/{id}") public String
 * order(@PathVariable("id") int id, Model model, Basket basket) throws
 * Exception { model.addAttribute("category", categoryService.findAll());
 * model.addAttribute("basket", basketService.findAll());
 * model.addAttribute("idd", id); return "/user/order"; } //1?????? ???????????????
 */