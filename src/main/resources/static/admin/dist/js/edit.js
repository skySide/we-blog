var blogEditor;
// Tags Input
$('#blogTags').tagsInput({
    width: '100%',
    height: '38px',
    defaultText: '点击Enter输入新标签'
});

//Initialize Select2 Elements
$('.select2').select2()
$(function () {
    blogEditor = editormd("blog-editormd", {
        width: "100%",
        height: 640,
        syncScrolling: "single",
        path: "/admin/plugins/editormd/lib/",
        toolbarModes: 'full',
        /**图片上传配置*/
        imageUpload: true,
        imageFormats: ["jpg", "jpeg", "gif", "png", "bmp", "webp"], //图片上传格式
        imageUploadURL: "/admin/blogs/md/uploadfile",
        onload: function (obj) { //上传成功之后的回调
        }
    });

    // 编辑器粘贴上传
    document.getElementById("blog-editormd").addEventListener("paste", function (e) {
        var clipboardData = e.clipboardData;
        if (clipboardData) {
            var items = clipboardData.items;
            if (items && items.length > 0) {
                for (var item of items) {
                    if (item.type.startsWith("image/")) {
                        var file = item.getAsFile();
                        if (!file) {
                            alert("请上传有效文件");
                            return;
                        }
                        var formData = new FormData();
                        formData.append('file', file);
                        var xhr = new XMLHttpRequest();
                        xhr.open("POST", "/admin/upload/file");
                        xhr.onreadystatechange = function () {
                            if (xhr.readyState == 4 && xhr.status == 200) {
                                var json=JSON.parse(xhr.responseText);
                                if (json.resultCode == 200) {
                                    blogEditor.insertValue("![](" + json.data + ")");
                                } else {
                                    alert("上传失败");
                                }
                            }
                        }
                        xhr.send(formData);
                    }
                }
            }
        }
    });
});

$('#confirmButton').click(function () {
    var title = $('#title').val();
    var blogTags = $('#blogTags').val();
    var categoryId = $('#blogCategoryId').val();
    var content = blogEditor.getMarkdown();
    if (isNull(title)) {
        swal("请输入文章标题", {
            icon: "error",
        });
        return;
    }
    if (!validLength(title, 150)) {
        swal("标题过长", {
            icon: "error",
        });
        return;
    }

    if (isNull(categoryId)) {
        swal("请选择文章分类", {
            icon: "error",
        });
        return;
    }
    if (isNull(blogTags)) {
        swal("请输入文章标签", {
            icon: "error",
        });
        return;
    }
    if (!validLength(blogTags, 150)) {
        swal("标签过长", {
            icon: "error",
        });
        return;
    }
    if (isNull(content)) {
        swal("请输入文章内容", {
            icon: "error",
        });
        return;
    }
    if (!validLength(blogTags, 100000)) {
        swal("文章内容过长", {
            icon: "error",
        });
        return;
    }
    $('#articleModal').modal('show');
});

$('#saveButton').click(function () {
    var id = $('#id').val();
    var title = $('#title').val();
    var tags = $('#blogTags').val();
    var categoryId = $('#blogCategoryId').val();
    var content = blogEditor.getMarkdown();
    var description = $('#description').val();
    var blogStatus = $("input[name='blogStatus']:checked").val();
    var enableComment = $("input[name='enableComment']:checked").val();
    if (isNull(description)) {
        swal("描述不可以为空", {
            icon: "error",
        });
        return;
    }
    var url = '/admin/blog/save';
    var swlMessage = '保存成功';
    var data = {
        "title": title, "tags": tags, "content": content, "description": description, "blogStatus": blogStatus,
        "enableComment": enableComment, "categoryId": categoryId
    };
    if (id > 0) {
        url = '/admin/blog/update';
        swlMessage = '修改成功';
        data = {
            "id": id,
            "title": title,
            "tags": tags,
            "content": content,
            "description": description,
            "blogStatus": blogStatus,
            "enableComment": enableComment,
            "categoryId": categoryId
        };
    }
    console.log(data);
    $.ajax({
        type: 'POST',//方法类型
        url: url,
        data: data,
        success: function (result) {
            if (result.code == 200) {
                $('#articleModal').modal('hide');
                swal({
                    title: swlMessage,
                    type: 'success',
                    showCancelButton: false,
                    confirmButtonColor: '#3085d6',
                    confirmButtonText: '返回博客列表',
                    confirmButtonClass: 'btn btn-success',
                    buttonsStyling: false
                }).then(function () {
                    window.location.href = "/admin/blog/";
                })
            }
            else {
                $('#articleModal').modal('hide');
                swal(result.msg, {
                    icon: "error",
                });
            }
            ;
        },
        error: function () {
            swal("操作失败", {
                icon: "error",
            });
        }
    });
});

$('#cancelButton').click(function () {
    window.location.href = "/admin/blog/";
});

