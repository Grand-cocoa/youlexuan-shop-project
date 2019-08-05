package com.offcn.seckill.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.offcn.entity.PageResult;
import com.offcn.mapper.TbSeckillGoodsMapper;
import com.offcn.mapper.TbSeckillOrderMapper;
import com.offcn.pojo.TbSeckillGoods;
import com.offcn.pojo.TbSeckillOrder;
import com.offcn.pojo.TbSeckillOrderExample;
import com.offcn.pojo.TbSeckillOrderExample.Criteria;
import com.offcn.seckill.service.SeckillOrderService;
import com.offcn.utli.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;

import java.util.Date;
import java.util.List;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class SeckillOrderServiceImpl implements SeckillOrderService {

	@Autowired
	private TbSeckillOrderMapper seckillOrderMapper;

	@Autowired
	private RedisTemplate redisTemplate;

	@Autowired
	private IdWorker idWorker;

	@Autowired
	private TbSeckillGoodsMapper seckillGoodsMapper;
	
	/**
	 * 查询全部
	 */
	public List<TbSeckillOrder> findAll() {
		return seckillOrderMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbSeckillOrder> page=   (Page<TbSeckillOrder>) seckillOrderMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	public void add(TbSeckillOrder seckillOrder) {
		seckillOrderMapper.insert(seckillOrder);		
	}

	
	/**
	 * 修改
	 */
	public void update(TbSeckillOrder seckillOrder){
		seckillOrderMapper.updateByPrimaryKey(seckillOrder);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	public TbSeckillOrder findOne(Long id){
		return seckillOrderMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	public void delete(Long[] ids) {
		for(Long id:ids){
			seckillOrderMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
	public PageResult findPage(TbSeckillOrder seckillOrder, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbSeckillOrderExample example=new TbSeckillOrderExample();
		Criteria criteria = example.createCriteria();
		
		if(seckillOrder!=null){			
						if(seckillOrder.getUserId()!=null && seckillOrder.getUserId().length()>0){
				criteria.andUserIdLike("%"+seckillOrder.getUserId()+"%");
			}			if(seckillOrder.getSellerId()!=null && seckillOrder.getSellerId().length()>0){
				criteria.andSellerIdLike("%"+seckillOrder.getSellerId()+"%");
			}			if(seckillOrder.getStatus()!=null && seckillOrder.getStatus().length()>0){
				criteria.andStatusLike("%"+seckillOrder.getStatus()+"%");
			}			if(seckillOrder.getReceiverAddress()!=null && seckillOrder.getReceiverAddress().length()>0){
				criteria.andReceiverAddressLike("%"+seckillOrder.getReceiverAddress()+"%");
			}			if(seckillOrder.getReceiverMobile()!=null && seckillOrder.getReceiverMobile().length()>0){
				criteria.andReceiverMobileLike("%"+seckillOrder.getReceiverMobile()+"%");
			}			if(seckillOrder.getReceiver()!=null && seckillOrder.getReceiver().length()>0){
				criteria.andReceiverLike("%"+seckillOrder.getReceiver()+"%");
			}			if(seckillOrder.getTransactionId()!=null && seckillOrder.getTransactionId().length()>0){
				criteria.andTransactionIdLike("%"+seckillOrder.getTransactionId()+"%");
			}	
		}
		
		Page<TbSeckillOrder> page= (Page<TbSeckillOrder>)seckillOrderMapper.selectByExample(example);
		return new PageResult(page.getTotal(), page.getResult());
	}

	public void submitOrder(final long seckillId, final String userId) {
		//开启Redis事务
		redisTemplate.setEnableTransactionSupport(true);
		redisTemplate.execute(new SessionCallback() {
			public Object execute(RedisOperations operations) throws DataAccessException {
				//监控Redis数据库秒杀商品的数据
				redisTemplate.watch("seckillGoods");
				//Redis事务开启前读取秒杀商品信息
				TbSeckillGoods seckillGoods = (TbSeckillGoods) operations.boundHashOps("seckillGoods").get(seckillId);
				//开启事务
				operations.multi();
				//配合事务进行必要的空查询
				operations.boundHashOps("seckillGoods").get(seckillId);
				if (seckillGoods == null)
					throw new RuntimeException("秒杀商品不存在");
				if (seckillGoods.getStockCount() <= 0)
					throw new RuntimeException("商品已被抢光");
				//扣减库存
				seckillGoods.setStockCount(seckillGoods.getStockCount() - 1);
				//更新库存信息到Redis
				operations.boundHashOps("seckillGoods").put(seckillId, seckillGoods);
				if (seckillGoods.getStockCount() == 0) {
					seckillGoodsMapper.updateByPrimaryKey(seckillGoods);
					operations.boundHashOps("seckillGoods").delete(seckillId);
				}
				//保存订单
				TbSeckillOrder seckillOrder = new TbSeckillOrder();
				seckillOrder.setId(idWorker.nextId());
				seckillOrder.setSeckillId(seckillId);
				seckillOrder.setMoney(seckillGoods.getCostPrice());
				seckillGoods.setSellerId(seckillGoods.getSellerId());
				seckillOrder.setUserId(userId);
				seckillOrder.setStatus("0");
				operations.boundHashOps("seckillOrder").put(userId, seckillOrder);
				return operations.exec();
			}
		});
	}

	public TbSeckillOrder findSeckillOrderFromRedis(String userId) {
		return (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
	}

	public void saveOrderFromRedisToDb(String userId, Long orderId, String aliId) {
		System.err.println("userID:" + userId + "\torderID:" + orderId);
		//根据用户ID查询秒杀订单
		TbSeckillOrder seckillOrder = (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
		if (seckillOrder == null)
			throw new RuntimeException("订单不存在");
		if (seckillOrder.getId().longValue() != orderId.longValue())
			throw new RuntimeException("订单状态异常");
		//更新订单交易流水号
		seckillOrder.setTransactionId(aliId);
		//更新支付事件
		seckillOrder.setPayTime(new Date());
		//更新支付状态
		seckillOrder.setStatus("1");
		//保存订单到数据库
		seckillOrderMapper.insert(seckillOrder);
		//清除缓存用户信息
		redisTemplate.boundHashOps("seckillOrder").delete(userId);
	}

	public void deleteOrderFrom(String userId, Long orderId) {
		TbSeckillOrder seckillOrder = (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
		if (seckillOrder != null && seckillOrder.getId().longValue() == orderId.longValue()) {
			redisTemplate.boundHashOps("seckillOrder").delete(userId);
			TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(seckillOrder.getSeckillId());
			if (seckillGoods != null){
				//恢复库存
				seckillGoods.setStockCount(seckillGoods.getStockCount() + 1);
				redisTemplate.boundHashOps("seckillGoods").put(seckillOrder.getSeckillId(), seckillGoods);
			}
		}
	}

}
