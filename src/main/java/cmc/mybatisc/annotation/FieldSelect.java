package cmc.mybatisc.annotation;

import cmc.mybatisc.base.CodeStandardEnum;
import cmc.mybatisc.model.DelFlag;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 字段查询
 *
 * @author cmc
 * &#064;date  2023/05/24
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface FieldSelect {
    /**
     * 查询表
     */
    String table() default "";

    /**
     * 查询字段
     */
    String value() default "";

    /**
     * 是否有逻辑删除字段
     */
    DelFlag[] delFlag() default {DelFlag.IS_DELETED, DelFlag.DEL_FLAG, DelFlag.TIME_DEL_FLAG,DelFlag.IS_DELETE};

    /**
     * 类型
     */
    CodeStandardEnum nameMode() default CodeStandardEnum.UNDERLINE;

    /**
     * 是否模糊
     */
    boolean like() default false;

    /**
     * 移除
     */
    String removeSuffix() default "List";

    /**
     * 默认映射 主键 对数据，前提是返回数据类型为Map的时候
     */
    boolean mapping() default true;

    /**
     * 映射字段
     */
    String mappingField() default "";

    /**
     * 允许空,如果允许空的话，查询时遇到空的字段将忽略条件，如果字段注解上有isNull字段将进行为空条件
     *
     * @return boolean
     */
    boolean allowNull() default false;

    /**
     * 从获取到的数据获取其中一个
     *
     * @return boolean
     */
    boolean first() default false;
}
