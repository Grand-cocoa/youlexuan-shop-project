app.controller("brand", function ($scope, $http, brandService, $controller) {

	$controller("baseController", {$scope:$scope});

	$scope.find = function (){
		$scope.findPage($scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);
	};


	$scope.search = function (thisPage, pageSize){
		brandService.findPage(thisPage, pageSize, $scope.entity).success(function (resp) {
			$scope.list = resp.rows;
			$scope.paginationConf.totalItems = resp.total;
		});
	};


	$scope.entity = {};

	$scope.updateInfo = function () {
		var info = "add";
		if ($scope.entity.id != null && $scope.entity != "") {
			info = "update";
		}
		brandService.updateInfo(info, $scope.entity).success(function (resp) {
			if (resp.flag) {
				$scope.find();
				$scope.entity = {};
			} else {
				alert(resp.info);
			}
		});
	};

	$scope.findOne = function (id) {
		brandService.findOne(id).success(function (resp) {
			$scope.entity = resp;
		});
	};

	$scope.close = function () {
		$scope.entity = {};
	};

	$scope.ids = [];

	$scope.checkboxUpdate = function (id, $event) {
		if ($event.target.checked){
			$scope.ids.push(id);
		}else {
			var idIndex = $scope.ids.indexOf(id);
			$scope.ids.splice(idIndex, 1);
		}
	};

	$scope.delete = function () {
		brandService.delete($scope.ids).success(function (resp) {
			if (resp.flag){
				$scope.find();
				$scope.ids = [];
			}else {
				alert(resp.info);
			}
		});
	}
});