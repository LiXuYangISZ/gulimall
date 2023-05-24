package com.atguigu.gulimall.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.constant.order.OrderConstant;
import com.atguigu.common.to.MemberTo;
import com.atguigu.common.to.SkuHasStockTo;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.order.entity.OrderItemEntity;
import com.atguigu.gulimall.order.enume.OrderStatusEnum;
import com.atguigu.gulimall.order.exception.NoStockException;
import com.atguigu.gulimall.order.feign.CartFeignService;
import com.atguigu.gulimall.order.feign.MemberFeignService;
import com.atguigu.gulimall.order.feign.ProductFeignService;
import com.atguigu.gulimall.order.feign.WareFeignService;
import com.atguigu.gulimall.order.interceptor.LoginInterceptor;
import com.atguigu.gulimall.order.service.OrderItemService;
import com.atguigu.gulimall.order.vo.*;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.google.common.base.Joiner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.order.dao.OrderDao;
import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.service.OrderService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    /**
     * 把所需的对象放置ThreadLocal中后，就无须每次传参了，只要是同一个线程都可以获取到
     * 【这里只是演示下，其实使用参数也行~~~】
     */
    private ThreadLocal<OrderSubmitVo> confirmVoThreadLocal = new ThreadLocal <>();

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    MemberFeignService memberFeignService;

    @Autowired
    LoginInterceptor loginInterceptor;

    @Autowired
    CartFeignService cartFeignService;

    @Autowired
    WareFeignService wareFeignService;

    @Autowired
    ThreadPoolExecutor executor;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    OrderItemService orderItemService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 订单确认
     *
     * MY NOTES
     *  一、Feign在远程调用之前要构造请求，调用很多的拦截器。 RequestInterceptor interceptor : requestInterceptors。
     *     所以我们可以把Cookie放至拦截器中，让其构造时加上~
     *  二、理论上第一个不给用户id也可以,无非也和第二个一样呗. 从threadLocal中获取~ 老师向我们展示了两种思路
     *
     * @return
     */
    @Override
    public OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException {
        MemberTo member = LoginInterceptor.threadLocal.get();
        OrderConfirmVo orderConfirmVo = new OrderConfirmVo();

        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        /**
         * 异步编排进行优化
         * 存在的问题：使用异步编排后，新线程内部的请求并没有携带Cookie等信息【这些信息是在老线程里面的】，从而导致请求出错【空指针】。而且咱们这些信息都是基于threadLocal的
         * 解决：把老线程里面的用户信息【COOKIE】放到异步编排创建的新线程中
         * RequestContextHolder.setRequestAttributes(attributes);
         * 这个语句在不同的线程中RequestContextHolder所代表的请求也不一样哦~ 底层也是ThreadLocal
         */
        CompletableFuture <Void> memberFuture = CompletableFuture.runAsync(() -> {
            // 1、远程查询用户所有收获地址列表
            // 每一个新线程都来共享之前的请求数据
            RequestContextHolder.setRequestAttributes(attributes);
            List <MemberReceiveAddressVo> address = memberFeignService.getAddress(member.getId());
            orderConfirmVo.setAddress(address);
        }, executor);

        CompletableFuture <Void> cartFuture = CompletableFuture.runAsync(() -> {
            // 2、远程查询用户选中的商品列表
            // 每一个新线程都来共享之前的请求数据
            RequestContextHolder.setRequestAttributes(attributes);
            List <OrderItemVo> cartItems = cartFeignService.currentUserCartItems();
            System.out.println("cartItems:"+cartItems);
            orderConfirmVo.setItems(cartItems);
        }, executor).thenRunAsync(()->{
            // 远程调用库存服务，查询商品库存情况
            List <OrderItemVo> orderItems = orderConfirmVo.getItems();
            // TODO 只有当购物车中有商品才可以去结算哦~
            List <Long> skuIds = orderItems.stream().map(OrderItemVo::getSkuId).collect(Collectors.toList());
            R r = wareFeignService.getSkusHasStock(skuIds);
            if(r.getCode()==0){
                List <SkuHasStockTo> skuHasStock = r.getData(new TypeReference <List <SkuHasStockTo>>() {
                });
                Map <Long, Boolean> stockMap = skuHasStock.stream().collect(Collectors.toMap(SkuHasStockTo::getSkuId, SkuHasStockTo::getHasStock));
                orderConfirmVo.setStocks(stockMap);
            }
        },executor);


        // 3、查询用户积分
        Integer integration = member.getIntegration();
        orderConfirmVo.setIntegration(integration);

        // 4、其他数据自动计算

        CompletableFuture.allOf(memberFuture,cartFuture).get();

        // 5、防重令牌【防止用户在某一时刻不断的请求服务器】
        String token = UUID.randomUUID().toString().replace("-", "");
        orderConfirmVo.setOrderToken(token);
        redisTemplate.opsForValue().set(OrderConstant.USER_ORDER_TOKEN_PREFIX+member.getId(),token);

        return orderConfirmVo;
    }

    /**
     * 提交订单
     *
     *MY NOTES
     * @Transactional：本地事务。在分布式系统中，只能控制自己的回滚，控制不了其他服务的回滚。
     *  分布式事务解决方案（Seata）出现的最大原因：网络原因（真失败还是假失败）+分布式机器
     * 举例：
     *   ① 网络原因。比如 调用锁库存的服务，由于网络原因出现了超时。Order这边得事务就会回滚，但是Ware那边锁库存成功了。
     *     从而就会出现，下单失败，但是库存锁成功了~
     *   ② 分布式机器。比如库存锁成功了，但是下面扣减积分的时候出现了异常。然后Order进行回滚，但是锁定的库存无法进行回滚了。
     *   因为你本地事务是无法控制人家远程服务的事务的。事务本质是在一个连接中的多个操作，而不同服务肯定对应着不同的连接了。
     *
     * @param orderSubmitVo
     * @return
     */
    @Transactional
    @Override
    public SubmitOrderResponseVo submitOrder(OrderSubmitVo orderSubmitVo) {
        confirmVoThreadLocal.set(orderSubmitVo);
        SubmitOrderResponseVo submitOrderResponseVo = new SubmitOrderResponseVo();
        MemberTo member = LoginInterceptor.threadLocal.get();
        String orderToken = orderSubmitVo.getOrderToken();
        // 1、验证令牌【令牌的获取、对比和删除必须保证原子性】
        // 0令牌失败 1 删除成功
        String script="if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        Long result = redisTemplate.execute(new DefaultRedisScript <Long>(script, Long.class), Arrays.asList(OrderConstant.USER_ORDER_TOKEN_PREFIX + member.getId()), orderToken);
        if(result==0L){
            submitOrderResponseVo.setCode(1);
        }else{
            // 1、创建订单
            OrderCreateVo order = createOrder();
            /**
             * 2、验证价格
             * payAmount:后台计算过的实际价格
             * payPrice：前台传递过去的价格
             */
            BigDecimal payAmount = order.getOrder().getPayAmount();
            BigDecimal payPrice = orderSubmitVo.getPayPrice();
            if(Math.abs(payAmount.subtract(payPrice).doubleValue()) >= 0.01){
                // 验价失败
                submitOrderResponseVo.setCode(2);
                return submitOrderResponseVo;
            }
            // 3、保存订单
            saveOrder(order);
            /**
             * 4、远程锁库存【只要有异常就回滚订单数据】
             * 所有订单项（skuId、num）
             */
            WareSkuLockVo wareSkuLockVo = new WareSkuLockVo();
            wareSkuLockVo.setOrderSn(order.getOrder().getOrderSn());
            List <OrderItemVo> orderItemVos = order.getOrderItems().stream().map(orderItem -> {
                OrderItemVo orderItemVo = new OrderItemVo();
                orderItemVo.setSkuId(orderItem.getSkuId());
                orderItemVo.setCount(orderItem.getSkuQuantity().longValue());
                return orderItemVo;
            }).collect(Collectors.toList());
            wareSkuLockVo.setLockItems(orderItemVos);
            // TODO 模拟库存扣减成功了，但是网络原因超时。会出现 订单回滚，库存不滚。
            R r = wareFeignService.orderLockStock(wareSkuLockVo);
            if(r.getCode() == 0){
                // 锁成功
                submitOrderResponseVo.setCode(0);
                submitOrderResponseVo.setOrder(order.getOrder());
                // TODO 模拟远程扣减积分出现异常。通过现象可以看出订单回滚，库存不会滚~
                // int i = 10 / 0;
            }else{
                // MY NOTES 为了保证事务，这里改为抛出异常，不然这里只是一个普通的返回，订单数据依然可以保存成功。
                throw new NoStockException();
                // submitOrderResponseVo.setCode(3);
            }
            return submitOrderResponseVo;
        }
        return submitOrderResponseVo;
    }

    /**
     * 保存订单信息
     * @param orderCreateVo
     */
    private void saveOrder(OrderCreateVo orderCreateVo) {
        OrderEntity order = orderCreateVo.getOrder();
        List <OrderItemEntity> orderItems = orderCreateVo.getOrderItems();
        this.save(order);
        orderItemService.saveBatch(orderItems);
    }

    /**
     * 生成订单
     * @return
     */
    private OrderCreateVo createOrder() {
        OrderCreateVo createVo = new OrderCreateVo();

        // 1、创建Order
        String orderSn = IdWorker.getTimeId();
        OrderEntity orderEntity = buildOrder(orderSn);
        createVo.setOrder(orderEntity);

        // 2、创建购物车中的所有选中订单项
        List <OrderItemEntity> orderItems = buildOrderItems(orderSn);
        createVo.setOrderItems(orderItems);

        // 3、设置价格积分相关
        computePrice(orderEntity,orderItems);
        return createVo;
    }

    /**
     * 设置价格、积分相关信息
     * @param orderEntity
     * @param orderItems
     * @return
     */
    private void computePrice(OrderEntity orderEntity, List <OrderItemEntity> orderItems) {
        BigDecimal totalAmount = new BigDecimal("0.0");
        BigDecimal couponAmount = new BigDecimal("0.0");
        BigDecimal integrationAmount = new BigDecimal("0.0");
        BigDecimal promotionAmount = new BigDecimal("0.0");
        Integer integration = 0;
        Integer growth = 0;

        //叠加每一个订单项的信息
        for (OrderItemEntity entity : orderItems) {
            couponAmount = couponAmount.add(entity.getCouponAmount());
            integrationAmount = integrationAmount.add(entity.getIntegrationAmount());
            promotionAmount = promotionAmount.add(entity.getPromotionAmount());
            totalAmount = totalAmount.add(entity.getRealAmount());
            integration += entity.getGiftIntegration();
            growth += entity.getGiftGrowth();
        }

        // 订单价格相关
        orderEntity.setTotalAmount(totalAmount);
        orderEntity.setPayAmount(totalAmount.add(orderEntity.getFreightAmount()));
        orderEntity.setPromotionAmount(promotionAmount);
        orderEntity.setIntegrationAmount(integrationAmount);
        orderEntity.setCouponAmount(couponAmount);

        //设置积分等信息
        orderEntity.setIntegration(integration);
        orderEntity.setGrowth(growth);

        // 设置删除状态
        orderEntity.setDeleteStatus(0);
    }

    /**
     * 构建所有订单项数据
     * @param orderSn
     * @return
     */
    private List <OrderItemEntity> buildOrderItems(String orderSn) {
        List <OrderItemVo> orderItemVos = cartFeignService.currentUserCartItems();
        if(orderItemVos!=null && orderItemVos.size() > 0){
            return orderItemVos.stream().map(orderItemVo -> {
                OrderItemEntity orderItem = buildOrderItem(orderItemVo);
                orderItem.setOrderSn(orderSn);
                return orderItem;
            }).collect(Collectors.toList());
        }
        return null;
    }

    /**
     * 构建订单项
     * @param orderItemVo
     * @return
     */
    private OrderItemEntity buildOrderItem(OrderItemVo orderItemVo) {
        OrderItemEntity orderItem = new OrderItemEntity();

        // 1、封装SPU信息
        R r = productFeignService.getSpuInfoBySkuId(orderItemVo.getSkuId());
        SpuInfoVo spuInfo = r.getData(new TypeReference <SpuInfoVo>() {
        });
        orderItem.setSpuBrand(spuInfo.getBrandId().toString());
        orderItem.setSpuId(spuInfo.getId());
        orderItem.setSpuName(spuInfo.getSpuName());
        orderItem.setCategoryId(spuInfo.getCatalogId());

        // 2、封装SKU信息
        orderItem.setSkuId(orderItemVo.getSkuId());
        orderItem.setSkuName(orderItemVo.getTitle());
        orderItem.setSkuPic(orderItemVo.getDefaultImage());
        orderItem.setSkuPrice(orderItemVo.getPrice());
        orderItem.setSkuQuantity(orderItemVo.getCount().intValue());
        String attrStr = Joiner.on(";").skipNulls().join(orderItemVo.getAttrs());
        orderItem.setSkuAttrsVals(attrStr);

        // 3、TODO 远程查询优惠信息【暂不用做】

        // 4、积分信息
        orderItem.setGiftGrowth(orderItemVo.getPrice().multiply(new BigDecimal(orderItemVo.getCount())).intValue());
        orderItem.setGiftIntegration(orderItemVo.getPrice().multiply(new BigDecimal(orderItemVo.getCount())).intValue());

        // 5、订单项的价格信息
        // TODO 设置减免信息，实际开发中需要远程查询Coupon库
        orderItem.setPromotionAmount(BigDecimal.ZERO);
        orderItem.setCouponAmount(BigDecimal.ZERO);
        orderItem.setIntegrationAmount(BigDecimal.ZERO);
        BigDecimal originPrice = orderItem.getSkuPrice().multiply(new BigDecimal(orderItem.getSkuQuantity()));
        BigDecimal realPrice = originPrice.subtract(orderItem.getPromotionAmount())
                .subtract(orderItem.getCouponAmount())
                .subtract(orderItem.getIntegrationAmount());
        orderItem.setRealAmount(realPrice);
        return orderItem;
    }

    /**
     * 构建订单信息
     * @return
     */
    private OrderEntity buildOrder(String orderSn) {
        OrderSubmitVo orderSubmitVo = confirmVoThreadLocal.get();
        // 创建订单
        OrderEntity orderEntity = new OrderEntity();
        // 创建订单号
        orderEntity.setOrderSn(orderSn);
        // 获取订单地址信息
        R fare = wareFeignService.getFare(orderSubmitVo.getAddrId());
        FareAndAddressVo fareAndAddressData = fare.getData(new TypeReference <FareAndAddressVo>() {
        });

        // 获取订单运费信息
        orderEntity.setFreightAmount(fareAndAddressData.getFare());

        // 设置收货人信息
        orderEntity.setReceiverCity(fareAndAddressData.getAddress().getCity());
        orderEntity.setReceiverDetailAddress(fareAndAddressData.getAddress().getDetailAddress());
        orderEntity.setReceiverName(fareAndAddressData.getAddress().getName());
        orderEntity.setReceiverPhone(fareAndAddressData.getAddress().getPhone());
        orderEntity.setReceiverProvince(fareAndAddressData.getAddress().getProvince());
        orderEntity.setReceiverRegion(fareAndAddressData.getAddress().getRegion());
        orderEntity.setReceiverPostCode(fareAndAddressData.getAddress().getPostCode());
        orderEntity.setBillReceiverPhone(fareAndAddressData.getAddress().getPhone());
        orderEntity.setMemberId(fareAndAddressData.getAddress().getMemberId());

        // 设置订单状态
        orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        orderEntity.setSourceType(0);
        orderEntity.setAutoConfirmDay(7);
        return orderEntity;
    }


}