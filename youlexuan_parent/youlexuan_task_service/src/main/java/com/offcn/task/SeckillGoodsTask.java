package com.offcn.task;

import com.offcn.mapper.TbSeckillGoodsMapper;
import com.offcn.pojo.TbSeckillGoods;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class SeckillGoodsTask {

	@Autowired
	private RedisTemplate redisTemplate;

	@Autowired
	private TbSeckillGoodsMapper seckillGoodsMapper;

	@Scheduled(cron = "0 1 * * * ?")
	public void refreshSeckillGoods(){
		List<TbSeckillGoods> seckillGoods = seckillGoodsMapper.selectByExample(null);
		for (TbSeckillGoods goods :
				seckillGoods) {
			TbSeckillGoods goods1 = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(goods.getId());
			if (goods1 == null)
				redisTemplate.boundHashOps("seckillGoods").put(goods.getId(), goods);
		}
	}

	@Scheduled(cron = "0 1 * * * ?")
	public void removeSeckillGoods(){
		List<TbSeckillGoods> seckillGoods = redisTemplate.boundHashOps("seckillGoods").values();
		for (TbSeckillGoods goods :
				seckillGoods) {
			if (goods.getEndTime().getTime() < new Date().getTime()) {
				seckillGoodsMapper.updateByPrimaryKey(goods);
				redisTemplate.boundHashOps("seckillGoods").delete(goods.getId());
			}
		}
	}

}

