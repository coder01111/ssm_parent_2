app.controller('searchController', function ($scope, $location, searchService) {
    //搜索
    //搜索对象
    $scope.searchMap = {'keywords': '', 'category': '', 'brand': '', 'spec': {},'price':'','pageNo':1, 'pageSize':20, 'sort':'1', 'sortField':''};
    $scope.search = function () {
        searchService.search($scope.searchMap).success(
            function (response) {
                $scope.resultMap = response;
                console.log($scope.resultMap);
                console.log(JSON.stringify($scope.resultMap));
                console.log('------------');
                console.log(JSON.stringify($scope.searchMap));
                $scope.buildPageLable();
            }
        );
    }
    $scope.buildPageLable=function(){
        $scope.pageLable = [];

        $scope.showStartDot = false;
        $scope.showEndDot = false;

        var totalPages = $scope.resultMap.totalPages;
        var start = 1;
        var end = totalPages;
        if(totalPages>5) {
            //显示前面5页
            if($scope.searchMap.pageNo<=3) {
                start=1;
                end = 5;
                $scope.showEndDot = true;

            }else if($scope.searchMap.pageNo>=totalPages-2) {   //显示最后5页
                start = totalPages - 4;
                end = totalPages;
                $scope.showStartDot = true;

            } else {   //显示中间的5页
                start = $scope.searchMap.pageNo - 2;
                end = $scope.searchMap.pageNo + 2;
                $scope.showStartDot = true;
                $scope.showEndDot = true;
            }
        }
        //循环产生页码标签
        for(var i=start;i<=end;i++) {
            $scope.pageLable.push(i);
        }
    }

    //添加搜索项
    $scope.addSearchItem = function (key, value) {
        if (key == 'category' || key == 'brand' || key=='price'|| 'pageNo'==key) {//如果点击的是分类或者是品牌
            if('pageNo'==key) {
                value = parseInt(value);
            }
            $scope.searchMap[key] =  value;
        } else {
            $scope.searchMap.spec[key] = value;
        }
        $scope.search();//执行搜索
    }
    //移除搜索项
    $scope.removeSearchItem = function (key) {
        if ('category' == key || 'brand' == key || key=='price') {
            $scope.searchMap[key] = '';
        } else {
            delete $scope.searchMap.spec[key];
        }

    }
    $scope.search();//执行搜索



    //判断当前是否已经到了第一页
    $scope.isTopPage=function () {
        return $scope.searchMap.pageNo<=1;
    }

    //判断当前是否到了最后一页
    $scope.isEndPage=function () {
        return $scope.searchMap.pageNo==$scope.searchMap.pageSize;
    }

    $scope.sortSearch=function (sortField,sort) {
        $scope.searchMap.sortField=sortField;
        $scope.searchMap.sort=sort;
        //执行搜素
        $scope.search();
    }
    //判断searchMap关键字是不是品牌
    $scope.keywordsIsBrand=function () {
        var brandList = $scope.resultMap.brandList;
        for(var i=0;i<brandList.length;i++) {
            if($scope.searchMap.keywords.indexOf(brandList[i].text)>=0) {
                return true;
            }
        }
        return false;
    }

    //加载查询字符串,从首页传递过来关键字查询传到搜索的关键字里面
    $scope.loadkeywords=function(){
        $scope.searchMap.keywords=  $location.search()['keywords'];
        // alert( $location.search()['keywords'])
        $scope.search();
    }

});