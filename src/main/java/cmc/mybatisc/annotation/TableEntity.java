package cmc.mybatisc.annotation;

import cmc.mybatisc.base.CodeStandardEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 表实体
 *
 * @author cmc
 * &#064;date  2023/05/31
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface TableEntity {
    /**
     * 表名称
     */
    String value() default "";

    /**
     * 类名是否是表名
     */
    boolean isClassName() default true;

    /**
     * 类型
     */
    CodeStandardEnum nameMode() default CodeStandardEnum.UNDERLINE;
}
