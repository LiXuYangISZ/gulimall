package com.atguigu.gulimall.authserver.controller;

import com.atguigu.common.constant.authserver.AuthServerConstant;
import com.atguigu.common.exception.BizCodeEnum;
import com.atguigu.common.utils.R;
import com.atguigu.common.utils.RandomUtil;
import com.atguigu.gulimall.authserver.feign.ThirdPartFeignService;
import com.atguigu.gulimall.authserver.vo.UserRegisterVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author lxy
 * @version 1.0
 * @Description
 * @date 2023/5/5 13:59
 */
@Controller
public class IndexController {

    @Autowired
    ThirdPartFeignService thirdPartFeignService;

    @Autowired
    StringRedisTemplate redisTemplate;

    @ResponseBody
    @GetMapping("/sms/sendCode")
    public R sendCode(@RequestParam("phone") String phone){
        // 1.接口防刷

        // 2.验证码的前校验【为了防止一个用户1min内大量获取验证码】
        String redisCode = redisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone);
        if(StringUtils.isNotBlank(redisCode)){
            Long time = Long.valueOf(redisCode.split("_")[1]);
            if(System.currentTimeMillis() - time < 30000){
                return R.error(BizCodeEnum.SMS_CODE_EXCEPTION.getCode(),BizCodeEnum.SMS_CODE_EXCEPTION.getMessage());
            }
        }
        String code = RandomUtil.getSixBitRandom();
        redisTemplate.opsForValue().set(AuthServerConstant.SMS_CODE_CACHE_PREFIX+phone,code+"_"+System.currentTimeMillis(),AuthServerConstant.SMS_CODE_EXPIRATION_TIME, TimeUnit.MINUTES);
        thirdPartFeignService.sendCode(phone, code,AuthServerConstant.SMS_CODE_EXPIRATION_TIME.toString());
        return R.ok();
    }

    /**
     * // 转发：请求路径不变，重定向，请求路径改变~
     * // RedirectAttributes redirectAttributes：模拟重定向携带数据
     * //重定向携带数据，利用session原理。将数据放在session中。只要跳到下一个页面取出这个数据以后，session里面的数据就会删掉
     * //TODO 分布式下的session问题。
     * @param vo
     * @param result
     * @param model
     * @return
     */
    @PostMapping("/register")
    public String register(@Valid UserRegisterVo vo, BindingResult result,
                           Model model,
                           RedirectAttributes redirectAttributes,
                           HttpSession session){
        // 1、基本信息校验
        if(result.hasErrors()){
            // 校验失败，转发到注册页
            Map <String, String> errors = result.getFieldErrors().stream().collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
            // model.addAttribute("errors",errors);
            //使用转发，报错：Request method 'POST' not supported
            //原因：用户注册->/regist[post]----》转发/reg.html（路径映射默认都是get方式访问的。）
            redirectAttributes.addFlashAttribute("errors",errors);
            return "redirect:http://auth.gulimall.com/register.html";
        }

        // 2、验证码校验
        String code = vo.getCode();
        String redisCode = redisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + vo.getPhone());
        if (StringUtils.isBlank(redisCode) || !code.equals(redisCode.split("_")[0])) {
            Map <String, String> errors = new HashMap <>();
            errors.put("code","验证码错误");
            redirectAttributes.addFlashAttribute("errors",errors);
            return "redirect:http://auth.gulimall.com/register.html";
        }
        // 3、调用远程服务进行注册


        //删除验证码
        redisTemplate.delete(AuthServerConstant.SMS_CODE_CACHE_PREFIX + vo.getPhone());

        // 注册成功跳转到登录页
        return "redirect:/login.html";
    }


}
