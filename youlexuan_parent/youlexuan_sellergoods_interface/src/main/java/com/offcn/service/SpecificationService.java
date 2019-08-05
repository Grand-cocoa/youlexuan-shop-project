package com.offcn.service;

import com.offcn.entity.PackagingSpecification;
import com.offcn.entity.PageUtil;
import com.offcn.pojo.TbSpecification;

public interface SpecificationService {

	public PageUtil<TbSpecification> findPage(Integer page, Integer rows, TbSpecification specification);

	public void add(PackagingSpecification packagingSpecification);

	public PackagingSpecification findOne (Long id);

	public void update(PackagingSpecification packagingSpecification);

	public void dele(Long[] ids);
}
