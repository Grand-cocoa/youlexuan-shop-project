 //控制层 
app.controller('typeTemplateController' ,function($scope,$controller   ,typeTemplateService){	
	
	$controller('baseController',{$scope:$scope});//继承
	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		typeTemplateService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		typeTemplateService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(id){
		typeTemplateService.findOne(id).success(
			function(response){
				$scope.entity= response;
				$scope.entity.customAttributeItems = JSON.parse(response.customAttributeItems);
				$scope.entity.brandIds = JSON.parse(response.brandIds);
				$scope.entity.specIds = JSON.parse(response.specIds);
			}
		);				
	}
	
	//保存 
	$scope.save=function(){				
		var serviceObject;//服务层对象  				
		if($scope.entity.id!=null){//如果有ID
			serviceObject=typeTemplateService.update( $scope.entity ); //修改  
		}else{
			serviceObject=typeTemplateService.add( $scope.entity  );//增加 
		}				
		serviceObject.success(
			function(response){
				if(response.success){
					$scope.entity = {customAttributeItems:[]};
					//重新查询 
		        	$scope.reloadList();//重新加载
				}else{
					alert(response.message);
				}
			}		
		);				
	}
	
	 
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		typeTemplateService.dele( $scope.selectIds ).success(
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
		typeTemplateService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}

	$scope.entity = {customAttributeItems:[]};
	$scope.brandList = {data:[]};
	$scope.specList = {data:[]};

	$scope.findOther = function () {
		typeTemplateService.findBrand().success(function (resp) {
			$scope.brandList.data = resp;
		});
		typeTemplateService.findSpec().success(function (resp) {
			$scope.specList.data = resp;
		});
	}

	$scope.addTableRow = function () {
		$scope.entity.customAttributeItems.push({});
	}

	$scope.deleteTableRow = function (index) {
		$scope.entity.customAttributeItems.splice(index, 1);
	}

	$scope.close = function () {
		$scope.entity = {customAttributeItems:[]};
	}

	$scope.checkboxUpdate = function (id, $event) {
		if ($event.target.checked){
			$scope.selectIds.push(id);
		}else {
			var idIndex = $scope.selectIds.indexOf(id);
			$scope.selectIds.splice(idIndex, 1);
		}
	}

});	