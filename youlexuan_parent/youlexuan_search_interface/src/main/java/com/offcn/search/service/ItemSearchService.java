package com.offcn.search.service;

import com.offcn.pojo.TbItem;

import java.util.List;
import java.util.Map;

public interface ItemSearchService {

	/**
	 * 搜索
	 * @param searchMap
	 * @return
	 */
	public Map<String, Object> search(Map searchMap);

	public void importSolrFromItemList(List<TbItem> itemList);

	public void deleteByGoodsIds(List goodsIdList);
}
