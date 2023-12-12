package cmc.mybatisc.annotation;


import cmc.mybatisc.model.DelFlag;

/**
 * 连表
 *
 * @author cmc
 * @date 2023/05/31
 */
public @interface Join {
    /**
     * 表名
     */
    String table();

    /**
     * 字段名
     */
    String field();

    /**
     * 联接类型 默认左连接
     *
     * @return {@link String}
     */
    String joinType() default "left";

    /**
     * 关联的表，不写的话默认主表
     */
    String linkTable() default "";

    /**
     * 关联的字段，不写的话默认是上面设置的字段名
     */
    String linkField() default "";

    /**
     * 是是数据源
     */
    boolean isDataScope() default false;

    /**
     * 德尔旗
     *
     * @return boolean
     */
    DelFlag[] delFlag() default {};
}
