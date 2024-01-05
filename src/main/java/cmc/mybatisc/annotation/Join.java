package cmc.mybatisc.annotation;

import cmc.mybatisc.config.interfaces.TableEntity;

/**
 * 连表
 *
 * @author 程梦城
 * @version 1.0.0
 * &#064;date  2023/12/24
 */
public @interface Join {
    /**
     * 表名
     */
    Class<TableEntity> joinTable();

    /**
     * 字段名
     */
    String joinField();

    /**
     * 联接类型 默认左连接
     *
     * @return {@link String}
     */
    String joinType() default "left";

    /**
     * 关联的表，不写的话默认主表
     */
    Class<TableEntity> onTable() default TableEntity.class;

    /**
     * 关联的字段，不写的话默认是上面设置的字段名
     */
    String onField() default "";

    /**
     * 是是数据源
     */
    boolean useDataSource() default false;
}
