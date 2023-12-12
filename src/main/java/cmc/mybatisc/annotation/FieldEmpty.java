package cmc.mybatisc.annotation;

import cmc.mybatisc.base.CodeStandardEnum;
import cmc.mybatisc.model.DelFlag;

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
    String table() default "";

    /**
     * 需要清空的字段列表
     */
    DataScope.Field[] value();

    /**
     * 删除标志
     */
    DelFlag[] delFlag() default {DelFlag.IS_DELETED, DelFlag.DEL_FLAG, DelFlag.TIME_DEL_FLAG,DelFlag.IS_DELETE};

    /**
     * 类型
     */
    CodeStandardEnum nameMode() default CodeStandardEnum.UNDERLINE;

    /**
     * 移除
     */
    String removeSuffix() default "List";
}
