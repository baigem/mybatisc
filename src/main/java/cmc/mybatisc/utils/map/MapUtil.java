package cmc.mybatisc.utils.map;

import cn.hutool.core.util.ReflectUtil;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MapUtil {
    /**
     * 要映射
     *
     * @param list 列表
     * @param fun  函数
     * @return {@link Map}<{@link K},{@link V}>
     */
    public static <K,V> Map<K,V> toMap(List<V> list, Function<V, K> fun){
        return toMap(list,fun,e->e);
    }

    /**
     * 要映射
     *
     * @param list  列表
     * @param key   钥匙
     * @param value 值
     * @return {@link Map}<{@link K}, {@link V}>
     */
    public static <T,K,V> Map<K, V> toMap(List<T> list, Function<T, K> key,Function<T, V> value){
        if(list == null || list.isEmpty()){
            return Collections.emptyMap();
        }
        return list.stream().collect(Collectors.toMap(key, value));
    }

    /**
     * 分组
     *
     * @param list   列表
     * @param getKey 获取密钥
     * @return {@link Map}<{@link K}, {@link List}<{@link T}>>
     */
    public static <K,T> Map<K, List<T>> group(List<T> list, Function<T, K> getKey) {
        return list.stream().collect(Collectors.groupingBy(getKey));
    }

    /**
     * 要映射
     *
     * @param obj obj
     * @return {@link Map}<{@link String}, {@link Object}>
     */
    public static Map<String, Object> toMap(Object obj) {
        Map<String,Object> result = new HashMap<>();
        Map<String, Field> fieldMap = ReflectUtil.getFieldMap(obj.getClass());
        fieldMap.forEach((key,value)->{
            result.put(key,ReflectUtil.getFieldValue(obj,value));
        });
        return result;
    }
}
