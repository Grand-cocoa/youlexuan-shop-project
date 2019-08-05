package com.offcn.page.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import java.io.File;

@Component
public class PageDeleteListener implements MessageListener {

	@Value("${pagedir}")
	private String pageDir;

	public void onMessage(Message message) {
		if (message instanceof ObjectMessage){
			ObjectMessage objectMessage = (ObjectMessage) message;
			try {
				Long[] list = (Long[]) objectMessage.getObject();
				for (Long l :
						list) {
					new File(pageDir + l + ".html").delete();
				}
			} catch (JMSException e) {
				e.printStackTrace();
			}
		}
	}
}
