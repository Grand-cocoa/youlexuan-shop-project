package com.offcn.sellergoods.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.offcn.entity.PackagingGoods;
import com.offcn.entity.PageResult;
import com.offcn.entity.Result;
import com.offcn.pojo.TbGoods;
import com.offcn.pojo.TbItem;
import com.offcn.sellergoods.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import java.util.List;

/**
 * controller
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {

	@Reference
	private GoodsService goodsService;

	@Autowired
	private JmsTemplate jmsTemplate;

	@Resource
	private Destination queueSolrDestination;

	@Resource
	private Destination queueSolrDeleteDestination;

	@Resource
	private Destination topicPageDestination;

	@Resource
	private Destination topicPageDeleteDestination;

	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findAll")
	public List<TbGoods> findAll(){
		return goodsService.findAll();
	}


	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findPage")
	public PageResult  findPage(int page,int rows){
		return goodsService.findPage(page, rows);
	}

	/**
	 * 增加
	 * @param goods
	 * @return
	 */
	@RequestMapping("/add")
	public Result add(@RequestBody PackagingGoods goods){
		try {
			goods.getGoods().setSellerId(SecurityContextHolder.getContext().getAuthentication().getName());
			goodsService.add(goods);
			return new Result(true, "增加成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "增加失败");
		}
	}

	/**
	 * 修改
	 * @param goods {@link PackagingGoods}
	 * @return Result
	 */
	@RequestMapping("/update")
	public Result update(@RequestBody PackagingGoods goods){
		String name = SecurityContextHolder.getContext().getAuthentication().getName();
		PackagingGoods packagingGoods = goodsService.findOne(goods.getGoods().getId());
		if (name.equals(packagingGoods.getGoods().getSellerId()) && name.equals(goods.getGoods().getSellerId())){
			try {
				goodsService.update(goods);
				return new Result(true, "修改成功");
			} catch (Exception e) {
				e.printStackTrace();
				return new Result(false, "修改失败");
			}
		}
		return new Result(false, "禁止修改");
	}

	/**
	 * 获取实体
	 * @param id
	 * @return TbGoods
	 */
	@RequestMapping("/findOne")
	public PackagingGoods findOne(Long id){
		PackagingGoods goods = goodsService.findOne(id);
		System.err.println(goods.getItemList().toString());
		return goods;
	}

	/**
	 * 批量删除
	 * @param ids
	 * @return
	 */
	@RequestMapping("/delete")
	public Result delete(final Long [] ids){
		try {
			goodsService.delete(ids);
			//itemSearchService.deleteByGoodsIds(Arrays.asList(ids));
			jmsTemplate.send(queueSolrDeleteDestination, new MessageCreator() {
				public Message createMessage(Session session) throws JMSException {
					return session.createObjectMessage(ids);
				}
			});
			jmsTemplate.send(topicPageDeleteDestination, new MessageCreator() {
				public Message createMessage(Session session) throws JMSException {
					return session.createObjectMessage(ids);
				}
			});
			return new Result(true, "删除成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "删除失败");
		}
	}

	/**
	 * 查询+分页
	 * @param
	 * @param page
	 * @param rows
	 * @return
	 */
	@RequestMapping("/search")
	public PageResult search(@RequestBody TbGoods goods, int page, int rows  ){
		return goodsService.findPage(goods, page, rows);
	}

	@RequestMapping("/updateStatus")
	public Result updateStatus(Long[] ids, String status){
		try {
			goodsService.updateStatus(ids, status);
			if ("1".equals(status)){
				List<TbItem> list = goodsService.findItemListByGoodsIdAndStatus(ids, status);
				if (list.size() > 0){
					//itemSearchService.importSolrFromItemList(list);
					//把集合转换为JSON串
					final String jsonString = JSON.toJSONString(list);
					jmsTemplate.send(queueSolrDestination, new MessageCreator() {
						public Message createMessage(Session session) throws JMSException {
							return session.createTextMessage(jsonString);
						}
					});
				}
				for (final Long id :
						ids) {
					//itemPageService.genItemHtml(id);
					jmsTemplate.send(topicPageDestination, new MessageCreator() {
						public Message createMessage(Session session) throws JMSException {
							return session.createTextMessage(id + "");
						}
					});
				}
			}
			return new Result(true, "成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "失败");
		}
	}

	@RequestMapping("/genHtml")
	public void genHtml(Long goodsId){
		//itemPageService.genItemHtml(goodsId);
	}

}
