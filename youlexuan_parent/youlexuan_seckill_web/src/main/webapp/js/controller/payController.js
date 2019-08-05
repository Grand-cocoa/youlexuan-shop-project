app.controller("payController", function ($scope, payService, $location) {
	$scope.createNative = function () {
		payService.createNative().success(function (resp) {
			$scope.money = (resp.total_fee / 100).toFixed(2);
			$scope.out_trade_no = resp.out_trade_no;
			var qr = new QRious({
				element : document.getElementById('qrious'),
				size : 250,
				level : 'H',
				value : resp.qrcode
			});
		queryPayStatus($scope.out_trade_no);
		});
	};

	function queryPayStatus(out_trade_no) {
		payService.queryPayStatus(out_trade_no).success(function (resp) {
			if (resp.success){
				location.href = "paysuccess.html#?money=" + $scope.money;
			}else {
				if (resp.message == "二维码超时"){
					document.getElementById("timeout").innerHTML="二维码已过期，请刷新页面重新获取二维码。"
				}else {
					location.href = "payfail.html";
				}

			}
		})
	};

	$scope.getMoney = function () {
		return $location.search()["money"];
	};
});