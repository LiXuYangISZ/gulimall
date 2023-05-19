package com.atguigu.gulimall.member.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.constant.member.GenderEnum;
import com.atguigu.common.constant.member.MemberStateEnum;
import com.atguigu.common.constant.member.SocialTypeEnum;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.member.entity.SocialUserEntity;
import com.atguigu.gulimall.member.exception.PhoneExistException;
import com.atguigu.gulimall.member.exception.UsernameExistException;
import com.atguigu.gulimall.member.feign.AuthServerFeignService;
import com.atguigu.gulimall.member.service.MemberLevelService;
import com.atguigu.gulimall.member.service.SocialUserService;
import com.atguigu.gulimall.member.vo.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.member.dao.MemberDao;
import com.atguigu.gulimall.member.entity.MemberEntity;
import com.atguigu.gulimall.member.service.MemberService;
import org.springframework.transaction.annotation.Transactional;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl <MemberDao, MemberEntity> implements MemberService {

    @Autowired
    MemberLevelService memberLevelService;

    @Autowired
    SocialUserService socialUserService;

    @Autowired
    AuthServerFeignService authServerFeignService;

    @Override
    public PageUtils queryPage(Map <String, Object> params) {
        IPage <MemberEntity> page = this.page(
                new Query <MemberEntity>().getPage(params),
                new QueryWrapper <MemberEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void register(MemberRegisterVo vo) {
        MemberEntity member = new MemberEntity();
        // 检查用户名和手机号的唯一性.
        checkUsernameUnique(vo.getUserName());
        checkPhoneUnique(vo.getPhone());
        // 设置默认等级
        Long levelId = memberLevelService.getDefaultLevel();
        member.setLevelId(levelId);
        member.setNickname(vo.getUserName());
        // 密码加密存储
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        member.setPassword(passwordEncoder.encode(vo.getPassword()));
        member.setMobile(vo.getPhone());
        // 默认头像
        member.setHeader("https://blog-photos-lxy.oss-cn-hangzhou.aliyuncs.com/img/202304271224987.png");
        member.setStatus(MemberStateEnum.IN_USE.getCode());
        member.setSourceType(SocialTypeEnum.TYPE_NONE.getCode());
        member.setGender(GenderEnum.UNKNOW.getCode());
        this.baseMapper.insert(member);
    }

    @Override
    public MemberEntity login(MemberLoginVo vo) {
        String loginAccount = vo.getLoginAccount();
        String password = vo.getPassword();
        // 使用手机号、用户名、邮箱登录都可以
        MemberEntity member = this.baseMapper.selectOne(new LambdaQueryWrapper <MemberEntity>().eq(MemberEntity::getMobile, loginAccount).or().eq(MemberEntity::getNickname, loginAccount).or().eq(MemberEntity::getEmail, loginAccount));
        if (member != null) {
            String passwordDB = member.getPassword();
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            boolean flag = passwordEncoder.matches(password, passwordDB);
            if (flag) {
                return member;
            } else {
                return null;
            }
        }
        return null;
    }

    /**
     * 微博登录【登录和注册合并逻辑】
     *
     * @param socialWeiBoUser
     * @return
     * @throws Exception
     */
    @Transactional
    @Override
    public MemberEntity weiboLogin(SocialWeiBoAuthInfo socialWeiBoUser) throws Exception {
        String uid = socialWeiBoUser.getUid();
        SocialUserEntity socialUser = socialUserService.getSocialUserByUid(SocialTypeEnum.TYPE_WEIBO.getCode(), uid);
        if (socialUser == null) {
            // 进入注册流程
            R r = authServerFeignService.getWeiBoUserInfo(socialWeiBoUser);
            if (r.getCode() == 0) {
                SocialWeiBoUserInfo weiBoUserInfo = r.getData(new TypeReference <SocialWeiBoUserInfo>() {
                });
                // 保存member信息
                MemberEntity member = new MemberEntity();
                member.setNickname(weiBoUserInfo.getName());
                member.setSourceType(SocialTypeEnum.TYPE_WEIBO.getCode());
                member.setCity(weiBoUserInfo.getLocation());
                member.setGender(weiBoUserInfo.getGender().equals("f") ? GenderEnum.FEMALE.getCode() : (weiBoUserInfo.getGender().equals("m") ? GenderEnum.MALE.getCode() : GenderEnum.UNKNOW.getCode()));
                member.setHeader(weiBoUserInfo.getAvatar_hd());
                member.setSign(weiBoUserInfo.getDescription());
                member.setStatus(MemberStateEnum.IN_USE.getCode());
                member.setLevelId(memberLevelService.getDefaultLevel());
                // TODO 初始化密码(0-9，之后可以自行修改),当第一次用密码登录的时候，需要提醒重新设置
                // member.setPassword("0123456789");
                this.save(member);
                // 保存social_use信息
                socialUser = new SocialUserEntity();
                socialUser.setMemberId(member.getId());
                socialUser.setAccessToken(socialWeiBoUser.getAccess_token());
                socialUser.setExpiresIn(socialWeiBoUser.getExpires_in());
                socialUser.setSocialType(SocialTypeEnum.TYPE_WEIBO.getCode());
                socialUser.setSocialId(socialWeiBoUser.getUid());
                socialUserService.save(socialUser);
                return member;
            } else {
                // 远程调用失败~ 【网络原因】不用管，让前台重新登录就行
                return null;
            }
        } else {
            // 进入登录流程
            // TODO 更新下social_user表,其实我也不知道，把token和过期时间保存在库中啥意思。感觉应该是放到Redis中，当过期后，重新获取；用户退出后，移除~
            // 先按照老师的思路吧
            SocialUserEntity newSocialUser = new SocialUserEntity();
            newSocialUser.setId(socialUser.getId());
            newSocialUser.setAccessToken(socialWeiBoUser.getAccess_token());
            newSocialUser.setExpiresIn(socialWeiBoUser.getExpires_in());
            socialUserService.updateById(newSocialUser);
            MemberEntity member = this.getById(socialUser.getMemberId());
            //返回用户信息，供前台展示
            return member;
        }
    }

    /**
     * Gitee登录【登录和注册合并逻辑】
     *
     * @param giteeUserInfo
     * @return
     */
    @Override
    public MemberEntity giteeLogin(SocialGiteeUserInfo giteeUserInfo) {
        String uid = giteeUserInfo.getId().toString();
        SocialUserEntity socialUser = socialUserService.getSocialUserByUid(SocialTypeEnum.TYPE_GITEE.getCode(), uid);
        if (socialUser == null) {
            // 进入注册流程
            // 保存member信息
            MemberEntity member = new MemberEntity();
            member.setNickname(giteeUserInfo.getLogin());
            member.setUsername(giteeUserInfo.getName());
            member.setSourceType(SocialTypeEnum.TYPE_GITEE.getCode());
            member.setGender(GenderEnum.UNKNOW.getCode());
            member.setHeader(giteeUserInfo.getAvatar_url());
            member.setSign(giteeUserInfo.getBio());
            member.setStatus(MemberStateEnum.IN_USE.getCode());
            member.setLevelId(memberLevelService.getDefaultLevel());
            // TODO 初始化密码(0-9，之后可以自行修改),当第一次用密码登录的时候，需要提醒重新设置
            // member.setPassword("0123456789");
            this.save(member);
            // 保存social_use信息
            socialUser = new SocialUserEntity();
            socialUser.setMemberId(member.getId());
            socialUser.setSocialType(SocialTypeEnum.TYPE_GITEE.getCode());
            socialUser.setSocialId(giteeUserInfo.getId().toString());
            socialUserService.save(socialUser);
            return member;
        } else {
            // 进入登录流程
            // TODO 更新下social_user表,其实我也不知道，把token和过期时间保存在库中啥意思。感觉应该是放到Redis中，当过期后，重新获取；用户退出后，移除~
            // 咱们没有传送access_token，直接啥也不做吧~
            // SocialUserEntity newSocialUser = new SocialUserEntity();
            // newSocialUser.setId(socialUser.getId());
            // newSocialUser.setAccessToken(socialWeiBoUser.getAccess_token());
            // newSocialUser.setExpiresIn(socialWeiBoUser.getExpires_in());
            // socialUserService.updateById(newSocialUser);
            MemberEntity member = this.getById(socialUser.getMemberId());
            //返回用户信息，供前台展示
            return member;
        }
    }

    /**
     * 检验手机号是否唯一
     * 为了controller能感知异常，使用异常机制
     *
     * @param phone
     * @throws PhoneExistException
     */
    private void checkPhoneUnique(String phone) throws PhoneExistException {
        Integer count = this.baseMapper.selectCount(new LambdaQueryWrapper <MemberEntity>().eq(MemberEntity::getMobile, phone));
        if (count > 0) {
            throw new PhoneExistException();
        }
    }

    /**
     * 检验用户名是否唯一
     * 为了controller能感知异常，使用异常机制
     *
     * @param userName
     * @throws UsernameExistException
     */
    private void checkUsernameUnique(String userName) throws UsernameExistException {
        Integer count = this.baseMapper.selectCount(new LambdaQueryWrapper <MemberEntity>().eq(MemberEntity::getNickname, userName));
        if (count > 0) {
            throw new UsernameExistException();
        }
    }

}