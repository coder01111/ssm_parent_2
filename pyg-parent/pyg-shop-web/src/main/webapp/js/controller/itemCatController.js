//控制层
app.controller('itemCatController', function ($scope, $controller, itemCatService) {

    $controller('baseController', {$scope: $scope});//继承

    //读取列表数据绑定到表单中  
    $scope.findAll = function () {
        itemCatService.findAll().success(
            function (response) {
                $scope.list = response;
            }
        );
    }

    //分页
    $scope.findPage = function (page, rows) {
        itemCatService.findPage(page, rows).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    }

    //查询实体
    $scope.findOne = function (id) {
        itemCatService.findOne(id).success(
            function (response) {
                $scope.entity = response;
            }
        );
    }

    //保存，保存前首先要找到处于哪个等级根据parentId来找到处于哪个等级然后获取名称
    //定义一个父id
    $scope.parentId = 0;

    $scope.save = function () {

        var serviceObject;//服务层对象
        //赋予上级id
        $scope.entity.parentId = $scope.parentId;
        if ($scope.entity.id != null) {//如果有ID
            serviceObject = itemCatService.update($scope.entity); //修改
        } else {
            serviceObject = itemCatService.add($scope.entity);//增加
        }
        serviceObject.success(
            function (response) {
                if (response.success) {
                    //添加成功后，重新查询
                    $scope.findByParentId(  $scope.grade, {id :$scope.parentId});
                    console.log($scope.entity)
                } else {
                    alert(response.message);
                }
            }


        );
    }


    //批量删除
    $scope.dele = function () {
        //获取选中的复选框
        itemCatService.dele($scope.selectIds).success(
            function (response) {
                if (response.success) {
                    $scope.findByParentId(  $scope.grade, {id :0});//刷新列表
                    $scope.selectIds = [];
                }
            }
        );
    }

    $scope.searchEntity = {};//定义搜索对象

    //搜索
    $scope.search = function (page, rows) {
        itemCatService.search(page, rows, $scope.searchEntity).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    }
    //定义一个分类等级,初始化为顶级0
        //根据父id查询所有顶级列表，因为还要在导航窗口显示面包屑所以需要把名字也带过来
    //干脆直接传一个实体类,在控制器定义的模型必须要写$scope才能在视图中用表达式取得
    $scope.findByParentId = function (grade, entity) {
        $scope.grade = grade;
        //给模型赋值
        //给上级id赋值，上面定义的上级id
        $scope.parentId = entity.id;
        //判断处于哪个树枝下，判断树的分支
        if ($scope.grade == 0) {
            //顶级分支
            $scope.entity_1 = null;
            $scope.entity_2 = null;
        }
        if ($scope.grade == 1) {
            //1级分支
            //1级分支的列表用它的id来找他的下一级，并且拿到它的名字
            $scope.entity_1 = entity;
            $scope.entity_2 = null;
        }
        if ($scope.grade == 2) {
            //2级分支
            $scope.entity_2 = entity;
        }
        itemCatService.findByParentId(entity.id).success(
            function (response) {
                $scope.list = response;
            }
        );
    }
    //判断是否可以勾选删除，有下级的不能删除
    $scope.canDelete=function ($event, id) {
        //现根据id查询是否有下一级然后判断集合的长度
        itemCatService.findByParentId(id).success(function (res) {
            if(res.length>0) {
                $event.target.checked=false;//选了改为没选
                $event.target.disabled=true;//禁用
                var indexOfId = $scope.selectIds.indexOf(id);
                $scope.selectIds.splice(indexOfId, 1);
            }
        });
    }
    //定义下拉列表里面的数据来源必须时这种data格式
    $scope.typeList = {data: []};
    //然后调用brandService里面的查询服务得到真实的动态从数据库的数据
    $scope.findTypeList = function () {
        itemCatService.selectOptionList().success(function (response) {
            $scope.typeList = {data: response};
            console.log($scope.typeList)
        })
    }
});	
