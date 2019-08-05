app.controller("indexController", function ($scope, loginService) {
	$scope.loginName = function () {
		loginService.loginName().success(function (resp) {
			$scope.loginName = resp.loginName;
		});
	}
})