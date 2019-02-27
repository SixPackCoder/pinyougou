app.controller('brandController', function ($scope, $http, brandService) {
    //读取列表数据绑定到表单中
    //查询所有
    $scope.findAll = function () {
        bandService.findAll.success(
            function (response) {
                $scope.list = response;
            }
        );
    };

    //分页控件配置
    //分页控件配置currentPage:当前页   totalItems :总记录数  itemsPerPage:每页记录数  perPageOptions :分页选项  onChange:当页码变更后自动触发的方法
    $scope.paginationConf = {
        currentPage: 1,
        totalItems: 10,
        itemsPerPage: 10,
        perPageOptions: [10, 20, 30, 40, 50],
        onChange: function () {
            $scope.reloadList();//重新加载
        }
    }
    //刷新列表
    $scope.reloadList = function () {
        $scope.search($scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);
    }

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
    }

    //新建
    $scope.add = function () {
        var object = null;
        if ($scope.entity.id != null) {
            object = bandService.update($scope.entity)
        } else {
            object = bandService.add($scope.entity)
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

    //定义一个集合用于存放被选中的id
    $scope.selectedIds = [];
    //更新ID集合
    $scope.updateIds = function ($event, id) {
        if ($event.target.checked) {
            $scope.selectedIds.push(id);
        } else {
            var index = $scope.selectedIds.indexOf(id);
            $scope.selectedIds.splice(index, 1);
        }
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
        brandService.search(page,size,$scope.searchEntity).success(
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
}