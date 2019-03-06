 //控制层 
app.controller('goodsController' ,function($scope,$controller,goodsService,itemCatService){
	
	$controller('baseController',{$scope:$scope});//继承
	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		goodsService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		goodsService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(id){				
		goodsService.findOne(id).success(
			function(response){
				$scope.entity= response;					
			}
		);				
	}
	
	//保存 
	$scope.save=function(){				
		var serviceObject;//服务层对象  				
		if($scope.entity.id!=null){//如果有ID
			serviceObject=goodsService.update( $scope.entity ); //修改  
		}else{
			serviceObject=goodsService.add( $scope.entity  );//增加 
		}				
		serviceObject.success(
			function(response){
				if(response.success){
					//重新查询 
		        	$scope.reloadList();//重新加载
				}else{
					alert(response.message);
				}
			}		
		);				
	}
	
	 
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		goodsService.dele( $scope.selectedIds ).success(
			function(response){
				if(response.success){
					$scope.reloadList();//刷新列表
					$scope.selectedIds=[];
				}						
			}		
		);				
	}
	
	$scope.searchEntity={};//定义搜索对象 
	
	//搜索
	$scope.search=function(page,rows){			
		goodsService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}

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
	//审核商品  修改商品状态
	$scope.updateStatus = function (status) {
		goodsService.updateStatus($scope.selectedIds,status).success(
		function (response) {
			if(response.success){//成功
				$scope.reloadList();//刷新列表
				$scope.selectedIds=[];//清空ID集合
			}else{
				alert(response.message);
			}
		}
		);
	}
    
});	
