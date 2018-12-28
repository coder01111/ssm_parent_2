app.controller('indexController',function ($scope, $controller, indexService) {
    $controller('baseController',{$scope:$scope})   //继承，通过引用BaseController里面的$scope的变量
    $scope.username=function () {
        indexService.username().success(function (response) {
            $scope.username=response.loginName;
        })
    }
})