package cmc.mybatisc.annotation;


import cmc.mybatisc.config.interfaces.TableEntity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 搜索注解，最强大的注解
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Search {
    /**
     * 查询表
     */
    Class<? extends TableEntity> table() default TableEntity.class;

    /**
     * 关联查询
     */
    Join[] join() default {};

    /**
     * 字段列表，使用此选项时 excludeField addField将失效
     *
     * @return {@link String[]}
     */
    String[] fields() default {};

    /**
     * 排除字段
     */
    String[] excludeField() default {};

    /**
     * 添加字段
     */
    String[] addField() default {};

    /**
     * 移除后缀
     */
    String removeSuffix() default "List";

    /**
     * 是否映射
     * 主键 对数据
     */
    boolean mapping() default false;

    /**
     * 映射字段 默认主键
     */
    String mappingField() default "";
}