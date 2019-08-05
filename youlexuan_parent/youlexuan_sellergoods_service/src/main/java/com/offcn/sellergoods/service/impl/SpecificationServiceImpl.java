package com.offcn.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.offcn.entity.PackagingSpecification;
import com.offcn.entity.PageUtil;
import com.offcn.mapper.TbSpecificationMapper;
import com.offcn.mapper.TbSpecificationOptionMapper;
import com.offcn.pojo.TbSpecification;
import com.offcn.pojo.TbSpecificationExample;
import com.offcn.pojo.TbSpecificationOption;
import com.offcn.pojo.TbSpecificationOptionExample;
import com.offcn.service.SpecificationService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@Service
public class SpecificationServiceImpl implements SpecificationService {

	@Autowired
	private TbSpecificationMapper specificationMapper;

	@Autowired
	private TbSpecificationOptionMapper specificationOptionMapper;

	public PageUtil<TbSpecification> findPage(Integer page, Integer rows, TbSpecification specification) {
		PageHelper.startPage(page, rows);
		TbSpecificationExample example = null;
		if (specification.getSpecName() != null && !"".equals(specification.getSpecName())) {
			example = new TbSpecificationExample();
			TbSpecificationExample.Criteria criteria = example.createCriteria();
			criteria.andSpecNameLike("%" + specification.getSpecName() + "%");
		}
		Page<TbSpecification> tbSpecifications = (Page<TbSpecification>)specificationMapper.selectByExample(example);
		return new PageUtil<TbSpecification>((int)tbSpecifications.getTotal(), tbSpecifications.getPageSize(), tbSpecifications.getResult());
	}

	public void add(PackagingSpecification packagingSpecification) {
		TbSpecification specification = packagingSpecification.getSpecification();
		specificationMapper.insert(specification);
		for (TbSpecificationOption option : packagingSpecification.getSpecificationOption()){
			option.setSpecId(specification.getId());
			specificationOptionMapper.insert(option);
		}
	}

	public PackagingSpecification findOne(Long id) {
		TbSpecification tbSpecification = specificationMapper.selectByPrimaryKey(id);
		TbSpecificationOptionExample example = new TbSpecificationOptionExample();
		TbSpecificationOptionExample.Criteria criteria = example.createCriteria();
		criteria.andSpecIdEqualTo(id);
		List<TbSpecificationOption> tbSpecificationOptions = specificationOptionMapper.selectByExample(example);
		return new PackagingSpecification(tbSpecification, tbSpecificationOptions);
	}

	public void update(PackagingSpecification packagingSpecification) {
		TbSpecificationOptionExample example = new TbSpecificationOptionExample();
		TbSpecificationOptionExample.Criteria criteria = example.createCriteria();
		criteria.andSpecIdEqualTo(packagingSpecification.getSpecification().getId());
		specificationMapper.updateByPrimaryKey(packagingSpecification.getSpecification());
		specificationOptionMapper.deleteByExample(example);
		for (TbSpecificationOption option : packagingSpecification.getSpecificationOption()){
			option.setSpecId(packagingSpecification.getSpecification().getId());
			specificationOptionMapper.insert(option);
		}
	}

	public void dele(Long[] ids) {
		List<Long> idsList = new ArrayList<Long>();
		for (Long l : ids){
			idsList.add(l);
		}
		{
			TbSpecificationExample example = new TbSpecificationExample();
			TbSpecificationExample.Criteria criteria = example.createCriteria();
			criteria.andIdIn(idsList);
			specificationMapper.deleteByExample(example);
		}
		{
			TbSpecificationOptionExample example = new TbSpecificationOptionExample();
			TbSpecificationOptionExample.Criteria criteria = example.createCriteria();
			criteria.andSpecIdIn(idsList);
			specificationOptionMapper.deleteByExample(example);
		}
	}
}
