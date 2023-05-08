package com.atguigu.gulimall.member.controller;

import java.util.Arrays;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.atguigu.gulimall.member.entity.SocialUserEntity;
import com.atguigu.gulimall.member.service.SocialUserService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;



/**
 * 
 *
 * @author xuyang.li
 * @email xuyang.li@gmail.com
 * @date 2023-05-08 18:31:12
 */
@RestController
@RequestMapping("member/socialuser")
public class SocialUserController {
    @Autowired
    private SocialUserService socialUserService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("member:socialuser:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = socialUserService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("member:socialuser:info")
    public R info(@PathVariable("id") Long id){
		SocialUserEntity socialUser = socialUserService.getById(id);

        return R.ok().put("socialUser", socialUser);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("member:socialuser:save")
    public R save(@RequestBody SocialUserEntity socialUser){
		socialUserService.save(socialUser);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("member:socialuser:update")
    public R update(@RequestBody SocialUserEntity socialUser){
		socialUserService.updateById(socialUser);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("member:socialuser:delete")
    public R delete(@RequestBody Long[] ids){
		socialUserService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
