package cmc.mybatisc.annotation;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Param {
    /**
     * 字段参数名称
     */
    String value() default "";

    /**
     * 是否模糊查询
     */
    boolean like() default false;

    /**
     * 为null
     */
    boolean isNull() default false;

    /**
     * 此字段是否排序
     */
    boolean sort() default false;

    /**
     * 排序规则
     */
    String sortRule() default "desc";

    /**
     * or and not
     *
     * @return {@link String}
     */
    DataScope.Oan oan() default DataScope.Oan.AND;

    /**
     * 左
     */
    Over left() default Over.EMPTY;

    /**
     * 右
     */
    Over right() default Over.EMPTY;

    enum  Over{
        START("("),
        END(")"),
        EMPTY("");

        public final String value;
        Over(String value) {
            this.value = value;
        }
    }

}
