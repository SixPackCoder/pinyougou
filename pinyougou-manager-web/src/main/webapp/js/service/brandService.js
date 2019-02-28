//品牌服务
app.service('brandService', function ($http) {
    //查询所有品牌
    this.findAll = function () {
        return $http.get('../brand/findAll.do');
    }

    this.findPage = function (page, size) {
        return $http.get('../brand/findPage.do?page=' + page + '&size=' + size);
    }

    this.add = function (entity) {
        return $http.post('../brand/add.do', entity);
    }

    this.update = function (entity) {
        return $http.post('../brand/update.do', entity);
    }
    this.findById = function (id) {
        return $http.get('../brand/findById.do?id=' + id);
    }
    this.deleteByIds = function (ids) {
        return $http.get('../brand/delete.do?ids=' + ids);
    }
    this.search = function (page, size, searchEntity) {
        return $http.post('../brand/search.do?page=' + page + '&size=' + size, searchEntity);
    }
    //下拉列表品牌数据
    this.selectOptionList = function () {
        return $http.get('../brand/selectOptionList.do');
    }

});