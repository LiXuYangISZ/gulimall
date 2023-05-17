package com.atguigu.gulimall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.ware.feign.MemberFeignService;
import com.atguigu.gulimall.ware.vo.FareAndAddressVo;
import com.atguigu.gulimall.ware.vo.MemberReceiveAddressVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.ware.dao.WareInfoDao;
import com.atguigu.gulimall.ware.entity.WareInfoEntity;
import com.atguigu.gulimall.ware.service.WareInfoService;


@Service("wareInfoService")
public class WareInfoServiceImpl extends ServiceImpl <WareInfoDao, WareInfoEntity> implements WareInfoService {

    @Autowired
    MemberFeignService memberFeignService;

    @Override
    public PageUtils queryPage(Map <String, Object> params) {
        LambdaQueryWrapper <WareInfoEntity> queryWrapper = new LambdaQueryWrapper <>();
        String key = (String) params.get("key");
        if (StringUtils.isNotBlank(key)) {
            queryWrapper.eq(WareInfoEntity::getId, key).or()
                    .like(WareInfoEntity::getName, key).or()
                    .like(WareInfoEntity::getAddress, key).or()
                    .eq(WareInfoEntity::getAreacode, key);
        }
        IPage <WareInfoEntity> page = this.page(
                new Query <WareInfoEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    /**
     * 获取运费信息
     *
     * @param addrId
     * @return
     */
    @Override
    public FareAndAddressVo getFare(Long addrId) {
        // 远程调用会员表，获取用户的收货地址信息。根据仓库和收货地址的距离来计算运费~
        // TODO 可对接京东物流API或者快递100API
        R r = memberFeignService.getReceiveAddressInfo(addrId);
        FareAndAddressVo fareAndAddressVo = new FareAndAddressVo();
        MemberReceiveAddressVo memberReceiveAddress = r.getDataByName("memberReceiveAddress", new TypeReference <MemberReceiveAddressVo>() {
        });
        // 目前用手机号的最后一位作为运费
        String phone = memberReceiveAddress.getPhone();
        if (StringUtils.isNotBlank(phone)) {
            fareAndAddressVo.setFare(new BigDecimal(phone.substring(phone.length() - 1)));
        }else{
            fareAndAddressVo.setFare(new BigDecimal(0));
        }
        fareAndAddressVo.setAddress(memberReceiveAddress);
        return fareAndAddressVo;
    }

}