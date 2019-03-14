app.controller('searchController', function ($scope, searchService,$location) {
    //定义搜索对象的结构  category :商品分类名称 --手机 电视  电脑等等
    $scope.searchMap = {
        'keywords': '',
        'category': '',
        'brand': '',
        'spec': {},
        'price': '',
        'pageNo': 1,
        'pageSize': 20,
        'sortField': '',//排序的字段
        'sortMethod': ''//排序的方式 降序 desc  升序 asc
    };
    //搜索
    $scope.search = function () {
        $scope.searchMap.pageNo = parseInt($scope.searchMap.pageNo);//无论页面传过来的当前页是字符串还是数字类型  统统转换成int类型

        searchService.search($scope.searchMap).success(
            function (response) {
                $scope.resultMap = response;
                buildPageLabel();
            }
        );
    };

    //构建分页标签
    buildPageLabel = function () {
        $scope.pageLabel = [];//存放页码标签  [1,2,3,4,5,6,7,8.....]

        var firstPage = 1;//开始页码
        var lastPage = $scope.resultMap.totalPages;//结束页码
        $scope.firstDot = true;//分页前面有...
        $scope.lastDot = true;//分页后面有...

        if (lastPage > 5) { //总页数大于5
            if ($scope.searchMap.pageNo <= 3) {//如果当前页小于等于3 那么结束页就是5
                lastPage = 5;
                $scope.firstDot = false;//分页前面没有...
            } else if ($scope.searchMap.pageNo >= lastPage - 2) { //如果当前页大于等于最后页-2 那么开始页等于最后页-4
                firstPage = lastPage - 4;
                $scope.lastDot = false;//分页后面没有...
            } else { //否则 开始页等于当前页-2  结束页等于当前页+2
                firstPage = $scope.searchMap.pageNo - 2;
                lastPage = $scope.searchMap.pageNo + 2;
            }
        } else {//总页数小于5
            $scope.firstDot = false;//分页前面没有...
            $scope.lastDot = false;//分页后面没有...
        }
        for (var i = firstPage; i <= lastPage; i++) { //循环遍历总记录数 装在页码标签数组中
            $scope.pageLabel.push(i);
        }
    };

    //添加搜索项
    $scope.addSearchItem = function (key, value) {
        if (key == 'category' || key == 'brand' || key == 'price') {//如果用户点击的是分类名称或品牌
            $scope.searchMap[key] = value;
        } else {//用户点击的规格
            $scope.searchMap.spec[key] = value;
        }
        return $scope.search();//执行搜索
    }

    //移除搜索项
    $scope.removeSearchItem = function (key, value) {
        if (key == 'category' || key == 'brand' || key == 'price') {//如果用户点击的是分类名称或品牌
            $scope.searchMap[key] = "";
        } else {//用户点击的规格
            delete $scope.searchMap.spec[key]
        }
        return $scope.search();//执行搜索
    }

    //点击页码查询
    $scope.queryByPage = function (pageNo) { //用户点击页码进行查询 传递当前页页码
        if (pageNo < 1 || pageNo > $scope.resultMap.totalPages) { //当前页小于1  或者当前页大于最后一页  不进行查询
            return;
        }
        $scope.searchMap.pageNo = pageNo; //设置当前页为用户点击的 然后调用search()方法
        $scope.search();
    }

    //判断当前页是否是第一页
    $scope.isTopPage = function () {
        if ($scope.searchMap.pageNo == 1) {
            return true;
        } else {
            return false;
        }
    }

    //判断当前页是否是最后一页
    $scope.isEndPage = function () {
        if ($scope.searchMap.pageNo == $scope.resultMap.totalPages) {
            return true;
        } else {
            return false;
        }
    }

    //设置排序规则
    $scope.sortSearch = function (sortField, sortMethod) {
        $scope.searchMap.sortField = sortField;
        $scope.searchMap.sortMethod = sortMethod;
        $scope.search();
    }

    //判断关键字是不是品牌
    $scope.keywordsIsBrand = function () {
        for (var i = 1; i < $scope.resultMap.brandList.length; i++) {
            if ($scope.searchMap.keywords.indexOf($scope.resultMap.brandList[i].text)>=0){ //如果关键字包含了品牌
                    return true;
            }
        }
        return false;
    }

    //加载首页的搜索跳转
    $scope.loadKeywords = function () {
        $scope.searchMap.keywords = $location.search()['keywords'];
        $scope.search();
    }

});