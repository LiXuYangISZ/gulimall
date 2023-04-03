package com.atguigu.gulimall.product.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.atguigu.gulimall.product.entity.AttrEntity;
import com.atguigu.gulimall.product.service.AttrAttrgroupRelationService;
import com.atguigu.gulimall.product.service.AttrService;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.gulimall.product.vo.AttrGroupWithAttrsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.atguigu.gulimall.product.entity.AttrGroupEntity;
import com.atguigu.gulimall.product.service.AttrGroupService;


/**
 * 属性分组
 *
 * @author xuyang.li
 * @email xuyang.li@gmail.com
 * @date 2023-01-08 16:30:14
 */
@RestController
@RequestMapping("product/attrgroup")
public class AttrGroupController {
    @Autowired
    private AttrGroupService attrGroupService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    AttrService attrService;

    @Autowired
    AttrAttrgroupRelationService attrAttrgroupRelationService;

    /**
     * 添加属性与分组关联关系
     * @param attrgroupRelationEntities
     * @return
     */
    @PostMapping("/attr/relation")
    public R addAttrRelation(@RequestBody AttrAttrgroupRelationEntity[] attrgroupRelationEntities){
        attrAttrgroupRelationService.saveBatchAttrRelation(Arrays.asList(attrgroupRelationEntities));
        return R.ok();
    }

    /**
     * 获取分类下所有分组&关联属性
     * @param catelogId
     * @return
     */
    @GetMapping("/{catelogId}/withattr")
    public R getAttrGroupWithAttrs(@PathVariable("catelogId") Long catelogId){
        List<AttrGroupWithAttrsVo> attrGroupWithAttrsVos = attrGroupService.getAttrGroupWithAttrsByCatelogId(catelogId);
        return R.ok().put("data",attrGroupWithAttrsVos);
    }

    /**
     * 获取属性分组的关联的所有属性
     *
     * @param attrGroupId
     * @return
     */
    @GetMapping("/{attrgroupId}/attr/relation")
    public R attrRelation(@PathVariable("attrgroupId") Long attrGroupId) {
        List <AttrEntity> attrEntityList = attrService.getRelationAttr(attrGroupId);
        return R.ok().put("data", attrEntityList);
    }

    /**
     * 获取属性分组没有关联的其他属性
     * @param attrGroupId
     * @return
     */
    @GetMapping("/{attrgroupId}/noattr/relation")
    public R attrNoRelation(@PathVariable("attrgroupId") Long attrGroupId,@RequestParam Map <String, Object> params) {
        PageUtils page = attrService.getNoRelationAttr(params,attrGroupId);
        return R.ok().put("page", page);
    }

    /**
     * 获取三级分类对应的属性组列表
     */
    @RequestMapping("/list/{catelogId}")
    //@RequiresPermissions("product:attrgroup:list")
    public R list(@RequestParam Map <String, Object> params, @PathVariable("catelogId") Long catelogId) {
        // PageUtils page = attrGroupService.queryPage(params);
        PageUtils page = attrGroupService.queryPage(params, catelogId);
        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrGroupId}")
    //@RequiresPermissions("product:attrgroup:info")
    public R info(@PathVariable("attrGroupId") Long attrGroupId) {
        AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);
        Long catelogId = attrGroup.getCatelogId();
        // 根据三级分类id获取完整的分类路径
        Long[] path = categoryService.findCatelogPath(catelogId);
        attrGroup.setCatelogPath(path);
        return R.ok().put("attrGroup", attrGroup);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:attrgroup:save")
    public R save(@RequestBody AttrGroupEntity attrGroup) {
        attrGroupService.save(attrGroup);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:attrgroup:update")
    public R update(@RequestBody AttrGroupEntity attrGroup) {
        attrGroupService.updateById(attrGroup);

        return R.ok();
    }

    /**
     * 删除[删除分组信息+中间表信息]
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:attrgroup:delete")
    public R delete(@RequestBody Long[] attrGroupIds) {
        attrGroupService.removeGroup(Arrays.asList(attrGroupIds));

        return R.ok();
    }

    /**
     * 删除属性与分组的关联关系
     * @param attrAttrgroupRelationEntity
     * @return
     */
    @PostMapping("/attr/relation/delete")
    public R deleteRelation(@RequestBody AttrAttrgroupRelationEntity[] attrAttrgroupRelationEntity) {
        attrAttrgroupRelationService.removeRelation(Arrays.asList(attrAttrgroupRelationEntity));
        return R.ok();
    }

}
