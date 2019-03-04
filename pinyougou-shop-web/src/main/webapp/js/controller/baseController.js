app.controller('baseController', function ($scope) {

    //刷新列表
    $scope.reloadList = function () {
        $scope.search($scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);
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
    };

    //更新复选
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

    $scope.jsonToString = function (jsonString, key) {
        var json = JSON.parse(jsonString);
        var value = "";
        for (var i = 0; i < json.length; i++) {
            if (i > 0) {
                value += ", " + json[i][key];
            } else {
                value += json[i][key];
            }
        }
        return value;
    }

    //[{“attributeName”:”网络”,”attributeValue”:[“移动2G”,“移动3G”.... ]} , ....  ]
    //在list集合中查询key  有就返回该对象  没有就返回空
    $scope.searchObjectByKey = function (list, key, keyValue) {
        for (var i = 0; i < list.length; i++) {
            if (list[i][key] ==keyValue){
                return list[i];
            }
        }
        return null;
    }
});

