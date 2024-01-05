package cmc.mybatisc.annotation;

import cmc.mybatisc.base.model.BaseDict;
import cmc.mybatisc.datafilter.core.DataFilterOption;

import java.lang.annotation.*;

/**
 * 根据字段数据过滤，静态的，动态的请创建专属的数据过滤器
 *
 * @author 程梦城
 * @version 1.0.0
 * &#064;date  2023/07/07
 */
@Target({ElementType.TYPE, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataScope {
    /**
     * 生效的表名,默认全部表生效
     */
    Table[] from();

    /**
     * 需要过滤的表别名
     */
    String alias() default "";

    /**
     * 限权字段名
     */
    Field field();

    /**
     * 需要连表进行限权的表
     */
    Join[] join() default {};

    /**
     * 数据范围值
     *
     * @return {@link String}
     */
    String dataScopeValue();

    /**
     * 匹配模式
     */
    String matchPattern() default DataFilterOption.TO;


    @Dict("表名")
    enum Table implements BaseDict<String> {
        ALL("全部", "*");

        Table(String name, String value) {
            this.initialize(name, value);
        }

        @Override
        public String toString() {
            return this.getStrValue();
        }
    }

    @Dict("字段")
    enum Field implements BaseDict<String> {
        ID("主键", "id");

        Field(String name, String value) {
            this.initialize(name, value);
        }

        @Override
        public String toString() {
            return this.getStrValue();
        }
    }

    @Dict("连接符")
    enum Oan implements BaseDict<String> {
        OR("or"),
        AND("and"),
        NOT("not");

        Oan(String value) {
            this.initialize("", value);
        }

        @Override
        public String toString() {
            return this.getStrValue();
        }
    }

    /**
     * 比较
     *
     * @author 程梦城
     * @version 1.0.0
     * &#064;date  2023/11/18
     */
    @Dict("自定义比较")
    enum Compare implements BaseDict<String> {
        EQUAL_TO("="),
        GREATER_THAN(">"),
        LESS_THAN("<"),
        NOT_EQUAL_TO("!=");

        Compare(String value) {
            this.initialize("", value);
        }

        @Override
        public String toString() {
            return this.getStrValue();
        }
    }
}
