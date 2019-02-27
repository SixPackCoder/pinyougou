app.controller('brandController', function ($scope, brandService, $controller) {
    $controller('baseController', {$scope: $scope});

    //读取列表数据绑定到表单中
    //查询所有
    $scope.findAll = function () {
        brandService.findAll.success(
            function (response) {
                $scope.list = response;
            }
        );
    };
    //分页查询
    $scope.findPage = function (page, size) {
        brandService.findPage(page, size).success(
            function (response) {
                //显示当前页数据
                $scope.list = response.rows;
                //更新总记录数
                $scope.paginationConf.totalItems = response.total;
            }
        );
    };
    //新建
    $scope.add = function () {
        var object = null;
        if ($scope.entity.id != null) {
            object = brandService.update($scope.entity)
        } else {
            object = brandService.add($scope.entity)
        }
        object.success(
            function (response) {
                if (response.success) {//操作成功 -->刷新界面
                    $scope.reloadList();
                } else {                 //失败则弹出错误提示
                    alert(response.message)
                }
            })
    };

    //根据id查询品牌 用于修改的数据回显
    $scope.findById = function (id) {
        brandService.findById(id).success(
            function (response) {
                $scope.entity = response;
            })
    };
    //删除
    $scope.deleteByIds = function () {
        if ($scope.selectedIds.length > 0) {
            if (confirm("确认要删除选中项吗?")) {
                brandService.deleteByIds($scope.selectedIds).success(
                    function (response) {
                        if (response.success) {
                            $scope.reloadList();
                        } else {
                            alert(response.message)
                        }
                    })
            } else {
                return;
            }
        } else {
            alert("请选择要删除的品牌");
            $scope.reloadList();
        }
    };

    //条件搜索查询  带分页功能
    $scope.searchEntity = {};//定义搜索对象
    $scope.search = function (page, size) {
        brandService.search(page, size, $scope.searchEntity).success(
            function (response) {
                if (response != null) {//不为空
                    //显示当前页数据
                    $scope.list = response.rows;
                    //更新总记录数
                    $scope.paginationConf.totalItems = response.total;
                } else {//如果为空
                    alert("对不起,未找到您要查询的品牌信息,请重新输入查询条件!");
                    $scope.reloadList();
                }

            }
        );
    }
})