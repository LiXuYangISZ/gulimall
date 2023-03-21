package com.atguigu.common.valid;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.List;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author lxy
 * @version 1.0
 * @Description 自定义注解
 * @date 2023/3/21 14:43
 */

/**
 *
 */
@Documented
@Constraint(validatedBy = {ListValueConstraintValidator.class }) //指定校验器
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE }) //标注位置
@Retention(RUNTIME) // 时机
public @interface ListValue {
    /**
     * 错误信息
     */
    String message() default "{com.atguigu.common.valid.ListValue.message}";

    /**
     * 分组
     */
    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

    int[] values() default {};
}
