package com.offcn.sellergoods.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.offcn.entity.PageUtil;
import com.offcn.entity.ResultInfo;
import com.offcn.pojo.TbBrand;
import com.offcn.service.BrandService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/brand")
public class BrandController {

	@Reference
	private BrandService brandService;

	@RequestMapping("/findAll")
	public List<TbBrand> findAll(){
		return brandService.findAll();
	}

	@RequestMapping("/add")
	public ResultInfo add(@RequestBody TbBrand brand){
		try {
			brandService.addBrand(brand);
			return new ResultInfo(true, "添加成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new ResultInfo(false, "添加失败");
		}
	}

	@RequestMapping("/findOne")
	public TbBrand findOne(Long id){
		return brandService.findOne(id);
	}

	@RequestMapping("/update")
	public ResultInfo update(@RequestBody TbBrand brand){
		try {
			brandService.updateBrand(brand);
			return new ResultInfo(true, "修改成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new ResultInfo(false, "修改失败");
		}
	}

	@RequestMapping("/deleteByIds")
	public ResultInfo deleteByIds(Long[] ids){
		try {
			brandService.deleteByIds(ids);
			return new ResultInfo(true ,"删除成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new ResultInfo(false, "删除失败");
		}
	}

	@RequestMapping("/findPage")
	public PageUtil<TbBrand> findPage(Integer thisPage, Integer pageSize, @RequestBody TbBrand brand){
		System.out.println(brand.getName());
		return brandService.findPage(thisPage, pageSize, brand);
	}

}
