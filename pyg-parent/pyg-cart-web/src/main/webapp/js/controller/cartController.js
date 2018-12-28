//购物车控制层
app.controller('cartController',function($scope,cartService){
	//查询购物车列表
	$scope.findCartList=function(){
		cartService.findCartList().success(
			function(response){
				$scope.cartList=response;
				$scope.totalValue= cartService.sum($scope.cartList);
			}
		);
	}
	
	//数量加减
	$scope.addGoodsToCartList=function(itemId,num){
		cartService.addGoodsToCartList(itemId,num).success(
			function(response){
				if(response.success){//如果成功
					$scope.findCartList();//刷新列表
				}else{
					alert(response.message);
				}				
			}		
		);		
	}
	
	//求合计
	/*
	sum=function(){
		$scope.totalNum=0;//总数量
		$scope.totalMoney=0;//总金额
		
		for(var i=0;i<$scope.cartList.length ;i++){
			var cart=$scope.cartList[i];//购物车对象
			for(var j=0;j<cart.orderItemList.length;j++){
				var orderItem=  cart.orderItemList[j];//购物车明细
				$scope.totalNum+=orderItem.num;//累加数量
				$scope.totalMoney+=orderItem.totalFee;//累加金额				
			}			
		}
	}
	*/
	//根据用户名查找出所有地址列表,并确定默认地址
	$scope.findListByLoginUser=function () {
		cartService.findListByLoginUser().success(function (response) {
			$scope.addressList=response;
			//循环找到默认地址
			for(var i = 0;i<$scope.addressList.length;i++){
				if($scope.addressList[i].isDefault=="1"){
                    $scope.address=$scope.addressList[i];
                    break;
				}
			}
        })
    }
		//选择地址
    $scope.selectAddress=function (address) {
		$scope.address=address;
    }
    //判断某地址对象是不是当前选择的地址
	$scope.isSeletedAddress=function (address) {
		if(address==$scope.address){
			return true;
		}
		else {
			return false;
		}
    }

    $scope.order={paymentType:'1'};//订单对象
//选择支付类型
    $scope.selectPayType=function(type){
        $scope.order.paymentType=type;
    }
    //提交订单操作
    $scope.submitOrder=function () {
        $scope.order.receiverAreaName=$scope.address.address;//地址
        $scope.order.receiverMobile=$scope.address.mobile;//手机
        $scope.order.receiver=$scope.address.contact;//联系人
        cartService.submitOrder($scope.order).success(function (response) {
            if(response.success) {
                //页面跳转
                if ($scope.order.paymentType == '1') {//如果是微信支付，跳转到支付页面
                    location.href = "pay.html";
                }
                else {//如果货到付款，跳转到提示页面
                    location.href = "paysuccess.html";
                }
            }else{
                alert(response.message);	//也可以跳转到提示页面
            }
        })
    }


    //查询实体
    $scope.findOne=function(id){
        cartService.findOne(id).success(
            function(response){
                $scope.entity= response;
            }
        );
    }

    //保存
    $scope.save=function(){
        var serviceObject;//服务层对象
        if($scope.entity.id!=null){//如果有ID
            serviceObject=cartService.update( $scope.entity ); //修改
        }else{
            serviceObject=cartService.add( $scope.entity  );//增加
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
        cartService.dele( $scope.selectIds ).success(
            function(response){
                if(response.success){
                    $scope.reloadList();//刷新列表
                    $scope.selectIds=[];
                }
            }
        );
    }
    
});