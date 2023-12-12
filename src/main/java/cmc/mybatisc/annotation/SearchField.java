package cmc.mybatisc.annotation;

import cmc.mybatisc.base.CodeStandardEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 搜索字段
 *
 * @author cmc
 * @date 2023/05/31
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface SearchField {
    /**
     * 字段名称，默认是变量名
     */
    String value() default "";

    /**
     * 连表查询
     */
    Join[] join() default {};

    /**
     * 名称规范
     */
    CodeStandardEnum nameMode() default CodeStandardEnum.UNDERLINE;

    /**
     * 是否模糊搜索
     */
    boolean like() default false;

    /**
     * 是否在日期范围搜索 (如果是日期类型的话，会更加详细)
     */
    boolean range() default false;

    /**
     * 比较符号
     */
    DataScope.Compare compare() default DataScope.Compare.EQUAL_TO;

    /**
     * 范围搜索模式后缀
     */
    String rangeModeSuffix() default "QueryMode";

    /**
     * 范围结束变量后缀
     */
    String rangeEndSuffix() default "End";

    /**
     * list类型的变量后缀
     */
    String listTypeSuffix() default "List";

    /**
     * 是否排序
     */
    boolean sort() default false;

    /**
     * 排序序号
     */
    int sortNo() default 999;

    /**
     * 排序规则
     */
    String sortRule() default "DESC";

    /**
     * 是否分组
     */
    boolean group() default false;

    /**
     * 自定义实体
     *
     * @return boolean
     */
    boolean customEntity() default true;

    /**
     * 是否是必须的
     */
    boolean and() default true;

    /**
     * 分组查询，分组中的字段会被(包裹)
     */
    String grouping() default "";
}
