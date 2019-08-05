package com.offcn.util;

import com.aliyuncs.CommonResponse;
import com.aliyuncs.exceptions.ClientException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;

@Component
public class SmsListener implements MessageListener {

	@Autowired
	private SmsUtil smsUtil;

	public void onMessage(Message message) {
		if (message instanceof MapMessage){
			MapMessage mapMessage = (MapMessage) message;
			try {
				String mobile = mapMessage.getString("mobile");
				System.out.println("mobile=" + mobile);
				String code = mapMessage.getString("code");
				System.out.println("code=" + code);
				String sign_name = mapMessage.getString("sign_name");
				System.out.println("sign_name=" + sign_name);
				String param = mapMessage.getString("param");
				System.out.println("param=" + param);
				CommonResponse commonResponse = smsUtil.sendSms(mobile, code, sign_name, param);
				System.out.println(commonResponse.getData());
			} catch (JMSException e) {
				e.printStackTrace();
			} catch (ClientException e) {
				e.printStackTrace();
			}
		}
	}
}
