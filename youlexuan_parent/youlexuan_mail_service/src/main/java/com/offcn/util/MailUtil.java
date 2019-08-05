package com.offcn.util;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Component
public class MailUtil {

	//private final ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("/spring/applicationContext-mail.xml");

	//private final JavaMailSender sendMail = (JavaMailSender) context.getBean("sendMail");

	@Resource
	private JavaMailSender sendMail;

	public MimeMessageHelper getMimeMessageHelper(){
		MimeMessage message = sendMail.createMimeMessage();
		try {
			return new MimeMessageHelper(message, true);
		} catch (MessagingException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void sendMail (MimeMessageHelper helper){
		try {
			helper.setFrom("logo_qineryi@163.com");
		} catch (MessagingException e) {
			e.printStackTrace();
		}
		sendMail.send(helper.getMimeMessage());
	}

}
