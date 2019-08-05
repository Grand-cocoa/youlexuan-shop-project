package com.offcn.content.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.offcn.content.service.ContentCategoryService;
import com.offcn.entity.PageResult;
import com.offcn.mapper.TbContentCategoryMapper;
import com.offcn.pojo.TbContentCategory;
import com.offcn.pojo.TbContentCategoryExample;
import com.offcn.pojo.TbContentCategoryExample.Criteria;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * 内容分类服务实现层
 * @author Administrator
 *
 */
@Service
public class ContentCategoryServiceImpl implements ContentCategoryService {

	@Autowired
	private TbContentCategoryMapper contentCategoryMapper;
	
	/**
	 * 查询全部
	 */
	public List<TbContentCategory> findAll() {
		return contentCategoryMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbContentCategory> page=   (Page<TbContentCategory>) contentCategoryMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	public void add(TbContentCategory contentCategory) {
		contentCategoryMapper.insert(contentCategory);		
	}

	
	/**
	 * 修改
	 */
	public void update(TbContentCategory contentCategory){
		contentCategoryMapper.updateByPrimaryKey(contentCategory);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	public TbContentCategory findOne(Long id){
		return contentCategoryMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	public void delete(Long[] ids) {
		for(Long id:ids){
			contentCategoryMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
	public PageResult findPage(TbContentCategory contentCategory, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbContentCategoryExample example=new TbContentCategoryExample();
		Criteria criteria = example.createCriteria();
		
		if(contentCategory!=null){			
						if(contentCategory.getName()!=null && contentCategory.getName().length()>0){
				criteria.andNameLike("%"+contentCategory.getName()+"%");
			}	
		}
		
		Page<TbContentCategory> page= (Page<TbContentCategory>)contentCategoryMapper.selectByExample(example);
		return new PageResult(page.getTotal(), page.getResult());
	}
	
}
