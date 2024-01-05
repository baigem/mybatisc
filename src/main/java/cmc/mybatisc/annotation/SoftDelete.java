package cmc.mybatisc.annotation;

import cmc.mybatisc.config.interfaces.TableEntity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 逻辑删除
 *
 * @author cmc
 * @date 2023/05/24
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface SoftDelete {
    /**
     * 查询表
     */
    Class<TableEntity> table() default TableEntity.class;

    /**
     * 查询字段
     */
    String value() default "";

    /**
     * 移除
     */
    String removeSuffix() default "List";
}
