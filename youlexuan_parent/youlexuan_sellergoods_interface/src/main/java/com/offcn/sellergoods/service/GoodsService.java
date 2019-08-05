package com.offcn.sellergoods.service;

import com.offcn.entity.PackagingGoods;
import com.offcn.entity.PageResult;
import com.offcn.pojo.TbGoods;
import com.offcn.pojo.TbItem;

import java.util.List;

/**
 * 服务层接口
 * @author Administrator
 *
 */
public interface GoodsService {

	/**
	 * 返回全部列表
	 * @return
	 */
	public List<TbGoods> findAll();
	
	
	/**
	 * 返回分页列表
	 * @return
	 */
	public PageResult findPage(int pageNum, int pageSize);


	/**
	 * 增加
	*/
	public void add(PackagingGoods goods);


	/**
	 * 修改
	 */
	public void update(PackagingGoods goods);


	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	public PackagingGoods findOne(Long id);


	/**
	 * 批量删除
	 * @param ids
	 */
	public void delete(Long[] ids);

	/**
	 * 分页
	 * @param pageNum 当前页 码
	 * @param pageSize 每页记录数
	 * @return
	 */
	public PageResult findPage(TbGoods goods, int pageNum, int pageSize);

	public void updateStatus(Long[] ids, String status);

	public void updateMarketable(Long id, String marketable);

	public List<TbItem> findItemListByGoodsIdAndStatus(Long[] goodsIds, String status);
	
}
