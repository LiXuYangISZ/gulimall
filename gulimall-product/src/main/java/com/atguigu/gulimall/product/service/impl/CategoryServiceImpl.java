package com.atguigu.gulimall.product.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.product.service.CategoryBrandRelationService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.atguigu.gulimall.product.dao.CategoryDao;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query <CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List <CategoryEntity> listWithTree() {
        // 1.查询所有分类
        List <CategoryEntity> categoryList = baseMapper.selectList(null);

        // 2.组装成父子树形结构
        List <CategoryEntity> categoryTree = categoryList.stream().filter(categoryEntity ->
                // 2.1）查询所有的一级分类
                categoryEntity.getParentCid().equals(0L)
        ).map((category)->{
            // 2.2）设置所有一级分类的子分类
            category.setChildren(getChildren(category,categoryList));
            return category;
        }).sorted((category1, category2) -> {
            return (category1.getSort() == null ? 0 : category1.getSort()) - (category2.getSort() == null ? 0 : category2.getSort());
        }).collect(Collectors.toList());

        return categoryTree;
    }

    @Override
    public void removeMenuByIds(List <Long> asList) {
        // TODO 检查当前删除的菜单，是否被别的地方引用
        baseMapper.deleteBatchIds(asList);
    }

    /**
     * 找到catelogId的完整路径,如：2,23,225
     * @param catelogId
     * @return
     */
    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List <Long> paths = new ArrayList <>();
        findParentPath(catelogId,paths);
        Collections.reverse(paths);
        return paths.toArray(new Long[paths.size()]);
    }

    @Transactional
    @Override
    public void updateDetails(CategoryEntity category) {
        this.updateById(category);
        if(!StringUtils.isEmpty(category.getName())){
            categoryBrandRelationService.updateCategory(category.getCatId(),category.getName());
            // TODO 更新其他关联表
        }
    }

    @Override
    public List <CategoryEntity> getLevelOneCategorys() {
        LambdaQueryWrapper <CategoryEntity> queryWrapper = new LambdaQueryWrapper <>();
        queryWrapper.eq(CategoryEntity::getParentCid,0);
        List <CategoryEntity> categoryEntities = this.list(queryWrapper);
        return categoryEntities;
    }

    /**
     * 递归查找父类的catelog
     * @param catelogId
     * @param paths
     */
    private void findParentPath(Long catelogId, List<Long> paths) {
        // 手机当前节点id
        paths.add(catelogId);
        CategoryEntity category = this.getById(catelogId);
        // 判断是否有父亲节点
        if(category.getParentCid() != 0){
            // 继续递归查找
            findParentPath(category.getParentCid(),paths);
        }
    }

    /**
     * 递归寻找当前分类对应的子分类
     * @param root 当前分类记录
     * @param all  所有分类记录
     * @return
     */
    private List<CategoryEntity> getChildren(CategoryEntity root,List<CategoryEntity> all){
        List <CategoryEntity> children = all.stream()
                .filter(categoryEntity ->
                        // 1.设置子分类
                        categoryEntity.getParentCid().equals(root.getCatId())
                ).map(categoryEntity -> {
                    categoryEntity.setChildren(getChildren(categoryEntity, all));
                    return categoryEntity;
                }).sorted((category1, category2) -> {
                    // 2.分类的排序
                    return (category1.getSort() == null ? 0 : category1.getSort()) - (category2.getSort() == null ? 0 : category2.getSort());
                }).collect(Collectors.toList());
        return children;
    }

}