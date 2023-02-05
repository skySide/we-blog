package com.example.weblog.controller;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.ShearCaptcha;
import com.example.weblog.util.RedisConstants;
import com.example.weblog.util.SystemConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 用于生成验证码，并且保存到redis中，用于提交登录表单
 * 数据之后，从redis中取出数据
 */
@Controller
public class CommonController {
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @GetMapping("/common/kaptcha")
    public void defaultKaptcha(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
        httpServletResponse.setHeader("Cache-Control", "no-store");
        httpServletResponse.setHeader("Pragma", "no-cache");
        httpServletResponse.setDateHeader("Expires", 0);
        httpServletResponse.setContentType("image/png");

        ShearCaptcha shearCaptcha= CaptchaUtil.createShearCaptcha(150, 30, 4, 2);
        String verifyCode = shearCaptcha.getCode();
        /*
        验证码存入session中,不保存到redis中，因为key不可以保证唯一性
        此时在并发的情况下，就会导致验证码覆盖
        */
        httpServletRequest.getSession().setAttribute(SystemConstants.LOGIN_VERIFY_CODE, verifyCode);

        // 输出图片流
        shearCaptcha.write(httpServletResponse.getOutputStream());
    }
}
