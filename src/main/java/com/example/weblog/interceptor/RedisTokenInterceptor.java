package com.example.weblog.interceptor;

import cn.hutool.core.bean.BeanUtil;
import com.example.weblog.dto.UserDTO;
import com.example.weblog.util.RedisConstants;
import com.example.weblog.util.UserHolder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.TimeUnit;

//刷新token的过期时间
public class RedisTokenInterceptor implements HandlerInterceptor {

    public StringRedisTemplate stringRedisTemplate;
    public RedisTokenInterceptor(StringRedisTemplate stringRedisTemplate){
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("authorization");
        System.out.println("redisTokenInterceptor token = " + token);
        if(token == null || token.length() <= 0){
            //没有登录
            return true;
        }
        String key = RedisConstants.LOGIN_TOKEN + token;
        Map<Object, Object> map = stringRedisTemplate.opsForHash().entries(key);
        if(map.isEmpty()){
            //key已经过期,放行，然后被LoginInterceptor拦截，重新进行登录操作
            return true;
        }
        UserDTO userDTO = BeanUtil.fillBeanWithMap(map, new UserDTO(), false);
        UserHolder.setUser(userDTO);
        //刷新key的过期时间
        stringRedisTemplate.expire(key,30, TimeUnit.MINUTES);
        return true;
    }
}
