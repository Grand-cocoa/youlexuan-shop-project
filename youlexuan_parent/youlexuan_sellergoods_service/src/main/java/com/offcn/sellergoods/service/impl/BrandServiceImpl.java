package com.offcn.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.offcn.entity.PageUtil;
import com.offcn.mapper.TbBrandMapper;
import com.offcn.pojo.TbBrand;
import com.offcn.pojo.TbBrandExample;
import com.offcn.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@Service
public class BrandServiceImpl implements BrandService {

	@Autowired
	public TbBrandMapper brandMapper;

	public List<TbBrand> findAll() {
		List<TbBrand> tbBrands = brandMapper.selectByExample(null);
		return tbBrands;
	}

	public void addBrand(TbBrand brand) {
		brandMapper.insertSelective(brand);
	}

	public TbBrand findOne(Long id) {
		return brandMapper.selectByPrimaryKey(id);
	}

	public void updateBrand(TbBrand brand) {
		brandMapper.updateByPrimaryKeySelective(brand);
	}

	public void deleteByIds(Long[] ids) {
		List<Long> longs = new ArrayList<Long>();
		for (Long id : ids){
			longs.add(id);
		}
		TbBrandExample tbBrandExample = new TbBrandExample();
		TbBrandExample.Criteria criteria = tbBrandExample.createCriteria();
		criteria.andIdIn(longs);
		brandMapper.deleteByExample(tbBrandExample);
	}

	public PageUtil<TbBrand> findPage(Integer thisPage, Integer pageSize, TbBrand brand) {
		PageHelper.startPage(thisPage, pageSize);
		TbBrandExample brandExample = new TbBrandExample();
		if (brand != null){
			TbBrandExample.Criteria criteria = brandExample.createCriteria();
			if (brand.getName() != null && !"".equals(brand.getName())){
				criteria.andNameLike("%" + brand.getName() + "%");
			}
			if (brand.getFirstChar() != null && !"".equals(brand.getFirstChar())){
				criteria.andFirstCharEqualTo(brand.getFirstChar());
			}
		}
		Page<TbBrand> pageBrand = (Page<TbBrand>) brandMapper.selectByExample(brandExample);
		return new PageUtil<TbBrand>((int)pageBrand.getTotal(), pageBrand.getPageSize(), pageBrand.getResult());
	}

}
