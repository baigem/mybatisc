package cmc.mybatisc.annotation;

import cmc.mybatisc.base.CodeStandardEnum;
import cmc.mybatisc.config.interfaces.DelFlag;
import cmc.mybatisc.config.interfaces.TableEntity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 字段清空
 *
 * @author cmc
 * &#064;date  2023/05/24
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface FieldEmpty {
    /**
     * 查询表
     */
    Class<TableEntity> table() default TableEntity.class;

    /**
     * 需要清空的字段列表
     */
    DataScope.Field[] value();

    /**
     * 类型
     */
    CodeStandardEnum nameMode() default CodeStandardEnum.UNDERLINE;

    /**
     * 移除
     */
    String removeSuffix() default "List";
}
