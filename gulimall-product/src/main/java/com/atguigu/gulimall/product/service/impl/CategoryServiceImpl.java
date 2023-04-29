package com.atguigu.gulimall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.product.service.CategoryBrandRelationService;
import com.atguigu.gulimall.product.vo.front.Catelog2Vo;
import com.atguigu.gulimall.product.vo.front.Catelog3Vo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.commons.lang.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.atguigu.gulimall.product.dao.CategoryDao;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl <CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    RedissonClient redisson;

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

    /**
     * 级联更新所有关联的数据
     * @CacheEvict：失效模式
     * 1、同时进行多种缓存操作 @Caching
     * 2、指定删除某个分区下的所有数据 @CacheEvict(value = "category",allEntries = true)
     * 规定：存储同一类型的数据，都可以指定成同一个分区。分区名默认就是缓存的前缀  category:key
     * @param category
     *
     */

    // @Caching(evict = {
    //         @CacheEvict(value = "category",key = "'getLevelOneCategorys'") ,
    //         @CacheEvict(value = "category",key = "'getCatelogJson'")
    // })
    @CacheEvict(value = "category",allEntries = true)
    @Transactional
    @Override
    public void updateDetails(CategoryEntity category) {
        this.updateById(category);
        if (!StringUtils.isEmpty(category.getName())) {
            categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());
            // TODO 更新其他关联表
        }
    }

    /**
     * 1、每一个需要缓存的数据我们都来指定要放到那个名字的缓存。【缓存的分区(按照业务类型分)】
     * 2、 @Cacheable({"category"})
     *      代表当前方法的结果需要缓存，如果缓存中有，方法不用调用。
     *      如果缓存中没有，会调用方法，最后将方法的结果放入缓存
     * 3、默认行为
     *      1）、如果缓存中有，方法不用调用。
     *      2）、key默认自动生成；缓存的名字::SimpleKey [](自主生成的key值)
     *      3）、缓存的value的值。默认使用jdk序列化机制，将序列化后的数据存到redis
     *      4）、默认ttl时间 -1；
     *
     *    自定义：
     *      1）、指定生成的缓存使用的key：  key属性指定，接受一个SpEL
     *             SpEL的详细https://docs.spring.io/spring/docs/5.1.12.RELEASE/spring-framework-reference/integration.html#cache-spel-context
     *      2）、指定缓存的数据的存活时间： 配置文件中修改ttl
     *      3）、将数据保存为json格式:
     *              自定义RedisCacheConfiguration即可
     * 4、Spring-Cache的不足；
     *      1）、读模式：
     *          缓存穿透：查询一个null数据。解决：缓存空数据；ache-null-values=true
     *          缓存击穿：大量并发进来同时查询一个正好过期的数据。解决：加锁；？默认是无加锁的;sync = true（加锁，解决击穿）
     *          缓存雪崩：大量的key同时过期。解决：加随机时间。加上过期时间。：spring.cache.redis.time-to-live=3600000
     *      2）、写模式：（缓存与数据库一致）
     *          1）、读写加锁。
     *          2）、引入Canal，感知到MySQL的更新去更新数据库
     *          3）、读多写多，直接去数据库查询就行
     *    总结：
     *      常规数据（读多写少，即时性，一致性要求不高的数据）；完全可以使用Spring-Cache；写模式（只要缓存的数据有过期时间就足够了）
     *      特殊数据：特殊设计
     *
     *   原理：
     *      CacheManager(RedisCacheManager)->Cache(RedisCache)->Cache负责缓存的读写
     *
     *
     * @return
     */
    @Cacheable(value = {"category"},key = "#root.method.name")//失效模式
    // @CachePut//双写模式
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
     *
     * 获取分类的JSON数据
     * 缺点：多次循环查库造成效率太低
     *
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
     * 获取三级分类JSON【批量查询+Redis缓存+SpringCache】
     * @return
     */
    @Cacheable(value = "category",key = "#root.methodName")
    @Override
    public Map <String, List <Catelog2Vo>> getCatelogJson() {
        RLock lock = redisson.getLock("CatalogJson-lock");
        lock.lock();
        Map<String, List<Catelog2Vo>> dataFromDb;
        try {
            dataFromDb = getCatelogJsonFromDB();
        }finally {
            lock.unlock();
        }
        return dataFromDb;
    }

    /**
     *
     * 获取三级分类JSON【批量查询+Redis缓存】
     *
     * TODO 产生堆外内存溢出：OutOfDirectMemoryError
     * 1）、SpringBoot2.0以后默认使用lettuce作为操作redis的客户端，他使用Netty进行网络通信
     * 2）、lettuce的bug导致对外内存溢出。例：-Xmx300m netty如果没有指定堆外内存，默认使用-Xmx300m
     *     可以通过-Dio.netty.maxDirectMemory只去调大堆外内存
     * 解决方案：不能使用-Dio.netty.maxDirectMemory只去调大堆外内存，因为这样只是延迟了错误的发生。
     *          1）、升级lettuce客户端【上线后来通过日志查看解决】     2）、切换使用jedis  √
     * 补充：lettuce、jedis是操作redis的底层客户端。RedisTemplate是Spring再次封装的产物~
     *
     *
     * 使用缓存后需要解决的问题：
     *  1、空结果缓存：解决缓存穿透
     *  2、设置随机过期时间：解决缓存雪崩
     *  3、加锁【互斥锁/读写锁】：解决缓存击穿
     *
     *
     *
     * @return
     */
    // @Override
    public Map <String, List <Catelog2Vo>> getCatelogJson2() {
        // 给缓存中放JSON字符串，查询拿出的JSON字符串，逆转为可用的对象类型【序列化与反序列化】
        // 1.加入缓存逻辑，缓存中的是JSON字符串
        // JSON 语言的优点：跨平台
        String catelogJSON = stringRedisTemplate.opsForValue().get("catelogJSON");
        // 加上缓存逻辑
        if(StringUtils.isBlank(catelogJSON)){
            // 2.缓存中没有，查询数据库
            Map <String, List <Catelog2Vo>> catelogJsonFromDB = getCatalogJsonFromDbWithRedissonLock();
            return catelogJsonFromDB;
        }
        // 4.转为我们指定的对象
        Map <String, List <Catelog2Vo>> catelogMap = JSON.parseObject(catelogJSON, new TypeReference <Map <String, List <Catelog2Vo>>>() {
        });
        return catelogMap;
    }

    /**
     *
     * 获取数据库中的分类JSON【Redisson互斥锁】
     * 注意：这里使用读写锁也是可以的。但是咱们这里没有读操作，不需要读锁。如果使用读写锁，加的也是写锁。综合考虑还是互斥锁效果好点~
     *      读写锁一般适合与读操作>写操作，读吞吐量要求很高的场景
     * 缓存里面的数据如何和数据库保持一致？
     * 1）、双写模式
     * 2）、失效模式  √
     * @return
     */
    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDbWithRedissonLock() {
        // 锁的名字。锁的粒度，越细越快
        // 锁的粒度：具体缓存的是某个数据，n-号商品：product-11-lock product-12-lock 和product-lock相比，前者的粒度更小，速度会更快~
        RLock lock = redisson.getLock("CatalogJson-lock");
        lock.lock();
        Map<String, List<Catelog2Vo>> dataFromDb;
        try {
            dataFromDb = getCatelogJsonFromDB();
            // 查到的数据再放入缓存，将对象转为JSON放在缓存中
            String json = JSON.toJSONString(dataFromDb);
            stringRedisTemplate.opsForValue().set("catelogJSON",json);
        }finally {
            lock.unlock();
        }
        return dataFromDb;
    }

    /**
     *  获取数据库中的分类JSON【手动实现的分布式锁】
     * @return
     */
    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDbWithRedisLock() {


        //1、占分布式锁。去redis占坑
        String uuid = UUID.randomUUID().toString();
        Boolean lock = stringRedisTemplate.opsForValue().setIfAbsent("lock", uuid, 300, TimeUnit.SECONDS);
        if (lock) {
            System.out.println("获取分布式锁成功...");
            //加锁成功... 执行业务
            //2、设置过期时间，必须和加锁是同步的，原子的
            //redisTemplate.expire("lock",30,TimeUnit.SECONDS);
            Map<String, List<Catelog2Vo>> dataFromDb;
            try {
                dataFromDb = getCatelogJsonFromDB();
                // 查到的数据再放入缓存，将对象转为JSON放在缓存中
                String json = JSON.toJSONString(dataFromDb);
                stringRedisTemplate.opsForValue().set("catelogJSON",json);
            } finally {
                //获取值对比+对比成功删除=原子操作 ==> lua脚本解锁
                String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
                //删除锁
                Long flag = stringRedisTemplate.execute(new DefaultRedisScript <Long>(script, Long.class)
                        , Arrays.asList("lock"), uuid);
            }
           // // 防止执行业务时间超时，释放错其他人的锁
           // String lockValue = redisTemplate.opsForValue().get("lock");
           // // 我自己的锁UUID肯定知道哇
           // if(uuid.equals(lockValue)){
           //     //删除我自己的锁
           //     redisTemplate.delete("lock");
           // }
            return dataFromDb;
        } else {
            //加锁失败...休眠100ms重试。模仿synchronized ()
            System.out.println("获取分布式锁失败...等待重试");
            try {
                Thread.sleep(200);
            } catch (Exception e) {

            }
            //自旋重试
            return getCatalogJsonFromDbWithRedisLock();
        }
    }

    /**
     * 获取数据库中的分类JSON【本地锁】
     * @return
     */
    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDbWithLocalLock() {
//        //1、如果缓存中有就用缓存的
//        Map<String, List<Catelog2Vo>> catalogJson = (Map<String, List<Catelog2Vo>>) cache.get("catalogJson");
//        if(cache.get("catalogJson") == null) {
//            //调用业务  xxxxx
//            //返回数据又放入缓存
//            cache.put("catalogJson",parent_cid);
//        }
//        return catalogJson;
        //只要是同一把锁，就能锁住需要这个锁的所有线程
        //1、synchronized (this)：SpringBoot所有的组件在容器中都是单例的。
        //TODO 本地锁：synchronized，JUC（Lock），在分布式情况下，想要锁住所有，必须使用分布式锁

        synchronized (this) {
            //得到锁以后，我们应该再去缓存中确定一次，如果没有才需要继续查询
            return getCatelogJsonFromDB();
        }


    }

    /**
     * 获取数据库中的分类JSON【无锁】
     * @return
     */
    public Map <String, List <Catelog2Vo>> getCatelogJsonFromDB() {
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