package com.atguigu.gulimall.member.controller;

import java.util.Arrays;
import java.util.Map;

import com.atguigu.common.exception.BizCodeEnum;
import com.atguigu.gulimall.member.exception.PhoneExistException;
import com.atguigu.gulimall.member.exception.UsernameExistException;
import com.atguigu.gulimall.member.feign.CouponFeignService;
import com.atguigu.gulimall.member.vo.MemberLoginVo;
import com.atguigu.gulimall.member.vo.MemberRegisterVo;
import com.atguigu.gulimall.member.vo.SocialGiteeUserInfo;
import com.atguigu.gulimall.member.vo.SocialWeiBoAuthInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.atguigu.gulimall.member.entity.MemberEntity;
import com.atguigu.gulimall.member.service.MemberService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;



/**
 * 会员
 *
 * @author xuyang.li
 * @email xuyang.li@gmail.com
 * @date 2023-02-27 21:28:27
 */
@RestController
@RequestMapping("member/member")
public class MemberController {
    @Autowired
    private MemberService memberService;

    @Autowired
    CouponFeignService couponFeignService;

    @RequestMapping("/coupons")
    public R test(){
        MemberEntity member = new MemberEntity();
        member.setNickname("张三");
        R r = couponFeignService.memberCoupons();
        return R.ok().put("member",member).put("coupons",r.get("coupons"));
    }

    /**
     * 会员注册
     * @param vo
     * @return
     */
    @PostMapping("/register")
    public R register(@RequestBody MemberRegisterVo vo){
        try {
            memberService.register(vo);
        }catch (PhoneExistException p){
            return R.error(BizCodeEnum.PHONE_UP_EXCEPTION.getCode(),BizCodeEnum.PHONE_UP_EXCEPTION.getMessage());
        }catch (UsernameExistException u){
            return R.error(BizCodeEnum.USER_EXIST_EXCEPTION.getCode(),BizCodeEnum.USER_EXIST_EXCEPTION.getMessage());
        }

        return R.ok();
    }

    /**
     * 会员登录
     * @param vo
     * @return
     */
    @PostMapping("/login")
    public R login(@RequestBody MemberLoginVo vo){
        MemberEntity member = memberService.login(vo);
        if(member == null){
            return R.error(BizCodeEnum.LOGINACCOUNT_PASSWORD_INVALID_EXCEPTION.getCode(),BizCodeEnum.LOGINACCOUNT_PASSWORD_INVALID_EXCEPTION.getMessage());
        }
        return R.ok().setData(member);
    }


    /**
     * 微博登录【社交登录&注册】
     * @param vo
     * @return
     */
    @PostMapping("/oauth2/weibo/login")
    public R weiboLogin(@RequestBody SocialWeiBoAuthInfo vo){
        MemberEntity member = null;
        try {
            member = memberService.weiboLogin(vo);
        } catch (Exception e) {
            return R.error(BizCodeEnum.NETWORY_IS_BUSY.getCode(),BizCodeEnum.NETWORY_IS_BUSY.getMessage());
        }
        if(member == null){
            return R.error(BizCodeEnum.NETWORY_IS_BUSY.getCode(),BizCodeEnum.NETWORY_IS_BUSY.getMessage());
        }
        return R.ok().setData(member);
    }

    /**
     * Gitee【社交登录&注册】
     * @param vo
     * @return
     */
    @PostMapping("/oauth2/giteeLogin/login")
    public R giteeLogin(@RequestBody SocialGiteeUserInfo vo){
        MemberEntity member = memberService.giteeLogin(vo);
        return R.ok().setData(member);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("member:member:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("member:member:info")
    public R info(@PathVariable("id") Long id){
		MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("member:member:save")
    public R save(@RequestBody MemberEntity member){
		memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("member:member:update")
    public R update(@RequestBody MemberEntity member){
		memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("member:member:delete")
    public R delete(@RequestBody Long[] ids){
		memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
