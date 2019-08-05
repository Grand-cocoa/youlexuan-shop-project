package com.offcn.entity;

import com.offcn.pojo.TbGoods;
import com.offcn.pojo.TbGoodsDesc;
import com.offcn.pojo.TbItem;

import java.io.Serializable;
import java.util.List;

public class PackagingGoods implements Serializable {

	private TbGoods goods;

	private TbGoodsDesc goodsDesc;

	private List<TbItem> itemList;

	public PackagingGoods() {
	}

	public PackagingGoods(TbGoods goods, TbGoodsDesc goodsDesc, List<TbItem> itemList) {
		this.goods = goods;
		this.goodsDesc = goodsDesc;
		this.itemList = itemList;
	}

	public TbGoods getGoods() {
		return goods;
	}

	public void setGoods(TbGoods goods) {
		this.goods = goods;
	}

	public TbGoodsDesc getGoodsDesc() {
		return goodsDesc;
	}

	public void setGoodsDesc(TbGoodsDesc goodsDesc) {
		this.goodsDesc = goodsDesc;
	}

	public List<TbItem> getItemList() {
		return itemList;
	}

	public void setItemList(List<TbItem> itemList) {
		this.itemList = itemList;
	}

}
