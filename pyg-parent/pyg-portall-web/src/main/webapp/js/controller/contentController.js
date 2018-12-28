//广告控制层（运营商后台）
app.controller("contentController",function($scope, contentService){
    $scope.contentList=[];//广告集合,所有广告的大集合每一个元素都是数组
    $scope.findByCategoryId=function(categoryId){
        contentService.findByCategoryId(categoryId).success(
            function(response){
                //给数组每一个对应元素赋值
                $scope.contentList[categoryId]=response;
            }
        );
    }
    //搜索跳转
    $scope.search=function () {
        location.href="http://localhost:9005/search.html#?keywords="+$scope.keywords;
    }

});
