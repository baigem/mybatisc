package cmc.mybatisc.datafilter.core;

import cmc.mybatisc.annotation.DataScope;
import cmc.mybatisc.annotation.Join;
import cmc.mybatisc.base.model.BaseDict;
import cn.hutool.core.util.ReflectUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;

/**
 * 数据范围源数据
 *
 * @author cmc
 * &#064;date  2023/02/04
 */
@Data
@Slf4j
@EqualsAndHashCode
public class DataScopeSourceData {
    /**
     * 生效的表名,默认全部表生效
     */
    private final String[] from;

    /**
     * 需要连表进行限权的表
     */
    private final List<Join> join;
    /**
     * 限权字段名
     */
    private final String field;
    /**
     * 注释
     */
    private final Annotation annotation;
    /**
     * 需要过滤的表别名
     */
    private String alias;

    public DataScopeSourceData(String[] from, List<Join> join, String field, Annotation annotation) {
        this.from = from;
        this.join = join;
        this.field = field;
        this.annotation = annotation;
    }

    public static DataScopeSourceData create(Annotation dataScope) {
        try {
            DataScope.Table[] from = ReflectUtil.invoke(dataScope, "from");
            Join[] join = ReflectUtil.invoke(dataScope, "join");
            DataScope.Field field = ReflectUtil.invoke(dataScope, "field");
            return new DataScopeSourceData(Arrays.stream(from).map(BaseDict::getValue).toArray(String[]::new), Arrays.asList(join), field.getValue(), dataScope);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("字段类型错误或注解缺少必备字段from、join、field" + e.getMessage());
        }
    }

}
