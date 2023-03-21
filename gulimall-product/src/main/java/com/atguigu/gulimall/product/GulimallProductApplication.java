package com.atguigu.gulimall.product;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 1、整合MyBatis-Plus
 *      1）、导入依赖
 *      2）、配置
 *          1、配置数据源
 *              1）、导入数据源驱动
 *              2）、在application.yml配置数据源相关信息
 *          2、配置MyBatis-Plus：
 *              1）、使用@MapperScan【@Mapper和@MapperScan两者使用一个就行，由于代码生成器生成的代码带着@Mapper，这里我们就无须次此注解了】
 *
 * 2、逻辑删除
 *  1）、配置全局的逻辑删除规则（省略）
 *  2）、配置逻辑删除的组件Bean（省略）
 *  3）、给Bean加上逻辑删除注解@TableLogic
 *
 * 3、JSR303数据校验
 *  1）、给Bean添加校验注解：javax.validation.constraints，并定义自己的message提示
 *  2）、开启校验功能@Valid
 *      效果：校验错误以后会有默认的响应；
 *  3）、给校验的bean后紧跟一个BindingResult，就可以获取到校验的结果
 *  4）、分组校验（多场景的复杂校验）
 *      1）、@NotBlank(message = "品牌名必须提交",groups = {AddGroup.class,UpdateGroup.class})
 *          给校验注解标注什么情况下需要进行校验
 *      2）、@Validated({AddGroup.class})在Controller的方法入口出标注方法所属的分组
 *      3）、默认没有指定分组的校验注解@NotBlank，在分组校验情况@Validated({AddGroup.class})下不生效
 *
 *  5）、自定义校验
 *      1）、编写一个自定义的校验注解
 *      2）、编写一个自定义的校验器
 *      3）、关联自定义的校验器和自定义的校验注解
 *      @Documented
 *      @Constraint(validatedBy = {ListValueConstraintValidator.class【可以指定多个不同的校验器，适配不同类型的校验】 }) //指定校验器
 *      @Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE }) //标注位置
 *      @Retention(RUNTIME) // 时机
 *      public @interface ListValue {}
 *
 *
 * 4、统一的异常处理
 *  1）、编写异常处理类，使用@ControllerAdvice
 *  2）、使用@ExceptionHandler标注方法可以处理的异常
 *
 */
@EnableDiscoveryClient
@SpringBootApplication
public class GulimallProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallProductApplication.class, args);
    }

}
