package com.atguigu.gulimall.member.service;

import com.atguigu.gulimall.member.vo.MemberLoginVo;
import com.atguigu.gulimall.member.vo.MemberRegisterVo;
import com.atguigu.gulimall.member.vo.SocialGiteeUserInfo;
import com.atguigu.gulimall.member.vo.SocialWeiBoAuthInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.member.entity.MemberEntity;

import java.util.Map;

/**
 * 会员
 *
 * @author xuyang.li
 * @email xuyang.li@gmail.com
 * @date 2023-02-27 21:28:27
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void register(MemberRegisterVo vo);

    MemberEntity login(MemberLoginVo vo);

    MemberEntity weiboLogin(SocialWeiBoAuthInfo vo) throws Exception;

    MemberEntity giteeLogin(SocialGiteeUserInfo vo);
}

