package com.atguigu.gulimall.member.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.member.dao.SocialUserDao;
import com.atguigu.gulimall.member.entity.SocialUserEntity;
import com.atguigu.gulimall.member.service.SocialUserService;


@Service("socialUserService")
public class SocialUserServiceImpl extends ServiceImpl<SocialUserDao, SocialUserEntity> implements SocialUserService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SocialUserEntity> page = this.page(
                new Query<SocialUserEntity>().getPage(params),
                new QueryWrapper<>()
        );

        return new PageUtils(page);
    }

    @Override
    public SocialUserEntity getSocialUserByUid(Integer socialType, String uid) {
        SocialUserEntity socialUser = this.baseMapper.selectOne(new LambdaQueryWrapper <SocialUserEntity>().eq(SocialUserEntity::getSocialType, socialType).eq(SocialUserEntity::getSocialId, uid));
        return socialUser;
    }

}