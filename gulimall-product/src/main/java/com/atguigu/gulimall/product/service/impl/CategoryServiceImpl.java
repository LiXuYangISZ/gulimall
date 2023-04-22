package com.atguigu.gulimall.product.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.product.service.CategoryBrandRelationService;
import com.atguigu.gulimall.product.vo.front.Catelog2Vo;
import com.atguigu.gulimall.product.vo.front.Catelog3Vo;
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
public class CategoryServiceImpl extends ServiceImpl <CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

    @Override
    public PageUtils queryPage(Map <String, Object> params) {
        IPage <CategoryEntity> page = this.page(
                new Query <CategoryEntity>().getPage(params),
                new QueryWrapper <CategoryEntity>()
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
        ).map((category) -> {
            // 2.2）设置所有一级分类的子分类
            category.setChildren(getChildren(category, categoryList));
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
     *
     * @param catelogId
     * @return
     */
    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List <Long> paths = new ArrayList <>();
        findParentPath(catelogId, paths);
        Collections.reverse(paths);
        return paths.toArray(new Long[paths.size()]);
    }

    @Transactional
    @Override
    public void updateDetails(CategoryEntity category) {
        this.updateById(category);
        if (!StringUtils.isEmpty(category.getName())) {
            categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());
            // TODO 更新其他关联表
        }
    }

    @Override
    public List <CategoryEntity> getLevelOneCategorys() {
        long l = System.currentTimeMillis();
        LambdaQueryWrapper <CategoryEntity> queryWrapper = new LambdaQueryWrapper <>();
        queryWrapper.eq(CategoryEntity::getParentCid, 0);
        List <CategoryEntity> categoryEntities = this.list(queryWrapper);
        System.out.println("消耗时间:"+(System.currentTimeMillis()-l));
        return categoryEntities;
    }

    /**
     * 小窍门，如何知道这里使用Stream还是For循环呢，就看是否需要返回值&结果是否需要处理
     * 再进行Stream操作时，我们没有进行判断是否为空，原因在于我们使用了MP封装的，如果找不到则为一个空集合
     * TODO 下面这个方法的实现嵌套层数过多、嵌套中查库。可以进行优化：先批量把所有的数据查出，存到Map中，然后进行封装~【 具体可参考谷粒学院的分类下拉列表功能】
     */
    // @Override
    // public Map <String, List <Catelog2Vo>> getCatelogJson() {
    //     // 1、获得一级分类
    //     List <CategoryEntity> level1Categorys = getLevelOneCategorys();
    //     Map <String, List <Catelog2Vo>> map = level1Categorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
    //         // 通过一级分类id，获得二级分类id的信息
    //         List <CategoryEntity> level2Categorys = this.baseMapper.selectList(new LambdaQueryWrapper <CategoryEntity>().eq(CategoryEntity::getParentCid, v.getCatId()));
    //
    //         List <Catelog2Vo> catelog2Vos = level2Categorys.stream().map(level2 -> {
    //
    //             // 通过二级分类id，获得三级分类的信息
    //             List <CategoryEntity> level3Categorys = this.baseMapper.selectList(new LambdaQueryWrapper <CategoryEntity>().eq(CategoryEntity::getParentCid, level2.getCatId()));
    //             List <Catelog3Vo> catelog3Vos = level3Categorys.stream().map(level3 -> {
    //                 Catelog3Vo catelog3Vo = new Catelog3Vo(level2.getCatId().toString(), level3.getCatId().toString(), level3.getName());
    //                 return catelog3Vo;
    //             }).collect(Collectors.toList());
    //             Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), level2.getCatId().toString(), level2.getName(), catelog3Vos);
    //             return catelog2Vo;
    //         }).collect(Collectors.toList());
    //         return catelog2Vos;
    //     }));
    //     return map;
    // }

    /**
     * 获得三级分类JSON
     * @return
     */
    @Override
    public Map <String, List <Catelog2Vo>> getCatelogJson() {
        List <CategoryEntity> categoryEntities = this.baseMapper.selectList(null);
        // 1、获得一级分类
        List <CategoryEntity> level1Categorys = getCategorysByParentCid(categoryEntities,0L);
        Map <String, List <Catelog2Vo>> map = level1Categorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            // 通过一级分类id，获得二级分类id的信息
            List <CategoryEntity> level2Categorys = getCategorysByParentCid(categoryEntities,v.getCatId());

            List <Catelog2Vo> catelog2Vos = level2Categorys.stream().map(level2 -> {

                // 通过二级分类id，获得三级分类的信息
                List <CategoryEntity> level3Categorys = getCategorysByParentCid(categoryEntities,level2.getCatId());
                List <Catelog3Vo> catelog3Vos = level3Categorys.stream().map(level3 -> {
                    Catelog3Vo catelog3Vo = new Catelog3Vo(level2.getCatId().toString(), level3.getCatId().toString(), level3.getName());
                    return catelog3Vo;
                }).collect(Collectors.toList());
                Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), level2.getCatId().toString(), level2.getName(), catelog3Vos);
                return catelog2Vo;
            }).collect(Collectors.toList());
            return catelog2Vos;
        }));
        return map;
    }

    /**
     * 根据父分类id获取所有
     * @param categoryEntities
     * @param parentCid
     * @return
     */
    private List <CategoryEntity> getCategorysByParentCid(List <CategoryEntity> categoryEntities,Long parentCid) {
        return categoryEntities.stream().filter(categoryEntity -> categoryEntity.getParentCid().equals(parentCid)).collect(Collectors.toList());
    }

    /**
     * 递归查找父类的catelog
     *
     * @param catelogId
     * @param paths
     */
    private void findParentPath(Long catelogId, List <Long> paths) {
        // 手机当前节点id
        paths.add(catelogId);
        CategoryEntity category = this.getById(catelogId);
        // 判断是否有父亲节点
        if (category.getParentCid() != 0) {
            // 继续递归查找
            findParentPath(category.getParentCid(), paths);
        }
    }

    /**
     * 递归寻找当前分类对应的子分类
     *
     * @param root 当前分类记录
     * @param all  所有分类记录
     * @return
     */
    private List <CategoryEntity> getChildren(CategoryEntity root, List <CategoryEntity> all) {
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