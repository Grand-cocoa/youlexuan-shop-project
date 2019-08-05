package com.offcn.page.service.impl;

import com.offcn.mapper.TbGoodsDescMapper;
import com.offcn.mapper.TbGoodsMapper;
import com.offcn.mapper.TbItemCatMapper;
import com.offcn.mapper.TbItemMapper;
import com.offcn.page.service.ItemPageService;
import com.offcn.pojo.TbGoods;
import com.offcn.pojo.TbGoodsDesc;
import com.offcn.pojo.TbItem;
import com.offcn.pojo.TbItemExample;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ItemPageServiceImpl implements ItemPageService {

	@Value("${pagedir}")
	private String pageDir;

	@Autowired
	private FreeMarkerConfigurer freeMarkerConfigurer;

	@Autowired
	private TbGoodsMapper goodsMapper;

	@Autowired
	private TbGoodsDescMapper goodsDescMapper;

	@Autowired
	private TbItemCatMapper itemCatMapper;

	@Autowired
	private TbItemMapper itemMapper;

	public boolean genItemHtml(Long goodsId) {
		Configuration configuration = freeMarkerConfigurer.getConfiguration();
		try {
			Template template = configuration.getTemplate("item.ftl");
			//创建数据存储集合
			Map map = new HashMap();
			//从数据库读取商品基本信息对象
			TbGoods goods = goodsMapper.selectByPrimaryKey(goodsId);
			//把商品信息存放至集合
			map.put("goods", goods);
			TbGoodsDesc goodsDesc = goodsDescMapper.selectByPrimaryKey(goodsId);
			map.put("goodsDesc", goodsDesc);
			//读取商品分类名称
			String itemCat1 = itemCatMapper.selectByPrimaryKey(goods.getCategory1Id()).getName();
			String itemCat2 = itemCatMapper.selectByPrimaryKey(goods.getCategory2Id()).getName();
			String itemCat3 = itemCatMapper.selectByPrimaryKey(goods.getCategory3Id()).getName();
			map.put("itemCat1", itemCat1);
			map.put("itemCat2", itemCat2);
			map.put("itemCat3", itemCat3);

			TbItemExample example = new TbItemExample();
			TbItemExample.Criteria criteria = example.createCriteria();
			criteria.andGoodsIdEqualTo(goodsId);
			criteria.andStatusEqualTo("1");
			example.setOrderByClause("is_default desc");
			List<TbItem> itemList = itemMapper.selectByExample(example);

			map.put("itemList", itemList);

			//创建输出文件
			FileWriter fileWriter = new FileWriter(pageDir + goodsId + ".html");
			//调用模板引擎
			template.process(map, fileWriter);
			//关闭文件资源
			fileWriter.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} catch (TemplateException e) {
			e.printStackTrace();
			return false;
		}
	}
}
