package com.atguigu.gulimall.product.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.product.entity.ProductAttrValueEntity;
import com.atguigu.gulimall.product.service.ProductAttrValueService;
import com.atguigu.gulimall.product.vo.AttrRespVo;
import com.atguigu.gulimall.product.vo.AttrVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.atguigu.gulimall.product.service.AttrService;


/**
 * 商品属性
 *
 * @author xuyang.li
 * @email xuyang.li@gmail.com
 * @date 2023-01-08 16:30:14
 */
@RestController
@RequestMapping("product/attr")
public class AttrController {
    @Autowired
    AttrService attrService;

    @Autowired
    ProductAttrValueService productAttrValueService;


    /**
     * 获取规格参数列表：/product/attr/base/list/{catelogId}
     * 获取分类销售属性：/product/attr/sale/list/{catelogId}
     * 注意：在电商业务中，一般很少进行多表连接查询，因为电商业务数据量很大
     * 以该接口为例，规格表：100W，分类表：1000条，连接查询时相当于作笛卡尔积运算，将会产生10亿条数据~
     * @param params
     * @param catelogId
     * @return
     */
    @GetMapping("/{attrType}/list/{catelogId}")
    public R baseAttrList(@RequestParam Map<String, Object> params,
                          @PathVariable("catelogId") Long catelogId,
                          @PathVariable("attrType") String attrType){
        PageUtils page = attrService.queryBaseAttrPage(params,catelogId,attrType);
        return R.ok().put("page",page);
    }

    /**
     *
     * 获取spu规格
     * @param spuId
     * @return
     */
    @GetMapping("/base/listforspu/{spuId}")
    public R baseAttrListForSpu(@PathVariable("spuId") Long spuId){
        List <ProductAttrValueEntity> productAttrValueEntities = productAttrValueService.list(new LambdaQueryWrapper <ProductAttrValueEntity>().eq(ProductAttrValueEntity::getSpuId, spuId));
        return R.ok().put("data", productAttrValueEntities);
    }

    /**
     * 修改商品规格
     * @param spuId
     * @param productAttrValueEntities
     * @return
     */
    @PostMapping("/update/{spuId}")
    public R updateSpuAttr(@PathVariable("spuId") Long spuId,@RequestBody List<ProductAttrValueEntity> productAttrValueEntities){
        productAttrValueService.updateSpuAttr(spuId,productAttrValueEntities);
        return R.ok();
    }

    /**
     * 分页获取属性列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("product:attr:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = attrService.queryPage(params);
        return R.ok().put("page", page);
    }


    /**
     * 查询属性详情
     */
    @GetMapping("/info/{attrId}")
    //@RequiresPermissions("product:attr:info")
    public R info(@PathVariable("attrId") Long attrId){
		AttrRespVo attrRespVo = attrService.getAttrInfo(attrId);

        return R.ok().put("attr", attrRespVo);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:attr:save")
    public R save(@RequestBody AttrVo attr){
		attrService.saveAttr(attr);

        return R.ok();
    }

    /**
     * 更新属性
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:attr:update")
    public R update(@RequestBody AttrVo attr){
		attrService.updateAttr(attr);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:attr:delete")
    public R delete(@RequestBody Long[] attrIds){
		attrService.removeAttr(Arrays.asList(attrIds));

        return R.ok();
    }

}
