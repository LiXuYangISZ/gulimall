# gulimall

#### 介绍
谷粒商城

#### 软件架构
软件架构说明


#### 安装教程

1.  xxxx
2.  xxxx
3.  xxxx

#### 使用说明

1.  xxxx
2.  xxxx
3.  xxxx

#### 参与贡献

1.  Fork 本仓库
2.  新建 Feat_xxx 分支
3.  提交代码
4.  新建 Pull Request

#### 项目后期优化

**<font color=blue>Ware服务</font>**

- [ ] 1. ware->wms_purchase、wms_purchase_detail：采购金额我们可以搞一下（远程调用获取这个商品的金额）
- [ ] 2. ware->wms_purchase 仓库id我们可以设置下，比如我们规定 一个采购单只能采购同一个仓库的【这个我们管理员自己控制就行了，就好像外卖员、快递员负责自己的区域一样，别问我为啥】   
- [ ] 3. wms_purchase_detail 可以进行扩展，比如 采购失败原因字段（reason）、采购成功件数、采购失败件数...


**<font color=blue>Ware服务</font>**
- [ ] 1. search 对于属性包含;的，面包屑连接异常，比如网络类型：5g;4g。初步判断是编码问题！！！

**<font color=blue>Other</font>**

- [ ] 1. 入参全部改为DTO，视图数据全部改为VO。
- [ ] 2. 请求遵循RESTFUL规范
- [ ] <font color=green>3. 自定义判空工具类：包括 isBlank+ "0" +"null" + 其他字符 </font>

```java
public static boolean isAllNotEmpty(String... args) {
    return Stream.of(args).noneMatch(StringUtils::isBlank);
}
```

#### 项目存在的Bug
1. 查询SPU商品有时候回报：Required Long parameter 'catId' is not present（具体原因待排查）
2. 时间更新存在时差，比如上架时更新spu_info