package com.example.weblog.interceptor;

import com.example.weblog.dto.UserDTO;
import com.example.weblog.util.SystemConstants;
import com.example.weblog.util.UserHolder;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LoginInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        UserDTO user = (UserDTO)request.getSession().getAttribute(SystemConstants.LOGIN_USER);
        if(user == null){
            //如果用户为空，那么不能放行
            response.setContentType("text/html;charset=utf-8");
            response.getWriter().println("没有登录");
            return false;
        }
        return true;
    }
}
