app.service("loginService",function ($http) {
    this.showName=function () {
        return $http.get('../name.do');
    }
})