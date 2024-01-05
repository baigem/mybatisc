package cmc.mybatisc.annotation;

import cmc.mybatisc.config.interfaces.TableEntity;
import cmc.mybatisc.core.util.TableStructure;

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
     * 表结构对应的实体entity类
     *
     * @return {@link Class}<{@link ?}>
     */
    Class<? extends TableEntity> value() default TableEntity.class;

    /**
     * 忽略字段
     *
     * @return {@link String[]}
     */
    String[] ignoreField() default {};
}
