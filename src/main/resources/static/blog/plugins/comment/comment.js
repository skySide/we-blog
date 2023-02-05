$('#commentSubmit').click(function () {
    var id = $('#id').val();
    alert(id);
    var verifyCode = $('#verifyCode').val();
    var commentator = $('#commentator').val();
    var commentBody = $('#commentBody').val();
    if (isNull(id)) {
        alert(id + ", " + verifyCode + ", " + commentator + ", " + commentBody);
        swal("参数异常", {
            icon: "warning",
        });
        return;
    }
    if (isNull(commentator)) {
        swal("请输入你的称呼", {
            icon: "warning",
        });
        return;
    }
    if (isNull(verifyCode)) {
        swal("请输入验证码", {
            icon: "warning",
        });
        return;
    }
    if (!validCN_ENString2_100(commentator)) {
        swal("请输入符合规范的名称(不要输入特殊字符)", {
            icon: "warning",
        });
        return;
    }
    if (!validCN_ENString2_100(commentBody)) {
        swal("请输入符合规范的评论内容(不要输入特殊字符)", {
            icon: "warning",
        });
        return;
    }
    var data = {
        "id": id, "verifyCode": verifyCode, "commentator": commentator,
        "commentBody": commentBody
    };
    console.log(data);
    $.ajax({
        type: 'POST',//方法类型
        url: '/blog/comment',
        data: data,
        success: function (result) {
            if (result.code == 200) {
                swal("评论提交成功请等待博主审核", {
                    icon: "success",
                });
                $('#commentBody').val('');
                $('#verifyCode').val('');
            }
            else {
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