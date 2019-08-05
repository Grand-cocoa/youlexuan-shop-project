package com.offcn.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.offcn.cart.service.CartService;
import com.offcn.entity.Cart;
import com.offcn.mapper.TbItemMapper;
import com.offcn.pojo.TbItem;
import com.offcn.pojo.TbOrderItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {

	@Autowired
	private TbItemMapper itemMapper;

	@Autowired
	private RedisTemplate redisTemplate;

	public List<Cart> addGoodsToCartList(List<Cart> oldCartList, Long itemId, Integer num) {
		TbItem item = itemMapper.selectByPrimaryKey(itemId);
		if (item == null)
			throw new RuntimeException("商品不存在");
		if (!item.getStatus().equals("1"))
			throw new RuntimeException("商品状态无效");
		String sellerId = item.getSellerId();
		//根据商家ID判断购物车列表是否存在该购物车
		Cart cart = searchCartsBySellerId(oldCartList, sellerId);
		if (cart == null) {
			cart = new Cart();
			cart.setSellerId(sellerId);
			cart.setSellerName(item.getSeller());
			//设定购物车对象的购物车明细
			TbOrderItem orderItem = careteOrderItem(item, num);
			//创建购物明细集合
			List<TbOrderItem> orderItemList = new ArrayList<TbOrderItem>();
			orderItemList.add(orderItem);
			cart.setOrderItemList(orderItemList);
			//将购物车对象添加到原有的购物车集合
			oldCartList.add(cart);
		}else {
			//如果购物车列表存在指定商家的购物车
			//判断购物车明细列表是否存在该商品
			TbOrderItem orderItem = searchPrderItemByItemId(cart.getOrderItemList(), itemId);
			if (orderItem == null){
				//创建商品购物明细对象
				orderItem = careteOrderItem(item, num);
				cart.getOrderItemList().add(orderItem);
			}else {
				//购物车内存在商家商品
				//修改购物车明细的购买数量
				orderItem.setNum(orderItem.getNum() + num);
				//修改合计金额
				orderItem.setTotalFee(new BigDecimal(orderItem.getNum() * orderItem.getPrice().doubleValue()));
				//判断购买数量
				if (orderItem.getNum() <= 0)
					cart.getOrderItemList().remove(orderItem);
				if (cart.getOrderItemList().size() == 0)
					oldCartList.remove(cart);
			}
		}
		return oldCartList;
	}

	public void saveCartListToRedis(String username, List<Cart> cartList) {
		System.err.println("向Resid写入购物车数据");
		redisTemplate.boundHashOps("cartList").put(username, cartList);
	}

	public List<Cart> findCartFromRedis(String username) {
		System.err.println("从Redis缓存读取购物车数据");
		List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(username);
		if (cartList == null)
			cartList = new ArrayList<Cart>();
		return cartList;
	}

	public List<Cart> mergeCartList(List<Cart> cartList1, List<Cart> cartList2) {
		System.err.println("合并购物车");
		for (Cart cart :
				cartList2) {
			for (TbOrderItem orderItem :
					cart.getOrderItemList()) {
				cartList1 = addGoodsToCartList(cartList1, orderItem.getItemId(), orderItem.getNum());
			}
		}
		return cartList1;
	}

	//根据商家ID判断购物车列表是否存在该购物车
	private Cart searchCartsBySellerId(List<Cart> cartList, String sellerId){
		//遍历购物车集合
		for (Cart cart :
				cartList) {
			if (cart.getSellerId().equals(sellerId))
				return cart;
		}
		return null;
	}

	//设定购物车明细
	private TbOrderItem careteOrderItem(TbItem item, Integer num){
		if (num <= 0)
			throw new RuntimeException("购买数量非法");
		else {
			TbOrderItem orderItem = new TbOrderItem();
			orderItem.setGoodsId(item.getGoodsId());
			orderItem.setItemId(item.getId());
			orderItem.setNum(num);
			orderItem.setPicPath(item.getImage());
			orderItem.setPrice(item.getPrice());
			orderItem.setSellerId(item.getSellerId());
			orderItem.setTitle(item.getTitle());
			//设置购物明细总金额
			orderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue() * num));
			return orderItem;
		}
	}

	//判断商品是否已存在于购物车
	private TbOrderItem searchPrderItemByItemId(List<TbOrderItem> orderItemList, Long itemId){
		for (TbOrderItem orderItem :
				orderItemList) {
			//比对ItemId是否相等
			if (orderItem.getItemId().longValue() == itemId.longValue())
				return orderItem;
		}
		return null;
	}
}
