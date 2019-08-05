package com.offcn.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.offcn.entity.Result;
import com.offcn.order.service.OrderService;
import com.offcn.pay.service.AliPayService;
import com.offcn.pojo.TbPayLog;
import com.offcn.utli.IdWorker;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/pay")
public class PayController {

	@Reference
	private AliPayService aliPayService;

	@Reference
	private OrderService orderService;

	@RequestMapping("/createNative")
	public Map createNative(){
		String userId = SecurityContextHolder.getContext().getAuthentication().getName();
		TbPayLog payLog = orderService.searchPayLogFromRedis(userId);
		if (payLog != null)
			return aliPayService.createNative(payLog.getOutTradeNo(), payLog.getTotalFee()+"");
		return new HashMap();
	}

	@RequestMapping("/queryPayStatus")
	public Result queryPayStatus(String out_trade_no){
		System.err.println("out_trade_no:" + out_trade_no);
		Result result = null;
		int x = 0;
		Map<String, String> map = null;
		while (true){
			try {
				map = aliPayService.queryPayStatus(out_trade_no);
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("调用查询服务出错");
			}
			if (map == null) {
				return new Result(false, "支付出错");
			}else if ("TRADE_SUCCESS".equals(map.get("tradestatus"))) {
				//修改订单状态
				orderService.updateOrderStatus(out_trade_no, map.get("trade_no"));
				return new Result(true, "支付成功");
			}else if ("TRADE_CLOSED".equals(map.get("tradestatus"))) {
				return new Result(false, "支付关闭");
			}
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			x ++;

			if (x >= 60) {
				System.err.println("超时撤销交易");
				if (aliPayService.tradeCancel(out_trade_no))
					System.out.println("撤销成功");
				else
					System.err.println("撤销失败");
				return new Result(false, "二维码超时");
			}
		}

	}

}
