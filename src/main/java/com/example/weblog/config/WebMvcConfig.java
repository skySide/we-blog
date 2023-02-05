package com.example.weblog.config;

import com.example.weblog.interceptor.LoginInterceptor;
import com.example.weblog.interceptor.RedisTokenInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //registry.addInterceptor(new RedisTokenInterceptor(stringRedisTemplate));
        registry.addInterceptor(new LoginInterceptor())
                .excludePathPatterns("/user/login/**")
                .excludePathPatterns("/common/**") //对验证码放行
                //对静态资源放行
                .excludePathPatterns("/admin/plugins/**")
                .excludePathPatterns("/admin/dist/**")
                .excludePathPatterns("/blog/amaze/**")
                .excludePathPatterns("/blog/default/**")
                .excludePathPatterns("/blog/plugins/**")
                .excludePathPatterns("/blog/yummy-jekyll/**")
               // .excludePathPatterns("/blog/page/**")
               // .excludePathPatterns("/blog/page/{currentPage}")
                .excludePathPatterns("/blog/**");
    }
}
