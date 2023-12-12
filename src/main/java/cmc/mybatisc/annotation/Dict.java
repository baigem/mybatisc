package cmc.mybatisc.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 字典注解
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Dict {
    String value();

    String code() default "";
}
