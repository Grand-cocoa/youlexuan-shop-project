package com.offcn.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.promeg.pinyinhelper.Pinyin;
import com.offcn.pojo.TbItem;
import com.offcn.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service(timeout = 3000)
@org.springframework.stereotype.Service
public class ItemSearchServiceImpl implements ItemSearchService {

	@Autowired
	private SolrTemplate solrTemplate;

	@Autowired
	private RedisTemplate redisTemplate;

	public Map<String, Object> search(Map searchMap) {
		Map<String, Object> map = new HashMap<String, Object>();
		if (searchMap == null || searchMap.get("keywords") == null || "".equals(searchMap.get("keywords"))){
			Query query = new SimpleQuery("*:*");
			page(searchMap, query);
			sort(searchMap, query);
			ScoredPage<TbItem> page = solrTemplate.queryForPage(query, TbItem.class);
			map.put("rows", page.getContent());
			//存放集合到map
			map.put("rows", page.getContent());
			map.put("totalPages", page.getTotalPages());
			map.put("total", page.getTotalElements());
			return map;
		}else {
			List list = searchListCategoryList(searchMap);
			map.put("categoryList", list);
			String category = (String) searchMap.get("category");
			if (!"".equals(category) && category != null){
				map.putAll(searchBrandAndSpecList(category));
			}else {
				if (list.size() > 0) {
					map.putAll(searchBrandAndSpecList((String) list.get(0)));
				}
			}
			//map.putAll(searchListTest(searchMap));
			map.putAll(searchList(searchMap));
			return map;
		}
	}

	public void importSolrFromItemList(List<TbItem> itemList) {
		for (TbItem item :
				itemList) {
			System.out.println("title\t==>\t" + item.getTitle());
			//读取规格数据，字符串，转换成 json 对象
			Map<String, String> specMap = JSON.parseObject(item.getSpec(), Map.class);
			//创建一个新 map 集合存储拼音
			Map<String, String> mapPinyin = new HashMap<String, String>();
			//遍历 map，替换 key 从汉字变为拼音
			for (String key : specMap.keySet()) {
				mapPinyin.put(Pinyin.toPinyin(key, "").toLowerCase(), specMap.get(key));
			}
			item.setSpecMap(mapPinyin);
		}
		//保存集合数据到 solr
		solrTemplate.saveBeans(itemList);
		solrTemplate.commit();
		System.out.println("保存商品数据到 solr 成功");
	}

	public void deleteByGoodsIds(List goodsIdList) {
		System.out.println("删除商品ID：" + goodsIdList);
		Query query = new SimpleQuery();
		Criteria criteria = new Criteria("item_goodsId").in(goodsIdList);
		query.addCriteria(criteria);
		solrTemplate.delete(query);
		solrTemplate.commit();
	}

	/**
	 * 高亮查询
	 * @param searchMap
	 * @return
	 */
	private Map searchList (Map searchMap){
		Map map = new HashMap();
		String keywords = (String) searchMap.get("keywords");
		searchMap.put("keywords", keywords.replace(" ", ""));
		//创建支持高亮查询对象
		SimpleHighlightQuery query = new SimpleHighlightQuery();
		//设置需要高亮字段
		HighlightOptions options = new HighlightOptions();
		options.addField("item_title");
		//设置高亮前缀后缀
		options.setSimplePrefix("<em style='color:red'>");
		options.setSimplePostfix("</em>");
		//关联高亮选项到高亮查询器对象
		query.setHighlightOptions(options);
		//设置查询条件
		Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
		query.addCriteria(criteria);
		//添加查询类别
		if (!"".equals(searchMap.get("category")) && searchMap.get("category") != null){
			Criteria conditions = new Criteria("item_category").is(searchMap.get("category"));
			SimpleFacetQuery facetQuery = new SimpleFacetQuery(conditions);
			query.addFilterQuery(facetQuery);
		}
		//添加查询品牌
		if (!"".equals(searchMap.get("brand")) && searchMap.get("brand") != null){
			Criteria conditions = new Criteria("item_brand").is(searchMap.get("brand"));
			SimpleFacetQuery facetQuery = new SimpleFacetQuery(conditions);
			query.addFilterQuery(facetQuery);
		}
		//添加查询扩展信息
		if (!"".equals(searchMap.get("spec")) && searchMap.get("spec") != null){
			Map<String, String> spec = (Map<String, String>) searchMap.get("spec");
			for (String key : spec.keySet()){
				String s = Pinyin.toPinyin(key, "").toLowerCase();
				spec.get(key);
				Criteria conditions = new Criteria("item_spec_" + s).is(spec.get(key));
				SimpleFacetQuery facetQuery = new SimpleFacetQuery(conditions);
				query.addFilterQuery(facetQuery);
			}
		}
		//添加价格条件
		if (!"".equals(searchMap.get("price")) && searchMap.get("price") != null){
			String price = (String) searchMap.get("price");
			String[] split = price.split("-");
			if (!split.equals("0")){
				//设置开始价格
				Criteria greaterThanEqual = new Criteria("item_price").greaterThanEqual(split[0]);
				SimpleFacetQuery facetQuery = new SimpleFacetQuery(greaterThanEqual);
				query.addFilterQuery(facetQuery);
			}
			if (!split.equals("*")){
				//设置结束价格
				Criteria lessThanEqual = new Criteria("item_price").lessThanEqual(split[1]);
				SimpleFacetQuery facetQuery = new SimpleFacetQuery(lessThanEqual);
				query.addFilterQuery(facetQuery);
			}
		}
		//添加分页条件
		page(searchMap, query);
		//排序
		sort(searchMap, query);
		//发出查询
		HighlightPage<TbItem> items = solrTemplate.queryForHighlightPage(query, TbItem.class);
		//获取高亮集合入口
		List<HighlightEntry<TbItem>> highlightEntryList = items.getHighlighted();
		//遍历集合入口
		for (HighlightEntry<TbItem> entry :
				highlightEntryList) {
			//获取基本数据对象
			TbItem entity = entry.getEntity();
			if (entry.getHighlights().size() > 0 && entry.getHighlights().get(0).getSnipplets().size() > 0) {
				List<HighlightEntry.Highlight> highlights = entry.getHighlights();
				//高亮词组
				List<String> snipplets = highlights.get(0).getSnipplets();
				//设置结果到商品标题
				entity.setTitle(snipplets.get(0));
			}

		}

		//存放集合到map
		map.put("rows", items.getContent());
		map.put("totalPages", items.getTotalPages());
		map.put("total", items.getTotalElements());

		return map;
	}

	/**
	 * 分类查询
	 * @param searchMap
	 * @return
	 */
	private List searchListCategoryList(Map searchMap){
		List list = new ArrayList();
		//创建查询器对象
		SimpleQuery query = new SimpleQuery();
		//创建查询条件
		Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
		query.addCriteria(criteria);
		//创建分组设置
		GroupOptions groupOptions = new GroupOptions();
		//设置分组字段
		groupOptions.addGroupByField("item_category");
		query.setGroupOptions(groupOptions);
		//发出分组查询请求
		GroupPage<TbItem> groupPage = solrTemplate.queryForGroupPage(query, TbItem.class);
		//获取分组字段结果
		GroupResult<TbItem> groupResult = groupPage.getGroupResult("item_category");
		//获取分组入口
		Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();
		List<GroupEntry<TbItem>> groupEntryList = groupEntries.getContent();
		for (GroupEntry<TbItem> groupEntrie :
				groupEntryList) {
			list.add(groupEntrie.getGroupValue());
		}
		return list;
	}

	/**
	 * 根据指定类目返回品牌列表
	 * @param category
	 * @return
	 */
	private Map searchBrandAndSpecList(String category){
		Map map = new HashMap();
		//从redis读取类目，根据类目获取模板ID
		Long typeId = (Long) redisTemplate.boundHashOps("itemCat").get(category);
		if (typeId != null){
			//读取品牌数据集合
			List brandList = (List) redisTemplate.boundHashOps("brandList").get(typeId);
			map.put("brandList", brandList);
			//
			List specList = (List) redisTemplate.boundHashOps("specList").get(typeId);
			map.put("specList", specList);
		}
		return map;
	}

	/**
	 * 高亮查询Plus
	 * @param searchMap
	 * @return
	 */
	private Map searchListTest (Map searchMap){
		Map map = new HashMap();
		//创建支持高亮查询对象
		SimpleHighlightQuery query = new SimpleHighlightQuery();
		//设置需要高亮字段
		HighlightOptions options = new HighlightOptions();
		options.addField("item_title");
		//设置高亮前缀后缀
		options.setSimplePrefix("<em style='color:red'>");
		options.setSimplePostfix("</em>");
		//关联高亮选项到高亮查询器对象
		query.setHighlightOptions(options);
		//设置查询条件
		Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
		query.addCriteria(criteria);
		//自动添加查询条件
		for (Object key :
				searchMap.keySet()) {
			String keyStr = key + "";
			if (!"".equals(searchMap.get(keyStr)) && searchMap.get(keyStr) != null){
				if (objectToMap(searchMap.get(keyStr))){
					Map map2 = (Map) searchMap.get(keyStr);
					for (Object key2 : map2.keySet()){
						String s = Pinyin.toPinyin( key2 + "", "").toLowerCase();
						//searchMap.get(keyStr);
						Criteria conditions = new Criteria("item_" + keyStr +"_" + s).is(map2.get(key2));
						SimpleFacetQuery facetQuery = new SimpleFacetQuery(conditions);
						query.addFilterQuery(facetQuery);
					}
				}else {
					Criteria conditions = new Criteria("item_" + keyStr).is(searchMap.get(keyStr));
					SimpleFacetQuery facetQuery = new SimpleFacetQuery(conditions);
					query.addFilterQuery(facetQuery);
				}
			}
		}
		
		//添加查询品牌
		if (!"".equals(searchMap.get("brand")) && searchMap.get("brand") != null){
			Criteria conditions = new Criteria("item_brand").is(searchMap.get("brand"));
			SimpleFacetQuery facetQuery = new SimpleFacetQuery(conditions);
			query.addFilterQuery(facetQuery);
		}
		//添加查询类别
		if (!"".equals(searchMap.get("category")) && searchMap.get("category") != null){
			Criteria conditions = new Criteria("item_category").is(searchMap.get("category"));
			SimpleFacetQuery facetQuery = new SimpleFacetQuery(conditions);
			query.addFilterQuery(facetQuery);
		}
		//添加查询扩展信息
		if (!"".equals(searchMap.get("spec")) && searchMap.get("spec") != null){
			Map<String, String> spec = (Map<String, String>) searchMap.get("spec");
			for (String key : spec.keySet()){
				String s = Pinyin.toPinyin(key, "").toLowerCase();
				spec.get(key);
				Criteria conditions = new Criteria("item_spec_" + s).is(spec.get(key));
				SimpleFacetQuery facetQuery = new SimpleFacetQuery(conditions);
				query.addFilterQuery(facetQuery);
			}
		}
		//发出查询
		HighlightPage<TbItem> items = solrTemplate.queryForHighlightPage(query, TbItem.class);
		//获取高亮集合入口
		List<HighlightEntry<TbItem>> highlightEntryList = items.getHighlighted();
		//遍历集合入口
		for (HighlightEntry<TbItem> entry :
				highlightEntryList) {
			//获取基本数据对象
			TbItem entity = entry.getEntity();
			if (entry.getHighlights().size() > 0 && entry.getHighlights().get(0).getSnipplets().size() > 0) {
				List<HighlightEntry.Highlight> highlights = entry.getHighlights();
				//高亮词组
				List<String> snipplets = highlights.get(0).getSnipplets();
				//设置结果到商品标题
				entity.setTitle(snipplets.get(0));
			}

		}

		//存放集合到map
		map.put("rows", items.getContent());

		return map;
	}

	/**
	 * 判定对象是否为Map集合
	 * @param map
	 * @return
	 */
	private static boolean objectToMap(Object map){
		String s = map.toString();
		if (s.indexOf('{') == s.indexOf('}')){
			return false;
		}
		return true;
	}

	/**
	 * 添加分页
	 * @param searchMap
	 * @param query
	 */
	private static void page(Map searchMap, Query query){
		//分页处理
		Integer pageNo = (Integer) searchMap.get("pageNo");
		if (pageNo == null){
			pageNo = 1;
		}
		Integer pageSize = (Integer) searchMap.get("pageSize");
		if (pageSize == null){
			pageSize = 10;
		}
		//游标开始位置
		Integer start = (pageNo - 1 )* pageSize;
		//设置游标开始位置
		query.setOffset(start);
		//设置页量
		query.setRows(pageSize);
	}

	/**
	 * 排序
	 * @param searchMap
	 * @param query
	 */
	private static void sort(Map searchMap, Query query){
		String sortValue = (String) searchMap.get("sort");
		String sortField = (String) searchMap.get("sortField");
		if (sortValue != null && !"".equals(sortField)){
			Sort sort = null;
			//判断排序方式
			if (sortValue.equals("ASC")){
				sort = new Sort(Sort.Direction.ASC, "item_" + sortField);
			}else if (sortValue.equals("DESC")){
				sort = new Sort(Sort.Direction.DESC, "item_" + sortField);
			}
			if (sort != null){
				//关联排序对象到查询器对象
				query.addSort(sort);
			}
		}
	}

}
