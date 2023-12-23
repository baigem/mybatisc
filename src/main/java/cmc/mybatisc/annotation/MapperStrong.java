package cmc.mybatisc.annotation;

import cmc.mybatisc.base.CodeStandardEnum;
import cmc.mybatisc.config.interfaces.DelFlag;

import java.lang.annotation.*;

/**
 * 映射器增强
 *
 * @author cmc
 * &#064;date  2023/05/25
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
public @interface MapperStrong {

    /**
     * 表名
     */
    String name() default "";

    /**
     * 表结构对应的实体entity类
     *
     * @return {@link Class}<{@link ?}>
     */
    Class<?> value() default Class.class;

    /**
     * 类型
     *
     * @return {@link CodeStandardEnum}
     */
    CodeStandardEnum nameMode() default CodeStandardEnum.UNDERLINE;

    /**
     * 忽略字段
     *
     * @return {@link String[]}
     */
    String[] ignoreField() default {};
}
