package com.atguigu.gulimall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.member.entity.SocialUserEntity;

import java.util.Map;

/**
 * 
 *
 * @author xuyang.li
 * @email xuyang.li@gmail.com
 * @date 2023-05-08 18:31:12
 */
public interface SocialUserService extends IService<SocialUserEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 根据用户uid，获取用户的社交信息
     * @return
     * @param socialType
     * @param uid
     */
    SocialUserEntity getSocialUserByUid(Integer socialType, String uid);
}

