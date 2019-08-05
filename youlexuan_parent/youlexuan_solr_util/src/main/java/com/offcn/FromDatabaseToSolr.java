package com.offcn;

import com.alibaba.fastjson.JSON;
import com.github.promeg.pinyinhelper.Pinyin;
import com.offcn.mapper.TbItemMapper;
import com.offcn.pojo.TbItem;
import com.offcn.pojo.TbItemExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class FromDatabaseToSolr {

	@Autowired
	private TbItemMapper itemMapper;

	@Autowired
	private SolrTemplate solrTemplate;

	public void importSolr() {
		TbItemExample example = new TbItemExample();
		TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andStatusEqualTo("1");//已审核通过的商品
		List<TbItem> itemList = itemMapper.selectByExample(example);
		//遍历全部通过审核商品列表数据
		for (TbItem item : itemList) {
			System.out.println(item.getTitle());
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

	public void deleteAllSolr() {
		Query query = new SimpleQuery("*:*");
		solrTemplate.delete(query);
		solrTemplate.commit();
	}

}
