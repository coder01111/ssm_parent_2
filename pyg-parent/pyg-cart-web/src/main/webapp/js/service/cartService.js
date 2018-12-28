//购物车服务层
app.service('cartService',function($http){
	//购物车列表
	this.findCartList=function(){
		return $http.get('cart/findCartList.do');
	}
	
	//添加商品到购物车
	this.addGoodsToCartList=function(itemId,num){
		return $http.get('cart/addGoodsToCartList.do?itemId='+itemId+'&num='+num);
	}

	//求出总和和总费用的方法可以写在controller但是逻辑比较复杂所以写在服务层，提高可重用性
	this.sum=function (cartList) {
		//定义一个对象用来存储总量和总费用
		var  totalValue={totalNum:0,totalMoney:0};
		for(var i =0;i<cartList.length;i++){
			//再遍历里面的购物车明细列表,得到每一个购物车对象
			for(var j = 0;j<cartList[i].orderItemList.length;j++){
				//获取每个购物车明细
              var orderItem=  cartList[i].orderItemList[j];//购物车明细
				//累加数量
				totalValue.totalNum+=orderItem.num;
				totalValue.totalMoney+=orderItem.totalFee;
			}
		}
		return totalValue;
    }
    this.findListByLoginUser=function () {
		return $http.get("address/findListByLoginUser.do");
    }

    //查询实体
    this.findOne=function(id){
        return $http.get('address/findOne.do?id='+id);
    }
    //增加
    this.add=function(entity){
        return  $http.post('address/add.do',entity );
    }
    //修改
    this.update=function(entity){
        return  $http.post('address/update.do',entity );
    }
    //删除
    this.dele=function(ids){
        return $http.get('address/delete.do?ids='+ids);
    }
    this.submitOrder=function(order){
        return $http.post('order/add.do',order);
    }

});