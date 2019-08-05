package com.offcn.sellergoods.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.offcn.entity.PackagingSpecification;
import com.offcn.entity.PageUtil;
import com.offcn.entity.Result;
import com.offcn.pojo.TbSpecification;
import com.offcn.service.SpecificationService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/specification")
public class SpecificationController {

	@Reference
	private SpecificationService specificationService;

	@RequestMapping("/search")
	public PageUtil<TbSpecification> search(Integer page, Integer rows, @RequestBody TbSpecification specification){
		return specificationService.findPage(page, rows, specification);
	}

	@RequestMapping("/add")
	public Result add(@RequestBody PackagingSpecification packagingSpecification){
		try {
			specificationService.add(packagingSpecification);
			return new Result(true, "添加成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "添加失败");
		}
	}

	@RequestMapping("/findOne")
	public PackagingSpecification findOne (Long id){
		return specificationService.findOne(id);
	}

	@RequestMapping("/update")
	public Result update(@RequestBody PackagingSpecification packagingSpecification){
		try {
			specificationService.update(packagingSpecification);
			return new Result(true, "修改成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "修改失败");
		}
	}

	@RequestMapping("/delete")
	public Result delete(Long[] ids){
		try {
			specificationService.dele(ids);
			return new Result(true, "删除成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "删除失败");
		}
	}

}
