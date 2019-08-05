 //商品类目控制层 
app.controller('itemCatController' ,function($scope,$controller,itemCatService,typeTemplateService){
	
	$controller('baseController',{$scope:$scope});//继承
	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		itemCatService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		itemCatService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(id){				
		itemCatService.findOne(id).success(
			function(response){
				$scope.entity= response;
			}
		);				
	}
	
	//保存 
	$scope.save=function(){				
		var serviceObject;//服务层对象
		if($scope.entity.id!=null){//如果有ID
			serviceObject=itemCatService.update( $scope.entity ); //修改
		}else{
			if ($scope.grade_2 == null){
				if ($scope.grade_1 == null){
					$scope.entity.parentId = 0;
				}else {
					$scope.entity.parentId = $scope.grade_1.id;
				}
			}else {
				$scope.entity.parentId = $scope.grade_2.id;
			}
			serviceObject=itemCatService.add( $scope.entity  );//增加
		}
		serviceObject.success(
			function(response){
				if(response.success){
					$scope.findParentId($scope.entity.parentId);
					$scope.entity = {};
				}else{
					alert(response.message);
				}
			}		
		);				
	}
	
	 
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		itemCatService.dele($scope.ids).success(
			function(response){
				if(response.success){
					if ($scope.grade_2 == null){
						if ($scope.grade_1 == null){
							$scope.findParentId(0);
						}else {
							$scope.findParentId($scope.grade_1.id);
						}
					}else {
						$scope.findParentId($scope.grade_2.id);
					}
					$scope.ids=[];
				}						
			}		
		);				
	}
	
	$scope.searchEntity={};//定义搜索对象 
	
	//搜索
	$scope.search=function(page,rows){			
		itemCatService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}

	$scope.findParentId = function (parentId) {
		itemCatService.findParentId(parentId).success(function (resp) {
			$scope.list = resp;
		});
	}

	$scope.grade = 1;

	$scope.setGrade = function (value) {
		$scope.grade = value;
	}

	$scope.grade_1 = null;

	$scope.grade_2 = null;

	$scope.setGradeList = function (entity) {
		if ($scope.grade == 1){
			$scope.grade_1 = null;
			$scope.grade_2 = null;
		}
		if ($scope.grade == 2){
			$scope.grade_1 = entity;
			$scope.grade_2 = null;
		}
		if ($scope.grade == 3){
			$scope.grade_2 = entity;
		}
		$scope.findParentId(entity.id);
	}

	//查询所有模板
	$scope.findAllTypeTemplate = function () {
		typeTemplateService.findAll().success(function (resp) {
			$scope.typeTemplateList = resp;
		})
	}

	$scope.ids = [];

	$scope.checkboxUpdate = function (id, $event) {
		if ($event.target.checked){
			$scope.ids.push(id);
		}else {
			var idIndex = $scope.ids.indexOf(id);
			$scope.ids.splice(idIndex, 1);
		}
	}

});	