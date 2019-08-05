package com.offcn.service;

import com.offcn.entity.PageUtil;
import com.offcn.pojo.TbBrand;

import java.util.List;

public interface BrandService {

	public List<TbBrand> findAll();

	public void addBrand(TbBrand brand);

	public TbBrand findOne(Long id);

	public void updateBrand(TbBrand brand);

	public void deleteByIds(Long[] ids);

	public PageUtil<TbBrand> findPage(Integer thisPage, Integer pageSize, TbBrand brand);
}
