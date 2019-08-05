package com.offcn.page.service.impl;

import com.offcn.page.service.ItemPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

@Component
public class PageListener implements MessageListener {

	@Autowired
	private ItemPageService itemPageService;

	public void onMessage(Message message) {
		if (message instanceof TextMessage){
			TextMessage textMessage = (TextMessage) message;
			try {
				String text = textMessage.getText();
				System.out.println(text);
				boolean b = itemPageService.genItemHtml(Long.parseLong(text));
				System.out.println("静态页面状态--" + b);
			} catch (JMSException e) {
				e.printStackTrace();
			}
		}
	}
}
