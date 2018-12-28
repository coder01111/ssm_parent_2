app.controller('seckillGoodsController', function ($scope, $interval, $location, seckillGoodsService) {
    //读取列表数据绑定到表单中
    $scope.findList = function () {
        seckillGoodsService.findList().success(
            function (response) {
                $scope.list = response;
            }
        );
    }
    //查询商品
    $scope.findOne = function () {
        //接收参数ID
        var id = $location.search()['id'];
        seckillGoodsService.findOne(id).success(
            function (response) {
                $scope.entity = response;
                //倒计时开始
                //获取从结束时间到当前日期的秒数
                allsecond = Math.floor((new Date($scope.entity.endTime).getTime() - new Date().getTime()) / 1000);
                //定义定时器页面显示倒计时
                time = $interval(function () {
                    allsecond = allsecond - 1;
                    //把秒转换为小时分
                    $scope.timeString = convertTimeString(allsecond);
                    //判断
                    if (allsecond <= 0) {
                        //取消定时
                        $interval.cancel(time);
                    }
                }, 1000)
            }
        );
    }
    //转换秒为，天时，分，秒
    convertTimeString = function () {
        var days = Math.floor(allsecond / (60 * 60 * 24));//天数
        var hours = Math.floor((allsecond - days * 60 * 60 * 24) / (60 * 60));//小时数
        var minutes = Math.floor((allsecond - days * 60 * 60 * 24 - hours * 60 * 60) / 60);//分钟数
        var seconds = allsecond - days * 60 * 60 * 24 - hours * 60 * 60 - minutes * 60; //秒数
        var timeString = ""

        if (days > 0) {
            timeString = days + "天 ";
        }
        if (days < 10 && days > 0) {
            days+="";
            days="0"+days;

        }
        if (hours > 0 && hours < 10) {
            hours+="";
            hours="0"+days;

        }
        if (minutes > 0 && minutes < 10) {
            minutes+="";
            minutes="0"+days;

        }
        if (seconds > 0 && seconds < 10) {
            seconds+="";
            seconds="0"+days;

        }
        return timeString + hours + ":" + minutes + ":" + seconds;
    }

    //下单的控制层方法
    $scope.submitOrder=function () {
        seckillGoodsService.submitOrder($scope.entity.id).success(function (response) {
            if(response.success){//如果下单成功
                alert("抢购成功，请在5分钟之内完成支付");
                location.href="pay.html";//跳转到支付页面
            }else{
                alert(response.message);
            }
        })
    }
});
