app.controller("searchController", function ($scope, $location, searchService) {

	$scope.searchMap = {};

	$scope.resultMap = {"totalPages" : 1};

	$scope.searchKey = "";

	$scope.loadKeywords = function(){
		$scope.searchMap.keywords = $location.search()["keywords"];
		$scope.search()
	};
	//搜索
	$scope.search = function () {
		searchService.search($scope.searchMap).success(function (resp) {
			$scope.searchKey = $scope.searchMap.keywords;
			$scope.searchMap.pageNo= parseInt($scope.searchMap.pageNo)
			$scope.resultMap = resp;
			//调用页码标签构建
			buildPageLabel();
		})
	};
	//定义搜索条件集合
	$scope.searchMap = {
		"keywords" : "",
		"category" : "",
		"brand" : "",
		"spec" : {},
		"price" : "",
		"pageNo" : 1,
		"pageSize" : 20,
		"sort" : "",
		"sortField" : ""
	};
	//添加搜索条件
	$scope.addSearchItem = function (key, value) {
		//判断搜索条件类型
		if (key == "category"){
			$scope.searchMap = {
				"keywords" : $scope.searchMap.keywords,
				"category" : value,
				"brand" : "",
				"spec" : {},
				"price" : "",
				"pageNo" : 1,
				"pageSize" : 20,
				"sort" : "",
				"sortField" : ""
			};
		}else if (key == "brand" || key == "price"){
			$scope.searchMap[key] = value;
		}else {
			$scope.searchMap.spec[key] = value;
		}
		$scope.searchMap.pageNo=1
		$scope.search();
	};
	//取消搜索条件
	$scope.removeSearchItem = function (key) {
		//判断搜索条件类型
		if (key == "category"){
			$scope.searchMap = {
				"keywords" : $scope.searchMap.keywords,
				"category" : "",
				"brand" : "",
				"spec" : {},
				"price" : "",
				"pageNo" : 1,
				"pageSize" : 20,
				"sort" : "",
				"sortField" : ""
			};
		}else if (key == "brand" || key == "price"){
			$scope.searchMap[key] = "";
		}else {
			delete $scope.searchMap.spec[key];
		}
		$scope.searchMap.pageNo=1
		$scope.search();
	};
	//构建分页标签
	buildPageLabel = function () {
		//分页属性对象
		$scope.pageLabel = [];
		//创建最后一页
		var maxPageNo = $scope.resultMap.totalPages;
		//创建开始页码
		var fisrtPage = 1;
		//创建截止页码
		var lastPage = maxPageNo;
		//判断总页码是否大于五页，显示部分页码
		if (maxPageNo > 5){
			//判断当前页
			if ($scope.searchMap.pageNo <= 3){
				lastPage = 5
				$scope.firstDot = false;
				$scope.lastDot = true;
			}else if ($scope.resultMap.totalPages <= lastPage - 2){
				fisrtPage = maxPageNo - 4;
				$scope.firstDot = true;
				$scope.lastDot = true;
			}else {
				fisrtPage = $scope.searchMap.pageNo - 2;
				lastPage = $scope.searchMap.pageNo + 2;
				$scope.firstDot = true;
				$scope.lastDot = false;
			}
		}else {
			$scope.firstDot = false;
			$scope.lastDot = false;
		}
		//循环产生页码
		for(var i = fisrtPage; i <= lastPage; i ++){
			$scope.pageLabel.push(i);
		}
	};
	//分页跳转
	$scope.queryPage = function(pageNo){
		if (pageNo < 1 || pageNo > $scope.resultMap.totalPages){
			return;
		}
		$scope.searchMap.pageNo = pageNo;
		$scope.search();
	};
	$scope.searchButton = function () {
		$scope.searchMap = {
			"keywords" : $scope.searchMap.keywords,
			"category" : "",
			"brand" : "",
			"spec" : {},
			"price" : "",
			"pageNo" : 1,
			"pageSize" : 20,
			"sort" : "",
			"sortField" : ""
		};
	};
	$scope.sortSearch = function (sortField, sort) {
		$scope.searchMap.sortField = sortField;
		$scope.searchMap.sort = sort;
		$scope.search();
	};
	$scope.keywordsIsBrand = function(){
		for (var i = 0; i < $scope.resultMap.brandList.length; i++){
			if ($scope.searchKey.indexOf($scope.resultMap.brandList[i].text) >= 0){
				return false;
			}
		}
		return true;

	}
});