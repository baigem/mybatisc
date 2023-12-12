package cmc.mybatisc.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 表字段不为空
 *
 * @author 程梦城
 * @version 1.0.0
 * &#064;date  2023/07/07
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface FieldNotEmpty {
    String value() default "";
}
