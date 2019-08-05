app.controller("seckillGoodsController", function ($scope, $location, $interval, seckillGoodsService) {
	$scope.findList = function () {
		seckillGoodsService.findList().success(function (resp) {
			$scope.list = resp;
		})
	};
	$scope.findOne = function () {
		var id = $location.search()["id"];
		seckillGoodsService.findOne(id).success(function (resp) {
			$scope.goods = resp;
			var endTime = new Date($scope.goods.endTime).getTime();
			var nowTime = new Date().getTime();
			$scope.allSecond = Math.floor((endTime - nowTime) / 1000);
			var time = $interval(function () {
				if ($scope.allSecond > 0){
					$scope.allSecond --;
					converTimeString($scope.allSecond);
				}else {
					$interval.cancel(time);
				}
			}, 1000);
		})
	};
	$scope.toItem = function (id) {
		window.location.href = "seckill-item.html#?id=" + id;
	};
	/*$scope.second = 10;
	var time = $interval(function () {
		if ($scope.second > 0){
			$scope.second--;
		}else {
			alert("时间到");
			$interval.cancel(time);
		}
	}, 1000);*/
	function converTimeString(allSecond) {
		var days = Math.floor(allSecond / (24 * 60 * 60));
		var hours = Math.floor((allSecond - days * 24 * 60 * 60) / (60 * 60));
		var minutes = Math.floor((allSecond - days * 24 * 60 * 60 - hours * 60 * 60) / 60);
		var seconds = Math.floor(allSecond - days * 24 * 60 * 60 - hours * 60 * 60 - minutes * 60);
		$scope.timeStr = "";
		if (days > 0){
			$scope.timeStr += days + "天";
		}
		$scope.timeStr += hours + "时" + minutes + "分" + seconds + "秒";
	};
	$scope.submitOrder = function () {
		seckillGoodsService.submitOrder($scope.goods.id).success(function (resp) {
			if (resp.success){
				alert("秒杀商品下单成功， 请在五分钟内完成付款");
				location.href = "pay.html";
			}else {
				alert(resp.message);
				if (resp.message == "用户未登录"){

					location.href = "Demo.html";
				}else {
					alert(resp.message);
				}
			}
		})
	}
});