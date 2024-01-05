package cmc.mybatisc.model;

import cmc.mybatisc.utils.reflect.ReflectUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 字段查询数据源
 *
 * @author 程梦城
 * @version 1.0.0
 * &#064;date  2023/07/07
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FieldSelectDataSource {
    private static final Map<Annotation, FieldSelectDataSource> CACHE = new HashMap<>();
    /**
     * 允许空
     */
    boolean allowNull;
    /**
     * id
     */
    private String id;
    /**
     * 方法
     */
    private Method method;
    /**
     * 表
     */
    private String table;
    /**
     * 查询字段
     */
    private String field;
    /**
     * 是否模糊搜索
     */
    private Boolean like;
    /**
     * 需要移除的后缀
     */
    private String removeSuffix;
    /**
     * 映射的字段默认主键
     */
    private Boolean mapping;
    /**
     * 映射字段
     */
    private String mappingField;
    /**
     * 从获取到的数据获取其中一个
     */
    private Boolean first;

    public static FieldSelectDataSource generate(Annotation annotation) {
        if (CACHE.containsKey(annotation)) {
            return CACHE.get(annotation);
        }
        // 进行反射获取注解中的值
        String table = ReflectUtils.invokeGet(annotation, "table", "");
        String field = ReflectUtils.invokeGet(annotation, "value", "");
        boolean isLike = ReflectUtils.invokeGet(annotation, "like", false);
        String removeSuffix = ReflectUtils.invokeGet(annotation, "removeSuffix", "");
        boolean mapping = ReflectUtils.invokeGet(annotation, "mapping", false);
        String mappingField = ReflectUtils.invokeGet(annotation, "mappingField", "");
        boolean first = ReflectUtils.invokeGet(annotation, "first", false);
        boolean allowNull = ReflectUtils.invokeGet(annotation, "allowNull", false);
        return FieldSelectDataSource.builder()
                .table(table)
                .field(field)
                .like(isLike)
                .removeSuffix(removeSuffix)
                .mapping(mapping)
                .mappingField(mappingField)
                .first(first)
                .allowNull(allowNull)
                .build();
    }
}
