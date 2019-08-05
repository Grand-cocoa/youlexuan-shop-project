package com.offcn.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.offcn.cart.service.CartService;
import com.offcn.entity.Cart;
import com.offcn.entity.Result;
import com.offcn.utli.CookieUtil;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {

	@Reference(timeout = 3000)
	private CartService cartService;

	//读取当前购物车数据
	@RequestMapping("/findCartList")
	public List<Cart> findCartList(HttpServletRequest req, HttpServletResponse resp){
		String name = SecurityContextHolder.getContext().getAuthentication().getName();
		System.err.println("当前登录用户名:" + name);
		List<Cart> carts = null;
		String cartList = CookieUtil.getCookieValue(req, "cartList", "UTF-8");
		if (cartList == null || cartList.equals(""))
			cartList = "[]";
		List<Cart> cart_cookie = JSON.parseArray(cartList, Cart.class);
		if (!name.equals("anonymousUser")) {
			carts = cartService.findCartFromRedis(name);
			if (cart_cookie.size() > 0){
				carts = cartService.mergeCartList(carts, cart_cookie);
				CookieUtil.deleteCookie(req, resp, "cartList");
				cartService.saveCartListToRedis(name, carts);
			}
			return carts;
		}else
			return cart_cookie;
	}

	@RequestMapping("/addGoodsToCartList")
	@CrossOrigin(origins = "http://localhost:9105", allowCredentials = "true")
	public Result addGoodsToCartList(Long itemId, Integer num, HttpServletResponse resp, HttpServletRequest req){
		String name = SecurityContextHolder.getContext().getAuthentication().getName();
		System.err.println("当前登录用户名:" + name);
		try {
			List<Cart> cartList = findCartList(req, resp);
			cartList = cartService.addGoodsToCartList(cartList, itemId, num);
			if (name.equals("anonymousUser"))
				CookieUtil.setCookie(req, resp, "cartList", JSON.toJSONString(cartList), 3600 * 24, "UTF-8");
			else
				cartService.saveCartListToRedis(name, cartList);
			/*resp.setHeader("Access-Control-Allow-Origin", "http://localhost:9105");
			resp.setHeader("Access-Control-Allow-Credentials", "true");*/
			return new Result(true, "添加成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "添加失败");
		}
	}

}
