package com.atguigu.gulimall.product.service.impl;

import com.atguigu.common.constant.product.PublishStatusEnum;
import com.atguigu.common.to.SkuHasStockTo;
import com.atguigu.common.to.SkuReductionTo;
import com.atguigu.common.to.SpuBoundsTo;
import com.atguigu.common.to.SkuEsModel;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.product.entity.*;
import com.atguigu.gulimall.product.feign.CouponFeignService;
import com.atguigu.gulimall.product.feign.SearchFeignService;
import com.atguigu.gulimall.product.feign.WareFeignService;
import com.atguigu.gulimall.product.service.*;
import com.atguigu.gulimall.product.vo.product.*;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.atguigu.gulimall.product.dao.SpuInfoDao;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl <SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Autowired
    SpuInfoDescService spuInfoDescService;

    @Autowired
    SpuImagesService spuImagesService;

    @Autowired
    ProductAttrValueService productAttrValueService;

    @Autowired
    AttrService attrService;

    @Autowired
    SkuInfoService skuInfoService;

    @Autowired
    SkuImagesService skuImagesService;

    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    CouponFeignService couponFeignService;

    @Autowired
    BrandService brandService;

    @Autowired
    CategoryService categoryService;

    @Autowired
    WareFeignService wareFeignService;

    @Autowired
    SearchFeignService searchFeignService;

    @Override
    public PageUtils queryPage(Map <String, Object> params) {
        IPage <SpuInfoEntity> page = this.page(
                new Query <SpuInfoEntity>().getPage(params),
                new QueryWrapper <SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 保存商品信息
     *
     * @param vo
     */
    @Transactional
    @Override
    public void saveSpuInfo(SpuSaveVo vo) {
        // 1.保存Spu基本信息 pms_spu_info
        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(vo, spuInfoEntity);
        this.save(spuInfoEntity);

        // 2.保存Spu的描述图片 pms_spu_info_desc
        SpuInfoDescEntity spuInfoDescEntity = new SpuInfoDescEntity();
        spuInfoDescEntity.setSpuId(spuInfoEntity.getId());
        List <String> decript = vo.getDecript();
        if (decript != null && decript.size() > 0) {
            spuInfoDescEntity.setDecript(String.join(",", decript));
        }
        spuInfoDescService.save(spuInfoDescEntity);

        // 3.保存Spu的图片集 pms_spu_images
        List <String> spuImages = vo.getImages();
        if (spuImages != null && spuImages.size() > 0) {
            List <SpuImagesEntity> spuImagesEntities = spuImages.stream().map(image -> {
                SpuImagesEntity spuImagesEntity = new SpuImagesEntity();
                spuImagesEntity.setSpuId(spuInfoEntity.getId());
                spuImagesEntity.setImgUrl(image);
                spuImagesEntity.setImgName(image.substring(image.lastIndexOf("/") + 1));
                return spuImagesEntity;
            }).collect(Collectors.toList());
            spuImagesService.saveBatch(spuImagesEntities);
        }

        // 4.保存Spu的规格参数 pms_product_attr_value
        List <BaseAttrs> baseAttrs = vo.getBaseAttrs();
        if (baseAttrs != null && baseAttrs.size() > 0) {
            List <ProductAttrValueEntity> productAttrValueEntities = baseAttrs.stream().map(baseAttr -> {
                ProductAttrValueEntity productAttrValueEntity = new ProductAttrValueEntity();
                productAttrValueEntity.setSpuId(spuInfoEntity.getId());
                productAttrValueEntity.setAttrId(baseAttr.getAttrId());
                productAttrValueEntity.setAttrValue(baseAttr.getAttrValues());
                productAttrValueEntity.setQuickShow(baseAttr.getShowDesc());
                AttrEntity attr = attrService.getById(baseAttr.getAttrId());
                productAttrValueEntity.setAttrName(attr.getAttrName());
                return productAttrValueEntity;
            }).collect(Collectors.toList());
            productAttrValueService.saveBatch(productAttrValueEntities);
        }

        // 5.保存Spu的积分信息 gulimall_sms ==> sms_spu_bounds
        Bounds bounds = vo.getBounds();
        SpuBoundsTo spuBoundsTo = new SpuBoundsTo();
        BeanUtils.copyProperties(bounds, spuBoundsTo);
        spuBoundsTo.setSpuId(spuInfoEntity.getId());
        R r = couponFeignService.saveSpuBounds(spuBoundsTo);
        if (!r.getCode().equals(0)) {
            log.error("远程保存spu积分信息失败~");
        }

        // 6.保存当前Spu所有的Sku信息
        List <Skus> skus = vo.getSkus();
        skus.forEach(sku -> {
            // 寻找默认图片
            String defaultImg = "";
            for (Images image : sku.getImages()) {
                if (image.getDefaultImg().equals(1)) {
                    defaultImg = image.getImgUrl();
                }
            }

            // 6.1）Sku的基本信息 pms_sku_info
            SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
            BeanUtils.copyProperties(sku, skuInfoEntity);
            skuInfoEntity.setSpuId(spuInfoEntity.getId());
            skuInfoEntity.setCatalogId(vo.getCatalogId());
            skuInfoEntity.setBrandId(vo.getBrandId());
            skuInfoEntity.setSkuDefaultImg(defaultImg);
            skuInfoEntity.setSaleCount(0L);
            skuInfoService.save(skuInfoEntity);
            Long skuId = skuInfoEntity.getSkuId();

            // 6.2) sku的图片纤细 pms_sku_images
            List <SkuImagesEntity> skuImagesEntities = sku.getImages().stream().
                    filter(image -> StringUtils.isNotEmpty(image.getImgUrl())).
                    map(image -> {
                        SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                        skuImagesEntity.setSkuId(skuId);
                        skuImagesEntity.setImgUrl(image.getImgUrl());
                        skuImagesEntity.setDefaultImg(image.getDefaultImg());
                        return skuImagesEntity;
                    }).collect(Collectors.toList());
            skuImagesService.saveBatch(skuImagesEntities);

            // 6.3) sku的销售属性 pms_sku_sale_attr_value
            List <SkuSaleAttrValueEntity> skuSaleAttrValueEntities = sku.getAttr().stream().map(attr -> {
                SkuSaleAttrValueEntity skuSaleAttrValueEntity = new SkuSaleAttrValueEntity();
                BeanUtils.copyProperties(attr, skuSaleAttrValueEntity);
                skuSaleAttrValueEntity.setSkuId(skuId);
                return skuSaleAttrValueEntity;
            }).collect(Collectors.toList());
            skuSaleAttrValueService.saveBatch(skuSaleAttrValueEntities);

            // 6.4) sku的优惠、满减、会员价格等信息 gulimall_sms ==> sms_sku_full_reduction/sms_sku_ladder/sms_member_price
            SkuReductionTo skuReductionTo = new SkuReductionTo();
            BeanUtils.copyProperties(sku, skuReductionTo);
            skuReductionTo.setSkuId(skuId);
            if (skuReductionTo.getFullPrice().compareTo(BigDecimal.ZERO) > 0 || skuReductionTo.getFullCount().compareTo(0) > 0 || CollectionUtils.isNotEmpty(skuReductionTo.getMemberPrice())) {
                R r1 = couponFeignService.saveSkuReduction(skuReductionTo);
                if (!r1.getCode().equals(0)) {
                    log.error("远程保存Sku优惠、满减、会员价格信息失败~");
                }
            }

        });

    }

    @Override
    public PageUtils queryPageByCondition(Map <String, Object> params) {
        QueryWrapper <SpuInfoEntity> queryWrapper = new QueryWrapper <>();
        String catelogId = (String) params.get("catelogId");
        String status = (String) params.get("status");
        String brandId = (String) params.get("brandId");
        String key = (String) params.get("key");
        if (StringUtils.isNotEmpty(key)) {
            queryWrapper.and(wrapper -> {
                wrapper.eq("id", key).or().like("spu_name", key);
            });
        }
        if (StringUtils.isNotEmpty(catelogId) && !catelogId.equals("0")) {
            log.info("hahhhh：{}", catelogId);
            queryWrapper.eq("catalog_id", catelogId);
        }
        if (StringUtils.isNotEmpty(brandId) && !brandId.equals("0")) {
            log.info("hahhhh：{}", brandId);
            queryWrapper.eq("brand_id", brandId);
        }
        if (StringUtils.isNotEmpty(status)) {
            queryWrapper.eq("publish_status", status);
        }

        IPage <SpuInfoEntity> page = this.page(
                new Query <SpuInfoEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public void up(Long spuId) {
        // 1、查询出所有Sku信息
        List <SkuInfoEntity> skuInfoEntities = skuInfoService.getSkusBySpuId(spuId);

        // 2.4 查询当前sku所有可以被用来检索的规格属性
        List <ProductAttrValueEntity> productAttrValueEntities = productAttrValueService.baseAttrListForSpu(spuId);
        List <Long> attrIds = productAttrValueEntities.stream().map(ProductAttrValueEntity::getAttrId).collect(Collectors.toList());
        List <Long> searchAttrIds = attrService.selectSearchAttrIds(attrIds);
        Set <Long> idSet = new HashSet <>(searchAttrIds);
        List <SkuEsModel.Attr> attrs = productAttrValueEntities.stream().filter(productAttrValueEntity -> {
            return idSet.contains(productAttrValueEntity.getAttrId());
        }).map(productAttrValueEntity -> {
            SkuEsModel.Attr attr = new SkuEsModel.Attr();
            BeanUtils.copyProperties(productAttrValueEntity, attr);
            return attr;
        }).collect(Collectors.toList());

        // 2.5 获取SKU所有的库存情况
        List <Long> skuIds = skuInfoEntities.stream().map(SkuInfoEntity::getSkuId).collect(Collectors.toList());
        Map <Long, Boolean> stockMap = null;
        try {
            R r = wareFeignService.getSkusHasStock(skuIds);
            List <SkuHasStockTo> skuHasStockTos = (List <SkuHasStockTo>) r.get("data");
            stockMap = skuHasStockTos.stream().collect(Collectors.toMap(SkuHasStockTo::getSkuId, SkuHasStockTo::getHasStock));
        } catch (Exception e) {
            log.error("库存服务查询异常:原因{}", e);
        }

        // 2、封装每个sku的信息
        Map <Long, Boolean> finalStockMap = stockMap;
        List <SkuEsModel> skuEsModels = skuInfoEntities.stream().map(skuInfoEntity -> {
            // 2.1 保存基本信息
            SkuEsModel skuEsModel = new SkuEsModel();
            BeanUtils.copyProperties(skuInfoEntity, skuEsModel);
            skuEsModel.setSkuPrice(skuInfoEntity.getPrice());
            skuEsModel.setSkuImg(skuInfoEntity.getSkuDefaultImg());
            skuEsModel.setHotScore(0L);
            // 2.2 保存品牌信息
            BrandEntity brand = brandService.getById(skuInfoEntity.getBrandId());
            skuEsModel.setBrandName(brand.getName());
            skuEsModel.setBrandImg(brand.getLogo());
            // 2.3 分类信息
            CategoryEntity category = categoryService.getById(skuInfoEntity.getSkuId());
            skuEsModel.setCatalogName(category.getName());
            // 2.4 属性信息
            skuEsModel.setAttrs(attrs);
            // 2.5 库存信息
            skuEsModel.setHasStock(finalStockMap == null ? true : finalStockMap.get(skuInfoEntity.getSkuId()));
            return skuEsModel;
        }).collect(Collectors.toList());
        // 3、将skuEsModels发送给es进行保存：gulimall-search
        R r = searchFeignService.productStatusUp(skuEsModels);
        if(r.getCode() == 0){
            // 调用成功，修改当前spu的状态
            baseMapper.updateSpuStatus(spuId, PublishStatusEnum.SPU_UP.getCode());
        }else {
            // TODO 调用失败，重复调用？（接口幂等性：重试机制）
        }

    }

}