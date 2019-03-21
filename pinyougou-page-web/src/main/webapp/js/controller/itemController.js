//商品详情页控制层
app.controller('itemController', function ($scope, $http) {
    $scope.specificationItems = {};//存储用户选择的规格
//商品数量的加减
    $scope.addNum = function (x) {
        $scope.num = parseInt($scope.num);
        $scope.num = $scope.num + x;
        if ($scope.num < 1) {
            $scope.num = 1;
        }
    };

//记录用户选择的规格
    $scope.selectSpecification = function (name, value) {
        $scope.specificationItems[name] = value;
        searchSku();//读取sku
    };

//判断某规格是否被用户选中
    $scope.isSelected = function (key, value) {
        if ($scope.specificationItems[key] == value) {
            return true;
        } else {
            return false;
        }
    };

//选择当前SKU
    $scope.sku = {};


    //加载默认SKU
    $scope.loadSku = function () {
        $scope.sku = skuList[0];	//默认第一个商品
        $scope.specificationItems = JSON.parse(JSON.stringify($scope.sku.spec));//规格列表  深克隆
    };

//匹配两个对象是否相等
    matchObject = function (map1, map2) {
        for (var k in map1) {
            if (map1[k] != map2[k]) {
                return false;
            }
        }
        for (var k in map2) {
            if (map2[k] != map1[k]) {
                return false;
            }
        }
        return true;
    };

    //查询SKU
    searchSku = function () {
        for (var i = 0; i < skuList.length; i++) {
            if (matchObject(skuList[i].spec, $scope.specificationItems)) {
                $scope.sku = skuList[i];
                return;
            }
        }
        $scope.sku = {id: 0, title: '--------', price: 0};//如果没有匹配的
    };

//添加商品到购物车
    $scope.addToCart = function () {
        //alert('skuId:'+$scope.sku.id);
        $http.get('http://localhost:9107/cart/addGoodsToCartList.do?itemId='
            + $scope.sku.id
            + '&num=' + $scope.num, {'withCredentials': true}).success(//{'withCredentials':true}携带证书才能跨域请求
            function (response) {
                if (response.success) {//跳转到购物车页面
                    location.href = 'http://localhost:9107/cart.html';
                } else {
                    alert(response.message)
                }
            }
        );
    }


});