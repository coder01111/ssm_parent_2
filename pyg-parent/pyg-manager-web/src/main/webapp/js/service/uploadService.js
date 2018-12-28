app.service("uploadService",function ($http) {
    this.uploadFile=function () {
        //用html5新特性获取表单数据
        var  formData = new FormData();
        //将文件提交的内容追加到这个变量去,固定写法
        formData.append("file",file.files[0]);
        return $http({
            method:'POST',
            url:"../upload.do",
            data:formData,
            //m默认提交是Json格式，要改成multipart/form-data.格式
            headers:{'Content-Type':undefined},
            //将序列化我们的formdata object.
            transformRequest:angular.identity
        });

    }
})