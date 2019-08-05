package com.offcn.order.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.offcn.entity.Cart;
import com.offcn.entity.PageResult;
import com.offcn.mapper.TbOrderItemMapper;
import com.offcn.mapper.TbOrderMapper;
import com.offcn.mapper.TbPayLogMapper;
import com.offcn.order.service.OrderService;
import com.offcn.pojo.TbOrder;
import com.offcn.pojo.TbOrderExample;
import com.offcn.pojo.TbOrderExample.Criteria;
import com.offcn.pojo.TbOrderItem;
import com.offcn.pojo.TbPayLog;
import com.offcn.utli.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 服务实现层
 *
 * @author Administrator
 */
@Service
public class OrderServiceImpl implements OrderService {

	@Autowired
	private TbOrderMapper orderMapper;

	@Autowired
	private RedisTemplate redisTemplate;

	@Autowired
	private TbOrderItemMapper orderItemMapper;

	@Autowired
	private IdWorker idWorker;

	@Autowired
	private TbPayLogMapper payLogMapper;

	/**
	 * 查询全部
	 */
	public List<TbOrder> findAll() {
		return orderMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		Page<TbOrder> page = (Page<TbOrder>) orderMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	public void add(TbOrder order) {
		//orderMapper.insert(order);
		//获取购物车数据
		List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(order.getUserId());
		List<String> orderIdList = new ArrayList<String>();
		double total_money = 0;
		for (Cart cart :
				cartList) {
			//创建订单对象
			TbOrder tbOrder = new TbOrder();
			long orderId = idWorker.nextId();
			System.err.println(orderId);
			tbOrder.setOrderId(orderId);
			//购买用户ID
			tbOrder.setUserId(order.getUserId());
			//支付方式 1扫码支付 2货到付款
			tbOrder.setPaymentType(order.getPaymentType());
			//订单状态
			tbOrder.setStatus("1");
			//订单创建时间
			tbOrder.setCreateTime(new Date());
			//订单更新时间
			tbOrder.setUpdateTime(new Date());
			//订单收货地址
			tbOrder.setReceiverAreaName(order.getReceiverAreaName());
			//收货人
			tbOrder.setReceiver(order.getReceiver());
			//手机号码
			tbOrder.setReceiverMobile(order.getReceiverMobile());
			//订单来源
			tbOrder.setSourceType(order.getSourceType());
			//商家ID
			tbOrder.setSellerId(cart.getSellerId());
			//支付金额
			double money = 0;
			//循环购物明细
			for (TbOrderItem orderItem :
					cart.getOrderItemList()) {
				//关联订单明细和订单编号
				orderItem.setId(idWorker.nextId());
				//设置订单ID
				orderItem.setOrderId(orderId);
				//设置商家ID
				orderItem.setSellerId(cart.getSellerId());
				//保存订单明细到数据库
				orderItemMapper.insert(orderItem);
				money += orderItem.getTotalFee().doubleValue();
			}
			tbOrder.setPayment(new BigDecimal(money));
			//支付明细
			orderIdList.add(orderId + "");
			total_money += money;
			//保存订单数据到数据库
			orderMapper.insert(tbOrder);
			//清空购物车
			redisTemplate.boundHashOps("cartList").delete(order.getUserId());
		}
		if ("1".equals(order.getPaymentType())){
			TbPayLog payLog = new TbPayLog();
			String outTradeNo = idWorker.nextId() + "";
			payLog.setOutTradeNo(outTradeNo);
			payLog.setCreateTime(new Date());
			String ids = orderIdList.toString().replace("[", "").replace("]", "").replace(" ", "");
			payLog.setOrderList(ids);
			payLog.setPayType("1");
			System.out.println("合计金额:" + total_money);
			BigDecimal bigDecimal = BigDecimal.valueOf(total_money);
			BigDecimal s = BigDecimal.valueOf(100L);
			BigDecimal multiply = bigDecimal.multiply(s);
			payLog.setTotalFee(multiply.toBigInteger().longValue());
			payLog.setTradeState("0");
			payLog.setUserId(order.getUserId());
			payLogMapper.insert(payLog);
			redisTemplate.boundHashOps("payLog").put(order.getUserId(), payLog);
		}
	}


	/**
	 * 修改
	 */
	public void update(TbOrder order) {
		orderMapper.updateByPrimaryKey(order);
	}

	/**
	 * 根据ID获取实体
	 *
	 * @param
	 * @return
	 */
	public TbOrder findOne(Long orderId) {
		return orderMapper.selectByPrimaryKey(orderId);
	}

	/**
	 * 批量删除
	 */
	public void delete(Long[] orderIds) {
		for (Long orderId : orderIds) {
			orderMapper.deleteByPrimaryKey(orderId);
		}
	}


	public PageResult findPage(TbOrder order, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);

		TbOrderExample example = new TbOrderExample();
		Criteria criteria = example.createCriteria();

		if (order != null) {
			if (order.getPaymentType() != null && order.getPaymentType().length() > 0) {
				criteria.andPaymentTypeLike("%" + order.getPaymentType() + "%");
			}
			if (order.getPostFee() != null && order.getPostFee().length() > 0) {
				criteria.andPostFeeLike("%" + order.getPostFee() + "%");
			}
			if (order.getStatus() != null && order.getStatus().length() > 0) {
				criteria.andStatusLike("%" + order.getStatus() + "%");
			}
			if (order.getShippingName() != null && order.getShippingName().length() > 0) {
				criteria.andShippingNameLike("%" + order.getShippingName() + "%");
			}
			if (order.getShippingCode() != null && order.getShippingCode().length() > 0) {
				criteria.andShippingCodeLike("%" + order.getShippingCode() + "%");
			}
			if (order.getUserId() != null && order.getUserId().length() > 0) {
				criteria.andUserIdLike("%" + order.getUserId() + "%");
			}
			if (order.getBuyerMessage() != null && order.getBuyerMessage().length() > 0) {
				criteria.andBuyerMessageLike("%" + order.getBuyerMessage() + "%");
			}
			if (order.getBuyerNick() != null && order.getBuyerNick().length() > 0) {
				criteria.andBuyerNickLike("%" + order.getBuyerNick() + "%");
			}
			if (order.getBuyerRate() != null && order.getBuyerRate().length() > 0) {
				criteria.andBuyerRateLike("%" + order.getBuyerRate() + "%");
			}
			if (order.getReceiverAreaName() != null && order.getReceiverAreaName().length() > 0) {
				criteria.andReceiverAreaNameLike("%" + order.getReceiverAreaName() + "%");
			}
			if (order.getReceiverMobile() != null && order.getReceiverMobile().length() > 0) {
				criteria.andReceiverMobileLike("%" + order.getReceiverMobile() + "%");
			}
			if (order.getReceiverZipCode() != null && order.getReceiverZipCode().length() > 0) {
				criteria.andReceiverZipCodeLike("%" + order.getReceiverZipCode() + "%");
			}
			if (order.getReceiver() != null && order.getReceiver().length() > 0) {
				criteria.andReceiverLike("%" + order.getReceiver() + "%");
			}
			if (order.getInvoiceType() != null && order.getInvoiceType().length() > 0) {
				criteria.andInvoiceTypeLike("%" + order.getInvoiceType() + "%");
			}
			if (order.getSourceType() != null && order.getSourceType().length() > 0) {
				criteria.andSourceTypeLike("%" + order.getSourceType() + "%");
			}
			if (order.getSellerId() != null && order.getSellerId().length() > 0) {
				criteria.andSellerIdLike("%" + order.getSellerId() + "%");
			}
		}

		Page<TbOrder> page = (Page<TbOrder>) orderMapper.selectByExample(example);
		return new PageResult(page.getTotal(), page.getResult());
	}

	public TbPayLog searchPayLogFromRedis(String userId) {
		return (TbPayLog) redisTemplate.boundHashOps("payLog").get(userId);
	}

	public void updateOrderStatus(String out_trade_no, String transaction_id) {
		TbPayLog payLog = payLogMapper.selectByPrimaryKey(out_trade_no);
		payLog.setPayTime(new Date());
		payLog.setTradeState("1");
		payLog.setTransactionId(transaction_id);
		payLogMapper.updateByPrimaryKey(payLog);
		String[] orderIds = payLog.getOrderList().split(",");
		for (String id :
				orderIds) {
			TbOrder order = orderMapper.selectByPrimaryKey(Long.parseLong(id));
			if (order !=null){
				order.setStatus("2");
				orderMapper.updateByPrimaryKey(order);
			}
		}
		redisTemplate.boundHashOps("payLog").delete(payLog.getUserId());
	}

}
