package cmc.mybatisc.utils;

import cmc.mybatisc.annotation.FieldNotEmpty;
import cmc.mybatisc.utils.reflect.ReflectUtils;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 检查工具【专注于数据库，mybatis等相关的校验工作】
 *
 * @author 程梦城
 * @version 1.0.0
 * &#064;date  2023/07/07
 */
public class CheckTools {
    /**
     * 保存检查
     */
    public static void check(Object obj) {
        // 获取所有的字段
        List<Field> allField = ReflectUtils.getAllField(obj.getClass()).stream().filter(e -> e.isAnnotationPresent(FieldNotEmpty.class)).collect(Collectors.toList());
        for (Field field : allField) {
            Object fieldValue = ReflectUtil.getFieldValue(obj, field);
            if (ObjectUtil.isEmpty(fieldValue)) {
                FieldNotEmpty annotation = field.getAnnotation(FieldNotEmpty.class);
                if (StringUtils.hasText(annotation.value())) {
                    throw new RuntimeException(annotation.value());
                }
                throw new RuntimeException(field.getName() + "不可为空");
            }
        }
    }
}
