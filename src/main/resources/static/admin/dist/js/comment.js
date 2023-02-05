$(function(){
        $("#jqGrid").jqGrid({
            url: '/admin/comment/list',
            method: "GET",
            datatype: "json",
            colModel: [
                {label: 'id', name: 'id', index: 'id', width: 50, key: true, hidden: true},
                {label: '评论人', name: 'nickname', index: 'nickname', width: 60},
                {label: '评论时间', name: 'createTime', index: 'createTime', width: 60, formatter: dateFormatter},
                {label: '评论博客', name: 'blogName', index: 'blogName', width: 60},
                {label: '评论内容', name: 'commentBody', index: 'commentBody', width: 100},
                {label: '评论状态', name: 'commentStatus', index: 'commentStatus', width: 60, formatter: statusFormatter},
            ],
            height: 700,
            rowNum: 10,
            rowList: [10, 20, 50],
            styleUI: 'Bootstrap',
            loadtext: '信息读取中...',
            rownumbers: false,
            rownumWidth: 20,
            autowidth: true,
            multiselect: true,
            pager: "#jqGridPager",
            jsonReader: {
                root: "obj.list",
                page: "obj.currentPage",
                total: "obj.pages",
                records: "obj.total"
            },
            prmNames: {
                page: "currentPage",
                rows: "pageSize",
                order: "order",
            },
            gridComplete: function () {
                //隐藏grid底部滚动条
                $("#jqGrid").closest(".ui-jqgrid-bdiv").css({"overflow-x": "hidden"});
            }
        });
        $(window).resize(function () {
            $("#jqGrid").setGridWidth($(".card-body").width());
        });
        function statusFormatter(cellvalue) {
            if (cellvalue == 0) {
                return "<button type=\"button\" class=\"btn btn-block btn-secondary btn-sm\" style=\"width: 80%;\">待审核</button>";
            }
            else if (cellvalue == 1) {
                return "<button type=\"button\" class=\"btn btn-block btn-success btn-sm\" style=\"width: 80%;\">已审核</button>";
            }
        }

        function dateFormatter(cellvalue){
            var date = new Date(cellvalue);
            var year = date.getFullYear();
            var month = date.getMonth() + 1;
            var day = date.getDate();
            var hours = date.getHours()
            var minutes = date.getMinutes()
            var seconds = date.getSeconds()

            if (month < 10) {
                month = "0" + month;
            }
            if (day < 10) {
                day = "0" + day;
            }
            if (hours < 10) {
                hours = "0" + hours;
            }
            if (minutes < 10) {
                minutes = "0" + minutes;
            }
            if (seconds < 10) {
                seconds = "0" + seconds;
            }
            return year + "-" + month + "-" + day + " " + hours + ":" + minutes + ":" + seconds;
        }

});

/**
 * jqGrid重新加载
 */
function reload() {
    var page = $("#jqGrid").jqGrid('getGridParam', 'page');
    $("#jqGrid").jqGrid('setGridParam', {
        page: page
    }).trigger("reloadGrid");
}

/**
 * 批量审核
 */
function checkDoneComments() {
    var ids = getSelectedRows();
    if (ids == null) {
        return;
    }
    swal({
        title: "确认弹框",
        text: "确认审核通过吗?",
        icon: "warning",
        buttons: true,
        dangerMode: true,
    }).then((flag) => {
            if (flag) {
                $.ajax({
                    type: "POST",
                    url: "/admin/comment/checkDone",
                    contentType: "application/json",
                    data: JSON.stringify(ids),
                    success: function (r) {
                        if (r.code == 200) {
                            swal("审核成功", {
                                icon: "success",
                            });
                            $("#jqGrid").trigger("reloadGrid");
                        } else {
                            swal(r.message, {
                                icon: "error",
                            });
                        }
                    }
                });
            }
        }
    );
}

/**
 * 批量删除
 */
function deleteComments() {
    var ids = getSelectedRows();
    if (ids == null) {
        return;
    }
    swal({
        title: "确认弹框",
        text: "确认删除这些评论吗?",
        icon: "warning",
        buttons: true,
        dangerMode: true,
    }).then((flag) => {
            if (flag) {
                $.ajax({
                    type: "POST",
                    url: "/admin/comment/delete",
                    contentType: "application/json",
                    data: JSON.stringify(ids),
                    success: function (r) {
                        if (r.code == 200) {
                            swal("删除成功", {
                                icon: "success",
                            });
                            $("#jqGrid").trigger("reloadGrid");
                        } else {
                            swal(r.message, {
                                icon: "error",
                            });
                        }
                    }
                });
            }
        }
    );
}


function reply() {
    var id = getSelectedRow();
    if (id == null) {
        return;
    }
    var rowData = $("#jqGrid").jqGrid('getRowData', id);
    console.log(rowData);
    if (rowData.commentStatus.indexOf('待审核') > -1) {
        swal("请先审核该评论再进行回复!", {
            icon: "warning",
        });
        return;
    }
    $("#replyBody").val('');
    $('#replyModal').modal('show');
}

//获取当前登录用户发布的评论
function myGivenComments(){
    //数据封装
    var searchData = {target: "my"};
    //传入查询条件参数
    $("#jqGrid").jqGrid("setGridParam", {postData: searchData});
    //点击搜索按钮默认都从第一页开始
    $("#jqGrid").jqGrid("setGridParam", {page: 1});
    //提交post并刷新表格
    $("#jqGrid").jqGrid("setGridParam", {url: '/admin/comment/list'}).trigger("reloadGrid");
}

//绑定modal上的保存按钮
$('#saveButton').click(function () {
    var replyBody = $("#replyBody").val();
    if (!validCN_ENString2_100(replyBody)) {
        swal("请输入符合规范的回复信息!", {
            icon: "warning",
        });
        return;
    } else {
        var url = '/admin/comment/reply';
        var id = getSelectedRow();
        var params = {"id": id, "replyBody": replyBody}
        $.ajax({
            type: 'POST',//方法类型
            url: url,
            data: params,
            success: function (result) {
                if (result.code == 200) {
                    alert("回复成功");
                    $('#replyModal').modal('hide');
                    swal("回复成功", {
                        icon: "success",
                    });
                    reload();
                }
                else {
                    $('#replyModal').modal('hide');
                    swal(result.msg, {
                        icon: "error",
                    });
                };
            },
            error: function () {
                swal("操作失败", {
                    icon: "error",
                });
            }
        });
    }
});
