package com.offcn.jms;

import com.alibaba.fastjson.JSON;
import com.offcn.pojo.TbItem;
import com.offcn.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.List;

@Component
public class ItemSearchListener implements MessageListener {

	@Autowired
	private ItemSearchService searchService;

	public void onMessage(Message message) {
		System.out.println("接收请求");
		if (message instanceof TextMessage){
			TextMessage textMessage = (TextMessage) message;
			try {
				String text = textMessage.getText();
				List<TbItem> list = JSON.parseArray(text, TbItem.class);
				/*for (TbItem item :
						list) {
					System.out.println(item.getTitle());
					//读取SKU对应的规格转换JSON对象
					Map<String, Object> specMap = JSON.parseObject(item.getSpec());
					Map map = new HashMap();
					for (String key :
							specMap.keySet()) {
						map.put(Pinyin.toPinyin(key, "").toLowerCase(), specMap.get(key));
					}
					item.setSpecMap(map);
				}*/
				for (TbItem item :
						list) {
					System.out.println(item.getTitle());
				}
					//searchService.importSolrFromItemList(list);
			} catch (JMSException e) {
				e.printStackTrace();
			}
		}
	}
}
