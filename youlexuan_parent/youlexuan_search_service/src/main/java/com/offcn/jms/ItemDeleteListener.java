package com.offcn.jms;

import com.offcn.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import java.util.Arrays;

@Component
public class ItemDeleteListener implements MessageListener {

	@Autowired
	private ItemSearchService itemSearchService;

	public void onMessage(Message message) {
		if (message instanceof ObjectMessage){
			ObjectMessage objectMessage = (ObjectMessage) message;
			try {
				Long[] ids = (Long[]) objectMessage.getObject();
				System.out.print("删除请求...");
				itemSearchService.deleteByGoodsIds(Arrays.asList(ids));
				System.out.println("OK");
			} catch (JMSException e) {
				e.printStackTrace();
			}
		}
	}
}
