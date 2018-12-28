//品牌控制层
app.controller('baseController', function ($scope) {

    //重新加载列表 数据
    $scope.reloadList = function () {
        //切换页码
        $scope.search($scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);
    }

    //分页控件配置
    $scope.paginationConf = {
        currentPage: 1,
        totalItems: 10,
        itemsPerPage: 10,
        perPageOptions: [10, 20, 30, 40, 50],
        onChange: function () {
            $scope.reloadList();//重新加载
        }
    };

    $scope.selectIds = [];//选中的ID集合

    //更新复选
    $scope.updateSelection = function ($event, id) {
        if ($event.target.checked) {//如果是被选中,则增加到数组
            $scope.selectIds.push(id);
        } else {
            var idx = $scope.selectIds.indexOf(id);
            $scope.selectIds.splice(idx, 1);//删除 
        }
    }
    //提取json字符串数据中某个属性，返回拼接字符串 逗号分隔
    // json字符串中某个属性的值提取出来，用逗号拼接成一个新的字符串。
    $scope.jsonToString = function (jsonString, key) {
        var json = JSON.parse(jsonString);//将json字符串转换为json对象
        var value = "";
        //遍历json对象
        for (var i = 0; i < json.length; i++) {
            if (i > 0) {
                value += ","
            }
            value += json[i][key];

        }
        return value;
    }
    //定义一个	//从集合中按照key查询对象的方法,key指的是集合中的属性attributeName,keyvalue就是规格名称
    ////[{“attributeName”:”规格名称”,”attributeValue”:[“规格选项1”,“规格选项2”.... ]  } , ....  ]
    $scope.searchObjectByKey=function (list, key,keyValue) {
        //遍历集合，判断规格名称key是否存在
        for(var i = 0; i<list.length;i++){
            if(list[i][key]==keyValue){
                //说明已存在直接返回这个对象
                return list[i];
            }
        }
            return null;
    }


});	