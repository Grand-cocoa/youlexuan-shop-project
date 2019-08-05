package com.offcn.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.offcn.entity.PackagingGoods;
import com.offcn.entity.PageResult;
import com.offcn.mapper.*;
import com.offcn.pojo.*;
import com.offcn.pojo.TbGoodsExample.Criteria;
import com.offcn.sellergoods.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 服务实现层
 *
 * @author Administrator
 */
@Service
@Transactional
public class GoodsServiceImpl implements GoodsService {

	@Autowired
	private TbGoodsMapper goodsMapper;

	@Autowired
	private TbGoodsDescMapper goodsDescMapper;

	@Autowired
	private TbItemMapper itemMapper;

	@Autowired
	private TbBrandMapper brandMapper;

	@Autowired
	private TbItemCatMapper itemCatMapper;

	@Autowired
	private TbSellerMapper sellerMapper;

	/**
	 * 查询全部
	 */
	public List<TbGoods> findAll() {
		return goodsMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		TbGoodsExample example = new TbGoodsExample();
		Criteria criteria = example.createCriteria();
		criteria.andIsDeleteIsNull();
		Page<TbGoods> page = (Page<TbGoods>) goodsMapper.selectByExample(example);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	public void add(PackagingGoods goods) {
		goods.getGoods().setAuditStatus("0");
		goodsMapper.insert(goods.getGoods());
		goods.getGoodsDesc().setGoodsId(goods.getGoods().getId());
		goodsDescMapper.insert(goods.getGoodsDesc());
		saveItemList(goods);
	}

	private void saveItemList(PackagingGoods goods) {
		if ("1".equals(goods.getGoods().getIsEnableSpec())) {
			for (TbItem item : goods.getItemList()) {
				String title = goods.getGoods().getGoodsName();
				Map<String, Object> specMap = JSON.parseObject(item.getSpec());
				for (String key : specMap.keySet()) {
					title += " " + specMap.get(key);
				}
				item.setTitle(title);
				setItemValue(goods, item);
				itemMapper.insert(item);

			}
		} else {
			TbItem item = new TbItem();
			item.setTitle(goods.getGoods().getGoodsName());
			item.setPrice(goods.getGoods().getPrice());
			item.setStatus("1");
			item.setIsDefault("1");
			item.setNum(999);
			item.setSpec("{}");
			setItemValue(goods, item);
			itemMapper.insert(item);
		}
	}

	private void setItemValue(PackagingGoods goods, TbItem item) {
		item.setGoodsId(goods.getGoods().getId());
		item.setSellerId(goods.getGoods().getSellerId());
		item.setCategoryid(goods.getGoods().getCategory3Id());
		item.setCreateTime(new Date());
		item.setUpdateTime(new Date());
		TbBrand brand = brandMapper.selectByPrimaryKey(goods.getGoods().getBrandId());
		item.setBrand(brand.getName());
		TbItemCat itemCat = itemCatMapper.selectByPrimaryKey(goods.getGoods().getCategory3Id());
		item.setCategory(itemCat.getName());
		TbSeller seller = sellerMapper.selectByPrimaryKey(goods.getGoods().getSellerId());
		item.setSeller(seller.getNickName());
		List<Map> imageList = JSON.parseArray(goods.getGoodsDesc().getItemImages(), Map.class);
		if (imageList.size() > 0) {
			item.setImage((String) imageList.get(0).get("url"));
		}
	}

	/**
	 * 修改
	 */
	public void update(PackagingGoods goods) {
		goods.getGoods().setAuditStatus("0");
		goodsMapper.updateByPrimaryKey(goods.getGoods());
		goodsDescMapper.updateByPrimaryKey(goods.getGoodsDesc());
		TbItemExample example = new TbItemExample();
		TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andGoodsIdEqualTo(goods.getGoods().getId());
		itemMapper.deleteByExample(example);
		saveItemList(goods);
	}

	/**
	 * 根据ID获取实体
	 *
	 * @param id
	 * @return
	 */
	public PackagingGoods findOne(Long id) {
		PackagingGoods packagingGoods = new PackagingGoods();
		packagingGoods.setGoods(goodsMapper.selectByPrimaryKey(id));
		packagingGoods.setGoodsDesc(goodsDescMapper.selectByPrimaryKey(id));
		TbItemExample example = new TbItemExample();
		TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andGoodsIdEqualTo(id);
		List<TbItem> items = itemMapper.selectByExample(example);
		packagingGoods.setItemList(items);
		return packagingGoods;
	}

	/**
	 * 批量删除
	 */
	public void delete(Long[] ids) {
		/*List<Long> list = new ArrayList<Long>();*/
		for (Long id : ids) {
			/*goodsMapper.deleteByPrimaryKey(id);*/
			TbGoods goods = goodsMapper.selectByPrimaryKey(id);
			goods.setIsDelete("1");
			goodsMapper.updateByPrimaryKey(goods);
			/*list.add(id);*/
		}
		List<TbItem> itemList = findItemListByGoodsIdAndStatus(ids, "1");
		for (TbItem item : itemList){
			item.setStatus("0");
			itemMapper.updateByPrimaryKey(item);
		}
		/*TbGoodsExample example = new TbGoodsExample();
		Criteria criteria = example.createCriteria();
		criteria.andIdIn(list);
		TbGoods goods = new TbGoods();
		goods.setIsDelete("1");
		goodsMapper.updateByExample(goods, example);
		example.clear();*/
	}


	public PageResult findPage(TbGoods goods, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);

		TbGoodsExample example = new TbGoodsExample();
		Criteria criteria = example.createCriteria();
		criteria.andIsDeleteIsNull();

		if (goods != null) {
			if (goods.getSellerId() != null && goods.getSellerId().length() > 0) {
				criteria.andSellerIdEqualTo(goods.getSellerId());
			}
			if (goods.getGoodsName() != null && goods.getGoodsName().length() > 0) {
				criteria.andGoodsNameLike("%" + goods.getGoodsName() + "%");
			}
			if (goods.getAuditStatus() != null && goods.getAuditStatus().length() > 0) {
				criteria.andAuditStatusLike("%" + goods.getAuditStatus() + "%");
			}
			if (goods.getIsMarketable() != null && goods.getIsMarketable().length() > 0) {
				criteria.andIsMarketableLike("%" + goods.getIsMarketable() + "%");
			}
			if (goods.getCaption() != null && goods.getCaption().length() > 0) {
				criteria.andCaptionLike("%" + goods.getCaption() + "%");
			}
			if (goods.getSmallPic() != null && goods.getSmallPic().length() > 0) {
				criteria.andSmallPicLike("%" + goods.getSmallPic() + "%");
			}
			if (goods.getIsEnableSpec() != null && goods.getIsEnableSpec().length() > 0) {
				criteria.andIsEnableSpecLike("%" + goods.getIsEnableSpec() + "%");
			}
			if (goods.getIsDelete() != null && goods.getIsDelete().length() > 0) {
				criteria.andIsDeleteLike("%" + goods.getIsDelete() + "%");
			}
		}

		Page<TbGoods> page = (Page<TbGoods>) goodsMapper.selectByExample(example);
		return new PageResult(page.getTotal(), page.getResult());
	}

	public void updateStatus(Long[] ids, String status) {
		for (Long id : ids) {
			//根据商品 id 获取商品信息
			TbGoods goods = goodsMapper.selectByPrimaryKey(id);
			//修改商品状态
			goods.setAuditStatus(status);
			//更新商品信息到数据库
			goodsMapper.updateByPrimaryKey(goods);
			//修改 sku 的状态
			TbItemExample example = new TbItemExample();
			TbItemExample.Criteria criteria = example.createCriteria();
			criteria.andGoodsIdEqualTo(id);
			List<TbItem> itemList = itemMapper.selectByExample(example);
			//遍历 sku 集合
			for (TbItem item : itemList) {
				//修改状态
				item.setStatus("1");
				itemMapper.updateByPrimaryKey(item);
			}
		}

	}

	public void updateMarketable(Long id, String marketable) {
		TbGoods goods = goodsMapper.selectByPrimaryKey(id);
		goods.setIsMarketable(marketable);
		goodsMapper.updateByPrimaryKey(goods);
		TbItemExample example = new TbItemExample();
		TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andGoodsIdEqualTo(id);
		List<TbItem> items = itemMapper.selectByExample(example);
		for (TbItem item : items){
			item.setStatus(marketable);
			itemMapper.updateByPrimaryKey(item);
		}
	}

	public List<TbItem> findItemListByGoodsIdAndStatus(Long[] goodsIds, String status) {
		TbItemExample example = new TbItemExample();
		TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andGoodsIdIn(Arrays.asList(goodsIds));
		criteria.andStatusEqualTo(status);
		return itemMapper.selectByExample(example);
	}


}
