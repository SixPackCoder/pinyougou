//控制层
app.controller('typeTemplateController', function ($scope, $controller, typeTemplateService, brandService, specificationService) {

    $controller('baseController', {$scope: $scope});//继承

    //读取列表数据绑定到表单中  
    $scope.findAll = function () {
        typeTemplateService.findAll().success(
            function (response) {
                $scope.list = response;
            }
        );
    }

    //分页
    $scope.findPage = function (page, rows) {
        typeTemplateService.findPage(page, rows).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    }

    //查询实体
    $scope.findOne = function (id) {
        typeTemplateService.findOne(id).success(
            function (response) {
                $scope.entity = response;
                /**
                 * 从数据库中查询出来的是字符串，我们必须将其转换为json对象才能实现信息的回显。
                 * @type {any}
                 */
                $scope.entity.brandIds = JSON.parse($scope.entity.brandIds);//转换品牌列表
                $scope.entity.specIds = JSON.parse($scope.entity.specIds);//转换规格列表
                $scope.entity.customAttributeItems = JSON.parse($scope.entity.customAttributeItems);//转换扩展属性

            }
        );
    }

    //保存
    $scope.save = function () {
        var serviceObject;//服务层对象
        if ($scope.entity.id != null) {//如果有ID
            serviceObject = typeTemplateService.update($scope.entity); //修改
        } else {
            serviceObject = typeTemplateService.add($scope.entity);//增加
        }
        serviceObject.success(
            function (response) {
                if (response.success) {
                    //重新查询
                    $scope.reloadList();//重新加载
                } else {
                    alert(response.message);
                }
            }
        );
    }


    //批量删除
    $scope.dele = function () {
        if ($scope.selectedIds.length > 0) {
            if (confirm("确认要删除选中项吗?")) {
                typeTemplateService.dele($scope.selectedIds).success(
                    function (response) {
                        if (response.success) {
                            $scope.reloadList();
                            $scope.selectedIds = [];
                        } else {
                            alert(response.message)
                        }
                    })
            } else {
                return;
            }
        } else {
            alert("请选择要删除的规格");
            $scope.reloadList();
        }
    };

    $scope.searchEntity = {};//定义搜索对象

    //搜索
    $scope.search = function (page, rows) {
        typeTemplateService.search(page, rows, $scope.searchEntity).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    }
    //查询品牌列表
    $scope.brandList = {data: []};//初始化品牌列表内容
    $scope.findBrandList = function () {
        brandService.selectOptionList().success(
            function (response) {
                $scope.brandList = {data: response};
            }
        );
    }

    //查询规格列表
    $scope.specList = {data: []};//初始化规格列表内容
    $scope.findSpecList = function () {
        specificationService.selectOptionList().success(
            function (response) {
                $scope.specList = {data: response};
            }
        );
    }

    //增加扩展属性选项
    $scope.addTableRow = function () {
        /** @namespace $scope.entity.customAttributeItems */
        $scope.entity.customAttributeItems.push({});
    }

    //删除规格选项行
    $scope.deleTableRow = function (index) {
        $scope.entity.customAttributeItems.splice(index, 1);
    }


});	
