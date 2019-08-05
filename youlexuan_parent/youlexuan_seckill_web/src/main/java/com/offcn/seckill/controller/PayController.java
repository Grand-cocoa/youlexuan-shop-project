package com.offcn.seckill.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.offcn.entity.Result;
import com.offcn.pay.service.AliPayService;
import com.offcn.pojo.TbSeckillOrder;
import com.offcn.seckill.service.SeckillOrderService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/pay")
public class PayController {

	//注入支付相关
	@Reference
	private AliPayService aliPayService;

	//秒杀下单服务
	@Reference
	private SeckillOrderService seckillOrderService;

	//发出与下单请求
	@RequestMapping("/createNative")
	public Map createNative(){
		//获取当前登录用户名
		String name = SecurityContextHolder.getContext().getAuthentication().getName();
		//根据用户名从Redis读取秒杀订单
		TbSeckillOrder seckillOrder = seckillOrderService.findSeckillOrderFromRedis(name);
		//判空
		if (seckillOrder != null){
			//获取秒杀订单金额
			Long fen = (long) (seckillOrder.getMoney().doubleValue() * 100);
			//调用支付服务生成二维码串
			return aliPayService.createNative(seckillOrder.getId() + "", fen + "");
		}else {
			return new HashMap();
		}
	}
	@RequestMapping("/queryPayStatus")
	public Result queryPayStatus(String out_trade_no){
		System.err.println("out_trade_no:" + out_trade_no);
		int x = 0;
		Map<String, String> map = null;
		while (true){
			try {
				map = aliPayService.queryPayStatus(out_trade_no);
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("调用查询服务出错");
			}
			if (map == null)
				return new Result(false, "支付出错");
			else if ("TRADE_SUCCESS".equals(map.get("tradestatus"))) {
				//修改订单状态
				seckillOrderService.saveOrderFromRedisToDb(SecurityContextHolder.getContext().getAuthentication().getName(), Long.valueOf(map.get("out_trade_no")), map.get("trade_no"));
				return new Result(true, "支付成功");
			}else if ("TRADE_CLOSED".equals(map.get("tradestatus")))
				return new Result(false, "支付关闭");

			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			x ++;

			if (x >= 60) {
				System.err.println("超时撤销交易");
				seckillOrderService.deleteOrderFrom(SecurityContextHolder.getContext().getAuthentication().getName(), Long.valueOf(out_trade_no));
				if (aliPayService.tradeCancel(out_trade_no))
					System.out.println("撤销成功");
				else
					System.err.println("撤销失败");
				return new Result(false, "二维码超时");
			}
		}

	}

}
