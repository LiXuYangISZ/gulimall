package com.atguigu.gulimall.member.service.impl;

import com.atguigu.gulimall.member.exception.PhoneExistException;
import com.atguigu.gulimall.member.exception.UsernameExistException;
import com.atguigu.gulimall.member.service.MemberLevelService;
import com.atguigu.gulimall.member.vo.MemberRegisterVo;
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


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Autowired
    MemberLevelService memberLevelService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
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
        member.setUsername(vo.getUserName());
        // 密码加密存储
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        member.setPassword(passwordEncoder.encode(vo.getPassword()));
        member.setMobile(vo.getPhone());
        // 默认头像
        member.setHeader("https://blog-photos-lxy.oss-cn-hangzhou.aliyuncs.com/img/202304271224987.png");
        member.setStatus(1);
        member.setSourceType(0);

        this.baseMapper.insert(member);
    }

    /**
     * 检验手机号是否唯一
     * 为了controller能感知异常，使用异常机制
     * @param phone
     * @throws PhoneExistException
     */
    private void checkPhoneUnique(String phone) throws PhoneExistException{
        Integer count = this.baseMapper.selectCount(new LambdaQueryWrapper <MemberEntity>().eq(MemberEntity::getMobile, phone));
        if(count > 0){
            throw new PhoneExistException();
        }
    }

    /**
     * 检验用户名是否唯一
     * 为了controller能感知异常，使用异常机制
     * @param userName
     * @throws UsernameExistException
     */
    private void checkUsernameUnique(String userName) throws UsernameExistException{
        Integer count = this.baseMapper.selectCount(new LambdaQueryWrapper <MemberEntity>().eq(MemberEntity::getUsername, userName));
        if(count > 0){
            throw new UsernameExistException();
        }
    }

}