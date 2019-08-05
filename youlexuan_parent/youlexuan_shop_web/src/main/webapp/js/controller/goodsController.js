 //控制层
app.controller('goodsController' ,function($scope, $controller, $location, goodsService, uploadService, itemCatService, typeTemplateService){

	$controller('baseController',{$scope:$scope});//继承

	$scope.entity = {goodsDesc:{itemImages:[], specificationItems:[]}};

    //读取列表数据绑定到表单中
	$scope.findAll=function(){
		goodsService.findAll().success(
			function(response){
				$scope.list=response;
			}
		);
	}

	//分页
	$scope.findPage=function(page,rows){
		goodsService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}
		);
	}

	$scope.typeTemplate = {customAttributeItems:[]};

	//查询实体
	$scope.findOne=function(){
		//goods_edit.html#?id=**
		var id = $location.search()["id"];
		if (id == null){
			return;
		}
		goodsService.findOne(id).success(
			function(response){
				$scope.entity= response;
				//向富文本写入
				editor.html($scope.entity.goodsDesc.introduction);
				$scope.entity.goodsDesc.itemImages = JSON.parse($scope.entity.goodsDesc.itemImages);
				$scope.entity.goodsDesc.customAttributeItems = JSON.parse($scope.entity.goodsDesc.customAttributeItems);
				$scope.entity.goodsDesc.specificationItems = JSON.parse($scope.entity.goodsDesc.specificationItems);
				for (var i = 0; i < $scope.entity.itemList.length; i++){
					$scope.entity.itemList[i].spec = JSON.parse($scope.entity.itemList[i].spec);
					$scope.typeTemplate.customAttributeItems[i] = $scope.entity.itemList[i].spec;
				}

			}
		);
	}

	//保存
	$scope.save=function(){
		var serviceObject;//服务层对象
		$scope.entity.goodsDesc.introduction = editor.html();
		if($scope.entity.goods.id!=null){//如果有ID
			serviceObject=goodsService.update( $scope.entity ); //修改
		}else{
			serviceObject=goodsService.add( $scope.entity  );//增加
		}
		serviceObject.success(
			function(response){
				if(response.success){
					/*alert("保存成功");
					$scope.entity={ goodsDesc:{itemImages:[],specificationItems:[]} };
					editor.html('');*/
					location.href = "/admin/goods.html";
				}else{
					alert(response.message);
				}
			}
		);
	}


	//批量删除
	$scope.dele=function(){
		//获取选中的复选框
		goodsService.dele( $scope.selectIds ).success(
			function(response){
				if(response.success){
					$scope.reloadList();//刷新列表
					$scope.selectIds=[];
				}
			}
		);
	}

	$scope.searchEntity={};//定义搜索对象

	//搜索
	$scope.search=function(page,rows){
		goodsService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}
		);
	}

	$scope.uploadFile = function () {
		uploadService.uploadFile().success(function (resp) {
			if (resp.success){
				$scope.image_entity.url = resp.message;
			}else {
				alert(resp.message);
			}
		}).error(function () {
			alert("上传错误");
		});
	}

	$scope.entity = {goods:{}, goodsDesc:{itemImages:[]}};

	$scope.add_image_entity=function () {
		$scope.entity.goodsDesc.itemImages.push($scope.image_entity);
	}

	$scope.remove_image_entity = function (index) {
		$scope.entity.goodsDesc.itemImages.splice(index, 1);
	}

	$scope.selectItemCat1List = function () {
		itemCatService.findParentId(0).success(function (resp) {
			$scope.itemCat1List = resp;
		})
	}

	$scope.$watch("entity.goods.category1Id", function (newValue, oldValue) {
		if (newValue != null){
			itemCatService.findParentId(newValue).success(function (resp) {
				$scope.itemCat2List = resp;
				$scope.itemCat3List = {};
				if ($location.search()["id"] == null) {
					$scope.entity.goodsDesc.specificationItems = [];
				}
			})
		}
	})

	$scope.$watch("entity.goods.category2Id", function (newValue, oldValue) {
		if (newValue != null){
			itemCatService.findParentId(newValue).success(function (resp) {
				$scope.itemCat3List = resp;
				if ($location.search()["id"] == null) {
					$scope.entity.goodsDesc.specificationItems = [];
				}
			})
		}
	})

	$scope.$watch("entity.goods.category3Id", function (newValue, oldValue) {
		if (newValue != null){
			itemCatService.findOne(newValue).success(function (resp) {
				$scope.entity.goods.typeTemplateId = resp.typeId;
				if ($location.search()["id"] == null) {
					$scope.entity.goodsDesc.specificationItems = [];
				}
			})
		}
	})

	$scope.$watch("entity.goods.typeTemplateId", function (newValue, oldValue) {
		if (newValue != null){
			typeTemplateService.findOne(newValue).success(function (resp) {
				$scope.typeTemplate = resp;
				$scope.typeTemplate.brandIds = JSON.parse($scope.typeTemplate.brandIds);
				if ($location.search()["id"] == null){
					$scope.typeTemplate.customAttributeItems = JSON.parse($scope.typeTemplate.customAttributeItems);
				}
			})
			typeTemplateService.findSpecList(newValue).success(function (resp) {
				$scope.specList = resp;
			})
		}
	})

	$scope.updateSpecAttribute = function ($event, name, value) {
		var object = $scope.searchObjectByKey($scope.entity.goodsDesc.specificationItems, "attributeName", name);
		if (object != null){
			if ($event.target.checked){
				object.attributeValue.push(value);
			}else {
				object.attributeValue.splice(object.attributeValue.indexOf(value), 1);
				if (object.attributeValue.length == 0){
					$scope.entity.goodsDesc.specificationItems.splice($scope.entity.goodsDesc.specificationItems.indexOf(object), 1);
				}
			}
		}else {
			$scope.entity.goodsDesc.specificationItems.push({"attributeName":name, "attributeValue":[value]})
		}
	}

	$scope.createItemList = function () {
		$scope.entity.itemList = [{spec:{}, price:0, num:0, status:"0", isDefault:"0"}];
		var items = $scope.entity.goodsDesc.specificationItems;
		for (var i = 0; i < items.length; i++){
			$scope.entity.itemList = addColumn($scope.entity.itemList, items[i].attributeName, items[i].attributeValue);
		}
	}

	function addColumn(list, columnName, columnValues) {
		var newList = [];
		for (var i = 0; i < list.length; i++){
			var oldRow = list[i];
			for (var j = 0; j < columnValues.length; j++){
				var newRow = JSON.parse(JSON.stringify(oldRow));
				newRow.spec[columnName] = columnValues[j];
				newList.push(newRow);
			}
		}
		return newList;
	}

	$scope.checkAttributeValue = function (specName, optionName) {
		var items = $scope.entity.goodsDesc.specificationItems;
		var object = $scope.searchObjectByKey(items, "attributeName", specName);
		if (object == null){
			return false;
		}else {
			if (object.attributeValue.indexOf(optionName) >= 0){
				return true;
			}else {
				return false;
			}
		}
	}

	$scope.status = ["未审核","审核通过","驳回","审核关闭"];

	$scope.itemCatList = [];

	$scope.findItemCatList = function () {
		itemCatService.findAll().success(function (resp) {
			for (var i = 0; i < resp.length; i++){
				$scope.itemCatList[resp[i].id] = resp[i].name;
			}
		})
	}

	$scope.checkAttributeValue=function(specName,optionName){
		var items= $scope.entity.goodsDesc.specificationItems;
		var object= $scope.searchObjectByKey(items,'attributeName',specName);
		if(object==null){
			return false;
		}else{
			if(object.attributeValue.indexOf(optionName)>=0){
				return true;
			}else{
				return false;
			}
		}
	}

	$scope.marketable = ["", "正常", "下架"];

	$scope.updateMarketable = function (id, status) {
		goodsService.updateMarketable(id, status).success(function (resp) {
			if (resp.success){
				$scope.reloadList();
			}else {
				alert(resp.message);
			}
		})
	}

});	