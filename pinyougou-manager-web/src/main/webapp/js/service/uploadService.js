app.service("uploadService", function ($http) {
    //文件上传服务
    this.uploadFile = function () {
        var formData = new FormData;
        formData.append("file", file.files[0]); //file.files[0] 获取第一个文件上传框的内容
        return $http({
            method: "post",
           url:"../upload.do",
            data:formData,
            headers:{'Content-Type':undefined},
            transformRequest:angular.identity
        });
    }
});