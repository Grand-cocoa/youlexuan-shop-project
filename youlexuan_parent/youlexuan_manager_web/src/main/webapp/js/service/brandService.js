app.service("brandService", function ($http) {
	this.findPage = function (thisPage, pageSize, entity) {
		return $http.post("/brand/findPage.do?thisPage=" + thisPage + "&pageSize=" + pageSize, entity);
	};

	this.updateInfo = function (info, entity) {
		return $http.post("/brand/" + info + ".do", entity);
	};

	this.delete = function (ids) {
		return $http.get("/brand/deleteByIds.do?ids=" + ids);
	};

	this.findOne = function (id) {
		return $http.get("/brand/findOne.do?id=" + id);
	};
});