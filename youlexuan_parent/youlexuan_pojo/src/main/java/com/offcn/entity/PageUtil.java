package com.offcn.entity;

import java.io.Serializable;
import java.util.List;

public class PageUtil<T> implements Serializable {

	private Integer total;

	private Integer pageSize;

	private List<T> rows;

	public Integer getTotal() {
		return total;
	}

	public void setTotal(Integer thisPage) {
		this.total = thisPage;
	}

	public Integer getPageSize() {
		return pageSize;
	}

	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}

	public List<T> getRows() {
		return rows;
	}

	public void setRows(List<T> list) {
		this.rows = list;
	}

	public PageUtil() {
	}

	public PageUtil(Integer thisPage, Integer pageSize, List<T> list) {
		this.total = thisPage;
		this.pageSize = pageSize;
		this.rows = list;
	}
}
