package com.example.weblog.controller.admin;

import cn.hutool.core.bean.BeanUtil;
import com.example.weblog.dto.UserDTO;
import com.example.weblog.entity.User;
import com.example.weblog.service.IUserService;
import com.example.weblog.util.MD5Utils;
import com.example.weblog.util.SystemConstants;
import com.example.weblog.vo.LoginVo;
import com.example.weblog.vo.Result;
import com.example.weblog.vo.ResultBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class LoginController {
    @Resource
    private IUserService userService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    //前往登录界面
    @GetMapping("/login")
    public String toLogin(){
        return "admin/login";
    }

    /*
     * 执行login操作,因为在login.html中已经进行校验过了，因此不需要在后台
     * 再次进行校验用户名，密码以及验证码的长度等问题，此时我们需要查找是
     * 否存在这个用户名和密码的用户，校验验证码是否一致即可
     *
     * 如果用户不存在，执行注册操作，否则就将用户保存到session中
     */
    @PostMapping("/login")
    @ResponseBody
    public Result doLogin(LoginVo loginVo, HttpSession session){
        String username = loginVo.getUsername();
        String password = loginVo.getPassword();
        String inputVerify = loginVo.getVerifyCode();
        String realVerify = (String)session.getAttribute(SystemConstants.LOGIN_VERIFY_CODE);
        if(!inputVerify.equals(realVerify)){
            return Result.fail(ResultBean.LOGIN_VERIFY_ERROR, null);
        }
        User user = userService.getUserByUsernameAndPassword(username, password);
        if(user == null){
            //用户不存在，执行注册操作
            user = new User();
            String dbPass = MD5Utils.formPassToDbPass(password);
            user.setNickname(username);
            user.setPassword(dbPass);
            userService.save(user);
        }
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        //将用户保存到session，之后就可以从session中取出用户
        session.setAttribute("user", userDTO);
         /*
        //生成token，作为key，然后将当前的登录用户保存到redis中，设置过期时间为30分钟
        //因为是缓存的是对象，所以采用哈希表操作
        Map<String, Object> beanMap = BeanUtil.beanToMap(userDTO, new HashMap<>(),
                CopyOptions.create()
                        .setIgnoreNullValue(true) //是否忽略空值
                        //stringRedisTemplate是操作字符串的，所以需要将Object转成字符串的类型
                        //否则就会发生类型转换异常，因为UserDTO中的某些属性为long，不能直接转成String类型
                        .setFieldValueEditor((String, Object) -> Object.toString()));
        String token = RandomUtil.randomString(20).replaceAll("-","");
        String key = RedisConstants.LOGIN_TOKEN + token;
        stringRedisTemplate.opsForHash().putAll(key,beanMap);
        stringRedisTemplate.expire(key, 30, TimeUnit.MINUTES);*/
        return Result.success();
    }

    @GetMapping("/logout")
    public String logout(HttpSession session){
        session.invalidate();
        return "/admin/login";
    }
}
