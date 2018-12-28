app.controller('payController',function ($scope,$location, payService) {
    $scope.createNative=function () {
        payService.createNative().success(function (response) {
            console.log(response.code_url);
            //显示订单号和金额
            $scope.money = (response.total_fee / 100).toFixed(2);
            $scope.out_trade_no = response.out_trade_no;

            //生成二维码
            var or = new  QRious({
                element: document.getElementById('qrious'),
                size: 250,
                value: response.code_url,
                level: 'H'
            });
            //调用查询方法
            queryPayStatus();
        })
    }
    queryPayStatus = function () {
        payService.queryPayStatus($scope.out_trade_no).success(function (response) {
            if(response.success){
                location.href = "paysuccess.html#?money=" + $scope.money;
            }else {
                if (response.message == '二维码超时') {
                    $scope.createNative();//重新生成二维码
                } else {
                    location.href = "payfail.html";
                }

            }
        })
    }
    //获取金额
    $scope.getMoney = function () {
        return $location.search()['money'];
    }

})