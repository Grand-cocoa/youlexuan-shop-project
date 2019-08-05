app.controller("itemController", function($scope, $http){

	$scope.num = 1;

	//添加减少商品数量
	$scope.addNum = function(x){
		$scope.num = $scope.num + x;
		if ($scope.num < 1){
			$scope.num = 1;
		}
	};

	//定义对象存储选中规格
	$scope.specItem = {};

	//用户选择规格选项
	$scope.selectSpec = function(name, value){
		$scope.specItem[name] = value;
		searchSku();
	};

	//判断指定规格选项是否被选中
	$scope.isSelected = function(name, value){
		if ($scope.specItem[name] == value){
			return true;
		}else{
			return false;
		}
	};

	//加载默认SKU
	$scope.loadSku = function(){
		$scope.sku = skuList[0];
		$scope.specItem = JSON.parse(JSON.stringify($scope.sku.spec));
	};

	//匹配两个对象
	matchObject = function(map1, map2){
		//遍历对象1与对象2比对
		for(var k in map1){
			if (map1[k] != map2[k]){
				return false;
			}
		}
		//遍历对象2与对象1比对
		for(var k in map2){
			if (map2[k] != map1[k]){
				return false;
			}
		}
		return true;
	};

	//查询SKU列表
	searchSku = function(){
		//循环全部SKU集合
		for (var i = 0; i < skuList.length; i++){
			if (matchObject (skuList[i].spec, $scope.specItem)){
				$scope.sku = skuList[i];
				return;
			}
		}
		$scope.sku = {id : 0, title : "-----", price : 0};
	};

	//添加商品到购物车
	$scope.addToCart = function(){
		alert("添加商品：" + $scope.sku.id + "到购物车成功");
	}

	$scope.addToCart = function () {
		$http.get("http://localhost:9108/cart/addGoodsToCartList.do?itemId="
			+ $scope.sku.id +"&num=" + $scope.num, {"withCredentials":true}).success(function (resp) {
			if (resp.success){
				window.location.href = "http://localhost:9108/cart.html";
			}
		})
	}

});