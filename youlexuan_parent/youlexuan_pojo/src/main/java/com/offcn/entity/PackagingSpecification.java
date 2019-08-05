package com.offcn.entity;

import com.offcn.pojo.TbSpecification;
import com.offcn.pojo.TbSpecificationOption;

import java.io.Serializable;
import java.util.List;

public class PackagingSpecification implements Serializable {

	private TbSpecification specification;

	private List<TbSpecificationOption> specificationOption;

	public TbSpecification getSpecification() {
		return specification;
	}

	public void setSpecification(TbSpecification specification) {
		this.specification = specification;
	}

	public List<TbSpecificationOption> getSpecificationOption() {
		return specificationOption;
	}

	public void setSpecificationOption(List<TbSpecificationOption> specificationOption) {
		this.specificationOption = specificationOption;
	}

	public PackagingSpecification() {
	}

	public PackagingSpecification(TbSpecification specification, List<TbSpecificationOption> specificationOption) {
		this.specification = specification;
		this.specificationOption = specificationOption;
	}
}
