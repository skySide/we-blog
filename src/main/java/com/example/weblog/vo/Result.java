package com.example.weblog.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 返回给前端的数据
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Result {
    private Integer code; //表示的是响应码
    private String msg; //消息
    private Object obj; //数据


    public static Result success(){
        return new Result(ResultBean.SUCCESS.getCode(), ResultBean.SUCCESS.getMsg(), null);
    }

    public static Result success(Object obj){
        return new Result(ResultBean.SUCCESS.getCode(), ResultBean.SUCCESS.getMsg(), obj);
    }

    public static Result fail(){
        return new Result(ResultBean.ERROR.getCode(),ResultBean.ERROR.getMsg(), null);
    }


    public static Result fail(ResultBean resultBean, Object obj){
        return new Result(resultBean.getCode(), resultBean.getMsg(), obj);
    }

}
