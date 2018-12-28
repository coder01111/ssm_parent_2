app.service('indexService',function($http){

    this.username=function(){
        return $http.get('../login/username.do');
    }
});
