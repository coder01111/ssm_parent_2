//控制层
app.controller('goodsController', function ($scope, $controller, $location, goodsService, uploadService, itemCatService, typeTemplateService) {

    $controller('baseController', {$scope: $scope});//继承

    //读取列表数据绑定到表单中  
    $scope.findAll = function () {
        goodsService.findAll().success(
            function (response) {
                $scope.list = response;
            }
        );
    }

    //分页
    $scope.findPage = function (page, rows) {
        goodsService.findPage(page, rows).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    }

    //查询实体不再是根据本页面提供的id查询然后回显而是要跳转页面进行回显，所以不能直接获取id
    //而是通过前端路由的方式要把id带过去
    $scope.findOne = function () {
        //先获取请求地址后面的所有参数,是一个json格式的数组包含了请求的参数
        var id = $location.search()['id'];
        //判断是否有id
        if(id==null){
            return;
        }
        goodsService.findOne(id).success(
            function (response) {
                console.log(response);
                $scope.entity = response;
                ////向富文本编辑器回显商品介绍
                     editor.html($scope.entity.tbGoodsDesc.introduction);
               //显示图片列表，需要把字符串文本转换为json对象
                $scope.entity.tbGoodsDesc.itemImages=JSON.parse($scope.entity.tbGoodsDesc.itemImages);
                //显示扩展属性，同样也要把字符串问吧转换为json对象，因为我们下面新增时有监控
                //模板id的变化模板一变化会清空属性让我们填写，查询的是新增后的
                $scope.entity.tbGoodsDesc.customAttributeItems=JSON.parse($scope.entity.tbGoodsDesc.customAttributeItems);
                //读取规格
                $scope.entity.tbGoodsDesc.specificationItems=JSON.parse($scope.entity.tbGoodsDesc.specificationItems);
                //读取sku列表
                for( var i=0;i<$scope.entity.itemList.length;i++ ){
                    $scope.entity.itemList[i].spec =
                        JSON.parse( $scope.entity.itemList[i].spec);
                }

            }
        );
    }
    //定义一个组合实体的模型初始化
    //保存记录的规格先初始化一个实体模型
    $scope.entity = {
        tbGoods: {},
        tbGoodsDesc: {itemImages: [], specificationItems: []},
        itemList: [{spec: {}, num: 99999, price: 99999, status: 1, isDefault: 1}]
    };
    //保存
    $scope.save = function () {
        var serviceObject;//服务层对象
        //获取商品扩展信息介绍从富文本编辑器提到的
        $scope.entity.tbGoodsDesc.introduction = editor.html();
        if ($scope.entity.tbGoods.id != null) {//如果有ID
            serviceObject = goodsService.update($scope.entity); //修改
        } else {
            //赋予上级id
            $scope.entity.parentId = $scope.parentId;
            serviceObject = goodsService.add($scope.entity);//增加
        }
        serviceObject.success(
            function (response) {
                if (response.success) {
                    alert(response.message);
                    //先清空富文本编辑器的内容
                    editor.html("");
                    //然后再清空整个组合类模型，因为富文本无法这样清除
                    $scope.entity = {};
                } else {
                    alert(response.message);
                }
            }
        );
    }


    //批量删除
    $scope.dele = function () {
        //获取选中的复选框
        goodsService.dele($scope.selectIds).success(
            function (response) {
                if (response.success) {
                    $scope.reloadList();//刷新列表
                    $scope.selectIds = [];
                }
            }
        );
    }

    $scope.searchEntity = {};//定义搜索对象
    $scope.status=['未审核','已审核','审核未通过','关闭'];//优化商品状态不在显示为0和1

    //搜索
    $scope.search = function (page, rows) {
        goodsService.search(page, rows, $scope.searchEntity).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    }
    $scope.image_entity = {};
    //上传图片的方法
    $scope.uploadFile = function () {
        uploadService.uploadFile().success(function (response) {
            if (response.success) {
                $scope.image_entity.url = response.message;
            } else {
                alert(response.message);
            }
        }).error(function () {
            alert("上传错误");
        })
    }
    //添加图片列表
    $scope.add_image_entity = function () {
        $scope.entity.tbGoodsDesc.itemImages.push($scope.image_entity);
    }
    //删除图片
    $scope.dele_image_entity = function (index) {
        $scope.entity.tbGoodsDesc.itemImages.splice(index, 1);
    }
    //定义一个商品分类的方法，获取第一级的列表
    $scope.findCatList1 = function () {
        itemCatService.findByParentId(0).success(function (response) {
            $scope.castList1 = response;
        })
    }
    //获取第二级的列表，根据一级分类的分类id来找到二级分类，监控id的变化
    $scope.$watch("entity.tbGoods.category1Id", function (newValue, old) {
        // console.log(newValue);//这个newValue就是监控的值，也可以不要newValue本题中
        // console.log($scope.entity.tbGoods.category1Id);
        itemCatService.findByParentId(newValue).success(function (response) {
            $scope.castList2 = response;
        })
    })
    //获取第三级的列表
    $scope.$watch("entity.tbGoods.category2Id", function (newValue) {
        itemCatService.findByParentId(newValue).success(function (response) {
            $scope.castList3 = response;
            //    集合里面是一个个模板对象里面有模板的id，我们可以把这个对象提交到一个
            //    模型中然后取出里面的id和模板id再给我们要保存的三级id赋值和在页面显示出模板id
        })
    })
    //获取模板id，监控第三级列表的变化,我们可以提交整个模板打的对象
    $scope.selectedCategory3 = {};
    $scope.$watch("selectedCategory3", function () {
        // console.log($scope.selectedCategory3);
        $scope.entity.tbGoods.category3Id = $scope.selectedCategory3.id;
        $scope.entity.tbGoods.typeTemplateId = $scope.selectedCategory3.typeId;
    })
    //获取模板id后监控模板id来获取可以选取的品牌信息
    $scope.$watch("entity.tbGoods.typeTemplateId", function (newValue) {
        typeTemplateService.findOne(newValue).success(function (response) {
            // console.log(response);
            //获取里面的品牌信息,是一个json串，需要转换json对象
            //[{"id":27,"text":"网络"},{"id":32,"text":"机身内存"}]
            $scope.brandIds = JSON.parse(response.brandIds);
            // console.log($scope.brandIds)
            //获取模板里面的扩展属性，也是一个json字符串需要转换为json对象,直接给扩展属性赋值即可
            //判断一下id是否为空，为空在用模板的扩展属性没有属性值进行替换否则就不替换
            if($location.search()['id']==null){
                //用模板的扩展属性替换
                $scope.entity.tbGoodsDesc.customAttributeItems = JSON.parse(response.customAttributeItems);
            }
            //获取模板里面的规格属性同样也需要转换
            //模板的规格数据[{"id":38,"text":"型号"},options":[{"id":118,"optionName":16G","orders":1,"specId":38}]，
            //把字符串转换为json对象
            $scope.specIds = JSON.parse(response.specIds);
            console.log($scope.specIds);
        })
    })

    //定义一个方法来记录点击保存的规格选项
    //[{“attributeName”:”规格名称”,”attributeValue”:[“规格选项1”,“规格选项2”.... ]  } , ....  ]
    $scope.updateSpecItems = function ($event, text, option) {
        //先判断集合中是否有这个规格名称

        var object = $scope.searchObjectByKey($scope.entity.tbGoodsDesc.specificationItems, 'attributeName', text);
        //判断集合是否为空
        if (object != null) {
            //判断是否选中
            if ($event.target.checked) {
                //选中,直接往attributeValue集合里面添加
                object.attributeValue.push(option);
            } else {
                //取消勾选
                object.attributeValue.splice(object.attributeValue.indexOf(option), 1);
                //如果选项都取消了，将此条记录移除就是只有attributeName，没有attributeValue的时候4
                //直接删除这个元素
                if (object.attributeValue.length == 0) {
                    //从大集合中移除这一个元素
                    $scope.entity.tbGoodsDesc.specificationItems.splice(
                        $scope.entity.tbGoodsDesc.specificationItems.indexOf(object), 1);
                }
            }
        } else {
            //创建一个新的元素
            $scope.entity.tbGoodsDesc.specificationItems.push({"attributeName": text, "attributeValue": [option]});
        }
        createItemList();

    }
    //根据规格选项生成sku列表
    createItemList = function () {
        //先初始化这个sku列表
        $scope.entity.itemList = [{spec: {}, num: 99999, price: 99999, status: 1, isDefault: 1}];
        //遍历$scope.entity.goodsDesc.specificationItems=
        // [{"attributeName":"网络","attributeValue":["移动3G","移动4G"]},{"attributeName":"机身内存","attributeValue":["16G"]}];
        var specItems = $scope.entity.tbGoodsDesc.specificationItems;
        for (var i = 0; i < specItems.length; i++) {
            //先确定树有几层节点
            //树的每一层对应一个集合
            var newSkuList = [];
            //取出规格列表的里面的没一个元素
            var specItem = specItems[i];
            //取出里面的属性名称和值
            var attrName = specItem.attributeName;
            var attrValues = specItem.attributeValue;
            //遍历sku树最后一层的每一个节点
            //此时已经生成了specificationItems的长度个的sku
            //遍历sku树最后一层的每一个节点
            for (var j = 0; j < $scope.entity.itemList.length; j++) {
                var oldSkuItem = $scope.entity.itemList[j];
                //取出里面的旧数据进行深克隆即使序列化和反序列化
                //遍历attrValues集合克隆attrValues生成新的节点设置attrName,attrValues属性
                for (var k = 0; k < attrValues.length; k++) {
                    //先取出里面的值
                    var attrValue = attrValues[k];
                    var newSkuItem = JSON.parse(JSON.stringify(oldSkuItem));
                    //设置属性值到spec
                    newSkuItem.spec[attrName] = attrValue;
                    newSkuList.push(newSkuItem);
                }

            }
            //树新的一层构建完成，赋值给 $scope.entity.itemList存放
            $scope.entity.itemList = newSkuList;
            //删除newSkuList
            delete newSkuList;
        }

    }
    $scope.itemCatList=[];//优化商品分类列表，不在显示一个商品的分类id而是对应的分类名称
    //加载商品分类列表
    $scope.findItemCatList=function () {
        //根据分类id查询分类名称
        itemCatService.findAll().success(function (response) {
            //给数组赋值，返回的是一个分类表的集合，遍历集合，给定义的数组赋值，页面初始化
            for(var i=0;i<response.length;i++){
                //给数组编号,每一个元素是response[i]
                $scope.itemCatList[response[i].id]=response[i].name
            }
        })
    }
    //根据规格名称和选项名称返回是否被勾选,回选被勾选的，用一个ng-checK属性来判断
    //值为true就被勾选，false就不勾选
    $scope.checkAttributeValue=function (specName,optionName) {
        //先把规格选项中的规格选项和属性列表定义出来
        //[{"attributeName":"网络制式","attributeValue":["移动3G","移动4G"]},{"attributeName":"屏幕尺寸","attributeValue":["6寸","5寸"]}]
        var items= $scope.entity.tbGoodsDesc.specificationItems;
        //判断商品规格选项列表中是否含有这个规格属性就是判断有没有这个规格名称   网络制式就是specName
            var object = $scope.searchObjectByKey(items,'attributeName',specName);
            //判断是否非空
        if(object==null){
            return false;
        }else {
            //里面有这个规格我们还要判断里面的规格属性的值
            if(object.attributeValue.indexOf(optionName)>=0){
                //说明里面有这个属性值勾选返回true
                return true;
            }else {
                //说明没有属性也就不用勾选，我们新增的本来就判断；饿这一步之前的
                //可能没有改
                return false;
            }
        }
    }

});	
