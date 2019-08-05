package com.offcn.cart.service;

import com.offcn.entity.Cart;

import java.util.List;

public interface CartService {

	public List<Cart> addGoodsToCartList(List<Cart> oldCartList, Long itemId, Integer num);

	public void saveCartListToRedis(String username, List<Cart> cartList);

	public List<Cart> findCartFromRedis(String username);

	public List<Cart> mergeCartList(List<Cart> cartList1, List<Cart> cartList2);

}
