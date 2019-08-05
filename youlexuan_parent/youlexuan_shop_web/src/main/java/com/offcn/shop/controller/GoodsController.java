package com.offcn.shop.controller;
import java.util.List;

import com.offcn.entity.PackagingGoods;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.dubbo.config.annotation.Reference;
import com.offcn.pojo.TbGoods;
import com.offcn.sellergoods.service.GoodsService;

import com.offcn.entity.PageResult;
import com.offcn.entity.Result;
/**
 * controller
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {

	@Reference
	private GoodsService goodsService;

	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findAll")
	public List<TbGoods> findAll(){
		return goodsService.findAll();
	}


	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findPage")
	public PageResult  findPage(int page,int rows){
		return goodsService.findPage(page, rows);
	}

	/**
	 * 增加
	 * @param goods
	 * @return
	 */
	@RequestMapping("/add")
	public Result add(@RequestBody PackagingGoods goods){
		try {
			goods.getGoods().setSellerId(SecurityContextHolder.getContext().getAuthentication().getName());
			goodsService.add(goods);
			return new Result(true, "增加成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "增加失败");
		}
	}

	/**
	 * 修改
	 * @param goods {@link PackagingGoods}
	 * @return Result
	 */
	@RequestMapping("/update")
	public Result update(@RequestBody PackagingGoods goods){
		String name = SecurityContextHolder.getContext().getAuthentication().getName();
		PackagingGoods packagingGoods = goodsService.findOne(goods.getGoods().getId());
		if (name.equals(packagingGoods.getGoods().getSellerId()) && name.equals(goods.getGoods().getSellerId())){
			try {
				goodsService.update(goods);
				return new Result(true, "修改成功");
			} catch (Exception e) {
				e.printStackTrace();
				return new Result(false, "修改失败");
			}
		}
		return new Result(false, "禁止修改");
	}

	/**
	 * 获取实体
	 * @param id
	 * @return TbGoods
	 */
	@RequestMapping("/findOne")
	public PackagingGoods findOne(Long id){
		PackagingGoods goods = goodsService.findOne(id);
		System.err.println(goods.getItemList().toString());
		return goods;
	}

	/**
	 * 批量删除
	 * @param ids
	 * @return
	 */
	@RequestMapping("/delete")
	public Result delete(Long [] ids){
		try {
			goodsService.delete(ids);
			return new Result(true, "删除成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "删除失败");
		}
	}

	/**
	 * 查询+分页
	 * @param
	 * @param page
	 * @param rows
	 * @return
	 */
	@RequestMapping("/search")
	public PageResult search(@RequestBody TbGoods goods, int page, int rows  ){
		//获取商家ID
		String sellerId = SecurityContextHolder.getContext().getAuthentication().getName();
		//设置按照商家ID进行查询
		goods.setSellerId(sellerId);
		return goodsService.findPage(goods, page, rows);
	}

	@RequestMapping("/updateMarketable")
	public Result updateMarketable(Long id, String marketable){
		try {
			goodsService.updateMarketable(id, marketable);
			return new Result(true, "修改成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "修改失败");
		}
	}

}
