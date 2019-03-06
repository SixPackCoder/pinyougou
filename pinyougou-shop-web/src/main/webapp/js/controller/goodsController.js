//控制层
app.controller('goodsController', function ($scope, $controller, $location, goodsService, uploadService, itemCatService, typeTemplateService) {

    $controller('baseController', {$scope: $scope});//继承

    //读取列表数据绑定到表单中  
    $scope.findAll = function () {
        goodsService.findAll().success(
            function (response) {
                $scope.list = response;
            }
        );
    };

    //分页
    $scope.findPage = function (page, rows) {
        goodsService.findPage(page, rows).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    };

    //查询实体
    $scope.findOne = function () {
        var id = $location.search()['id'];
        if (id == null) {
            return;
        }
        goodsService.findOne(id).success(
            function (response) {
                $scope.entity = response;
                //向富文本编辑器添加内容
                editor.html($scope.entity.goodsDesc.introduction)
                //图片
                $scope.entity.goodsDesc.itemImages = JSON.parse($scope.entity.goodsDesc.itemImages)
                //显示扩展属性
                $scope.entity.goodsDesc.customAttributeItems = JSON.parse($scope.entity.goodsDesc.customAttributeItems);
                //显示规格属性
                $scope.entity.goodsDesc.specificationItems = JSON.parse($scope.entity.goodsDesc.specificationItems)

                //sku列表

                for (var i = 0; i < $scope.entity.itemList.length ; i++) {
                    $scope.entity.itemList[i].spec = JSON.parse( $scope.entity.itemList[i].spec);
                }
            }
        );
    };


    //判断规格选项是否被选中  哪些被选中
    $scope.checkAttributeValue = function (specName, optionName) {
        var items = $scope.entity.goodsDesc.specificationItems;
        var object = $scope.searchObjectByKey(items, 'attributeName', specName);
        if(object!=null){
            if (object.attributeValue.indexOf(optionName)>=0){
                return true;
            }else {
                return false;
            }
        }else {
            false;
        }
    }

    //保存
    $scope.save = function () {
        $scope.entity.goodsDesc.introduction = editor.html();
        var serviceObject;//服务层对象
        if ($scope.entity.goods.id != null) {//如果有ID
            serviceObject = goodsService.update($scope.entity); //修改
        } else {
            serviceObject = goodsService.add($scope.entity);//增加
        }
        serviceObject.success(
            function (response) {
                if (response.success) {
                    alert("保存商品成功!");
                    location.href="goods.html";//跳转到商品列表页
                } else {
                    alert(response.message);
                }
            }
        );
    };


    //批量删除
    $scope.dele = function () {
        //获取选中的复选框
        goodsService.dele($scope.selectIds).success(
            function (response) {
                if (response.success) {
                    $scope.reloadList();//刷新列表
                    $scope.selectIds = [];
                }
            }
        );
    };

    $scope.searchEntity = {};//定义搜索对象

    //搜索
    $scope.search = function (page, rows) {
        goodsService.search(page, rows, $scope.searchEntity).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    };

    //上传文件
    $scope.uploadFile = function () {
        uploadService.uploadFile().success(
            function (response) {
                //成功===>去除url
                if (response.success) {
                    $scope.image_entity.url = response.message;//设置文件地址
                } else {
                    alert(response.message)
                }
            }).error(function () {
            alert("上传发生错误");
        });

    };


    $scope.entity = {goods: {}, goodsDesc: {itemImages: [], specificationItems: []}};//定义页面实体结构
    $scope.add_image_entity = function () {
        $scope.entity.goodsDesc.itemImages.push($scope.image_entity)
    };


    //删除商品图片
    $scope.remove_image_entity = function (index) {
        $scope.entity.goodsDesc.itemImages.splice(index, 1);
    };

    //读取一级分类
    $scope.selectItemCat1List = function () {
        itemCatService.findByParentId(0).success(
            function (response) {
                $scope.itemCat1List = response;
            }
        )
    };

    //读取二级分类
    $scope.$watch('entity.goods.category1Id', function (newValue, oldValue) {
        itemCatService.findByParentId(newValue).success(
            function (response) {
                $scope.itemCat2List = response;
                //当检测到第一级分类发生改变 应清空第三季分类的显示内容和模板id
                $scope.itemCat3List = {};
                $scope.entity.goods.typeTemplateId = null;
            }
        )
    });

    //读取三级分类
    $scope.$watch('entity.goods.category2Id', function (newValue, oldValue) {
        itemCatService.findByParentId(newValue).success(
            function (response) {
                $scope.itemCat3List = response;
                //当检测到第二级分类发生改变 应清空模板id
                $scope.entity.goods.typeTemplateId = null
            }
        )
    });

    //读取模板id
    $scope.$watch('entity.goods.category3Id', function (newValue, oldValue) {
        itemCatService.findOne(newValue).success(
            function (response) {
                $scope.entity.goods.typeTemplateId = response.typeId;
            }
        );
    });

    //获取品牌列表 扩展属性 规格列表
    $scope.$watch('entity.goods.typeTemplateId', function (newValue, oldValue) {
        typeTemplateService.findOne(newValue).success(
            function (response) {
                $scope.typeTemplate = response;//获取到的是模板对象
                // $scope.typeTemplate.brandIds获取的是一个json字符串  需要转换成json对象重新赋值
                //获取的品牌列表
                $scope.typeTemplate.brandIds = JSON.parse($scope.typeTemplate.brandIds);
                //获取的扩展属性
                //$scope.typeTemplate.customAttributeItems获取的是模板表中的扩展属性
                //但是是赋值给需要保存的变量 goodsDesc表中的扩展属性

                //如果没有ID，则加载模板中的扩展数据
                if ($location.search()['id'] == null) {
                    $scope.entity.goodsDesc.customAttributeItems = JSON.parse($scope.typeTemplate.customAttributeItems);
                }
            }
        );
        //获取规格列表
        typeTemplateService.findSpecList(newValue).success(
            function (response) {
                $scope.SpecList = response;
            }
        );
    });


    //更新规格选项[{“attributeName”:”网络”,”attributeValue”:[“移动2G”,“移动3G”.... ]} , ....  ]中的key和value
    $scope.updateSpecAttribute = function ($event, name, value) {
        var object = $scope.searchObjectByKey($scope.entity.goodsDesc.specificationItems, 'attributeName', name);
        if (object != null) {//集合里面有对象
            if ($event.target.checked) {//被勾选
                object.attributeValue.push(value);//就向attributeValue集合里面添加一个规格选项
            } else {//否则是被取消勾选的话
                object.attributeValue.splice(object.attributeValue.indexOf(value), 1);//attributeValue集合里面减少对应的一个规格选项
                if (object.attributeValue.length == 0) {//如果attributeValue集合里面一个规格都没有了  那么对应的attributeName也应该消失
                    $scope.entity.goodsDesc.specificationItems.splice($scope.entity.goodsDesc.specificationItems.indexOf(object), 1);
                }
            }
        } else {//集合里面没有对象  那就向集合中添加attributeName和attributeValue集合
            $scope.entity.goodsDesc.specificationItems.push(
                {"attributeName": name, "attributeValue": [value]});
        }
    };

    //生成sku列表  用到深克隆
    $scope.createItemList = function () {
        //初始化items列表
        $scope.entity.itemList = [{spec: {}, price: 0, num: 998, status: '0', isDefault: '0'}];
        var items = $scope.entity.goodsDesc.specificationItems;
        for (var i = 0; i < items.length; i++) {
            $scope.entity.itemList = addColumn($scope.entity.itemList, items[i].attributeName, items[i].attributeValue);
        }
    };

    //添加列值
    addColumn = function (list, columnName, columnValues) {
        var newList = [];
        for (var i = 0; i < list.length; i++) {
            var oldRow = list[i];
            for (var j = 0; j < columnValues.length; j++) {
                var newRow = JSON.parse(JSON.stringify(oldRow));//深克隆
                newRow.spec[columnName] = columnValues[j];
                newList.push(newRow)
            }
        }
        return newList;
    };


    //审核状态
    // 未申请
    // 申请中
    // 审核通过
    // 已驳回
    $scope.status = ['未审核', '审核中', '审核通过', '已驳回'];

    $scope.itemCatList = [];//商品分类列表
    //商品的上级分类列表  与商品状态的处理方式差不多
    $scope.findItemCatList = function () {
        itemCatService.findAll().success(
            function (response) {
                for (var i = 0; i < response.length; i++) {
                    //我们需要根据分类ID得到分类名称，所以我们将返回的分页结果以数组形式再次封装。
                    $scope.itemCatList[response[i].id] = response[i].name;
                }
            }
        )
    }

});
