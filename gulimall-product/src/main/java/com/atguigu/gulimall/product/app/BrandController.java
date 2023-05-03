package com.atguigu.gulimall.product.app;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;
import com.atguigu.common.valid.AddGroup;
import com.atguigu.common.valid.UpdateGroup;
import com.atguigu.common.valid.UpdateStatusGroup;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.atguigu.gulimall.product.entity.BrandEntity;
import com.atguigu.gulimall.product.service.BrandService;


/**
 * 品牌
 *
 * @author xuyang.li
 * @email xuyang.li@gmail.com
 * @date 2023-01-08 16:30:14
 */
@RestController
@RequestMapping("product/brand")
public class BrandController {
    @Autowired
    private BrandService brandService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("product:brand:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = brandService.queryPage(params);

        return R.ok().put("page", page);
    }

    /**
     * 批量获取品牌信息
     * @param brandIds
     * @return
     */
    @GetMapping("/infos")
    // @Cacheable(value = "brand",key = "#root.methodName")
    public R getBrandInfo(@RequestParam("brandIds") List<Long> brandIds){
        List <BrandEntity> brandEntities = brandService.listByIds(brandIds);
        return R.ok().put("brands", brandEntities);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{brandId}")
    public R info(@PathVariable("brandId") Long brandId){
		BrandEntity brand = brandService.getById(brandId);

        return R.ok().put("brand", brand);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:brand:save")
    public R save(@Validated({AddGroup.class}) @RequestBody BrandEntity brand /**, BindingResult result */){
        // if(result.hasErrors()){
        //     Map <String, String> map = new HashMap <>();
        //     //1.获取校验的错误结果
        //     result.getFieldErrors().forEach(item->{
        //         // FieldError获取到错误提示
        //         String message = item.getDefaultMessage();
        //         // 获取错误的属性的名字
        //         String field = item.getField();
        //         map.put(field,message);
        //     });
        //     return R.error(400,"提交的数据不合法").put("data",map);
        // }else{
        //
        // }
        brandService.save(brand);
        return R.ok();
    }

    /**
     * 更新品牌细节 : 包括brand表 以及 其他包含brand中相关字段的表
     * 举例：brand表中的name更新后，CategoryBrandRelation表中的brandName也要进行更新
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:brand:update")
    public R update(@Validated({UpdateGroup.class}) @RequestBody BrandEntity brand){
		brandService.updateDetails(brand);
        return R.ok();
    }

    /**
     * 修改状态
     */
    @RequestMapping("/update/status")
    //@RequiresPermissions("product:brand:update")
    public R updateStatus(@Validated({UpdateStatusGroup.class}) @RequestBody BrandEntity brand){
        brandService.updateById(brand);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:brand:delete")
    public R delete(@RequestBody Long[] brandIds){
		brandService.removeByIds(Arrays.asList(brandIds));

        return R.ok();
    }

}
