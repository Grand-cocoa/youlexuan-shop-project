package com.offcn.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.mail.MessagingException;

@Component
public class RegisterMail implements MessageListener {

	@Autowired
	private MailUtil mailUtil;

	public void onMessage(Message message) {
		if (message instanceof TextMessage){
			TextMessage textMessage = (TextMessage) message;
			try {
				String mail = textMessage.getText();
				MimeMessageHelper helper = mailUtil.getMimeMessageHelper();
				helper.setTo(mail);
				helper.setSubject("欢迎注册优乐选商城");
				helper.setText("<!DOCTYPE html>\n" +
						"<html lang=\"en\">\n" +
						"<head>\n" +
						"\t<meta charset=\"UTF-8\">\n" +
						"\t<title>Title</title>\n" +
						"</head>\n" +
						"<body>\n" +
						"\t<img src=\"https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&" +
						"sec=1563895111594&di=c9fc1e181f262e6a3d2c03a47ec737e0&imgtype=0&src=http%3A%2" +
						"F%2Fku.90sjimg.com%2Felement_origin_min_pic%2F17%2F07%2F13%2F5208c0414712f8a2" +
						"fb6f2a83b149e314.jpg%2521%2Ffwfh%2F804x536%2Fquality%2F90%2Funsharp%2Ftrue%2F" +
						"compress%2Ftrue\"><br><br>\n" +
						"\t<p>恭喜您已成功注册优乐选商城，请继续支持本站吧！</p>\n" +
						"</body>\n" +
						"</html>", true);
				mailUtil.sendMail(helper);
			} catch (JMSException e) {
				e.printStackTrace();
			} catch (MessagingException e) {
				e.printStackTrace();
			}
		}
	}
}
