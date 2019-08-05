package com.offcn.pay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradeCancelRequest;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeCancelResponse;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.offcn.pay.service.AliPayService;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class AliPayServiceImpl implements AliPayService {

	@Autowired
	private AlipayClient alipayClient;

	public Map createNative(String out_trade_no, String total_fee) {
		System.err.println(out_trade_no + "::::::" + total_fee);
		Map<String,String> map=new HashMap<String, String>();

		//创建预下单请求对象
		AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();

		//转换字符串金额为数字，分，整数
		long total=Long.parseLong(total_fee);
		//转换分为高精度数据
		BigDecimal bigTotal = BigDecimal.valueOf(total);
		//创建高精度除数100
		BigDecimal cs = BigDecimal.valueOf(100L);
		//进行除法运算，计算元
		BigDecimal bigYuan = bigTotal.divide(cs);
		System.out.println("预下单金额:"+bigYuan.doubleValue());
		//设定余下但请求参数
		request.setBizContent("{" +
				"    \"out_trade_no\":\""+out_trade_no+"\"," +
				"    \"total_amount\":\""+bigYuan.doubleValue()+"\"," +
				"    \"subject\":\"优乐选商城测试商品\"," +
				"    \"store_id\":\"NJ_001\"," +
				"    \"timeout_express\":\"90m\"}");
		//调用alipayClient，发出预下单请求
		try {
			AlipayTradePrecreateResponse response = alipayClient.execute(request);
			//从响应结果对象获取响应状态码
			String code = response.getCode();
			System.out.println("响应状态码:"+code);
			//返回结果
			String body = response.getBody();
			System.out.println("返回全部结果:"+body);
			//如果状态码是10000表示响应成功
			if("10000".equals(code)){
				//获取各个返回数据，封装map
				//预下单生成二维码字符串
				map.put("qrcode",response.getQrCode());
				//订单编号
				map.put("out_trade_no",response.getOutTradeNo());
				//订单费用 注意单位 分
				map.put("total_fee",total_fee);

			}else {
				System.out.println("预下单接口请求失败"+body);
			}
		} catch (AlipayApiException e) {
			e.printStackTrace();
		}
		return map;
	}

	public Map queryPayStatus(String out_trade_no) {
		System.err.println(out_trade_no);
		Map<String, String> map = new HashMap<String, String>();
		AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
		request.setBizContent("{" +
				"\"out_trade_no\":\"" + out_trade_no + "\"," +
				"\"trade_no\":\"\"}");
		try {
			AlipayTradeQueryResponse response = alipayClient.execute(request);
			String code = response.getCode();
			System.out.println("查询响应码：" + code);
			System.out.println("响应内容：" + response.getBody());
			if (code.equals("10000")){
				map.put("out_trade_no", out_trade_no);
				map.put("tradestatus", response.getTradeStatus());
				map.put("trade_no", response.getTradeNo());
			}

		} catch (AlipayApiException e) {
			e.printStackTrace();
		}
		return map;
	}

	public boolean tradeCancel(String out_trade_no) {
		AlipayTradeCancelRequest request = new AlipayTradeCancelRequest();
		request.setBizContent("{" +
				"\"out_trade_no\":\"" + out_trade_no + "\"," +
				"\"trade_no\":\"\"" +
				"  }");
		try {
			AlipayTradeCancelResponse response = alipayClient.execute(request);
			return response.isSuccess();
		} catch (AlipayApiException e) {
			e.printStackTrace();
			return false;
		}
	}


}
