app.controller("cartController", function ($scope, cartService) {

	$scope.findCartList = function () {
		cartService.findCartList().success(function (resp) {
			$scope.cartList = resp;
			$scope.totalValue = cartService.sum($scope.cartList);
		})
	};

	$scope.addGoodsToCart = function (itemId, num) {
		cartService.addGoodsToCart(itemId, num).success(function (resp) {
			if (resp.success){
				$scope.findCartList();
			}else {
				alert(resp.message);
			}
		})
	};

	$scope.findListByUserId = function () {
		cartService.findListByUserId().success(function (resp) {
			$scope.addressList = resp;
			for (var i = 0; i < resp.length; i++) {
				if (resp[i].isDefault == 1){
					$scope.address = resp[i];
					break;
				}
			}
		})
	};

	//选择地址
	$scope.selectAddress = function (address) {
		$scope.address = address;
	};

	//判断当前地址是否被选中
	$scope.isSelectAddress = function (address) {
		if ($scope.address == address){
			return true;
		}else {
			return false;
		}
	};

	//支付方式选择
	$scope.order = {paymentType:'1'};

	$scope.selectPayType = function (type) {
		$scope.order.paymentType = type;
	};

	//提交订单
	$scope.submitOrder = function () {
		//接收地址
		$scope.order.receiverAreaName = $scope.address.address;
		//接收手机号
		$scope.order.receiverMobile = $scope.address.mobile;
		//联系人
		$scope.order.receiver = $scope.address.contact;

		cartService.submitOrder($scope.order).success(function (resp) {
			if (resp.success){
				//订单提交成功跳转支付页面
				if ($scope.order.paymentType == "1"){
					location.href = "pay.html";
				}else {
					location.href = "paysuccess.html";
				}
			}else {
				alert(resp.message);
			}
		})
	}

});