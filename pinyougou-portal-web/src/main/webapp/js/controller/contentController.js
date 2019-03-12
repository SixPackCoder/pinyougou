app.controller('contentController',function ($scope,contentService) {

    $scope.contentList=[];//广告集合
    //根据广告分类id查询广告列表
    $scope.findByCategoryId = function (categoryId) {
        contentService.findByCategoryId(categoryId).success(
            function (response) {
                $scope.contentList[categoryId] = response;
            }
        );
    }

    //对接搜索页  (传递搜索的关键字到搜索界面) 搜索页跳转
$scope.search = function () {
    location.href = "http://localhost:9104/search.html#?keywords="+$scope.keywords;
}

})