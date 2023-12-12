package cmc.mybatisc.utils.reflect;

import lombok.Getter;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 通用类型
 *
 * @author 程梦城
 * @version 1.0.0
 * &#064;date  2023/07/07
 */
public class GenericType {
    private final Class<?> mainClass;
    @Getter
    private final List<Class<?>> list;
    private final Map<Class<?>, GenericType> superTypes;

    /**
     * 通用类型
     *
     * @param list 列表
     */
    private GenericType(Class<?> mainClass, List<Class<?>> list, Map<Class<?>, GenericType> superTypes) {
        this.mainClass = mainClass;
        this.list = list;
        this.superTypes = superTypes;
    }

    /**
     * 获取类上的泛型
     *
     * @param clazz 克拉兹
     * @return {@link Class}<{@link ?}>
     */
    public static GenericType forClass(Class<?> clazz) {
        if (clazz == null) {
            return new GenericType(clazz, Collections.emptyList(), Collections.emptyMap());
        }
        List<Class<?>> list = new ArrayList<>();
        Map<Class<?>, GenericType> map = new HashMap<>();
        for (Type type : clazz.getGenericInterfaces()) {
            if (type instanceof Class<?>) {
                map.put((Class<?>) type, forClass((Class<?>) type));
            } else if (type instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) type;
                List<Class<?>> collect = Arrays.stream(parameterizedType.getActualTypeArguments()).filter(e -> e instanceof Class).map(e -> (Class<?>) e).collect(Collectors.toList());
                Type rawType = parameterizedType.getRawType();
                if (rawType != clazz) {
                    map.put((Class<?>) rawType, new GenericType((Class<?>) rawType, collect, Collections.emptyMap()));
                }
                list.addAll(collect);
            }
        }
        return new GenericType(clazz, list, map);
    }

    /**
     * 从方法上获取泛型
     *
     * @param method 方法
     * @return {@link Class}<{@link ?}>
     */
    public static GenericType forMethod(Method method) {
        return forMethod(null, method);
    }

    /**
     * 从方法上获取泛型
     *
     * @param clazz  克拉兹
     * @param method 方法
     * @return {@link Class}<{@link ?}>
     */
    public static GenericType forMethod(Class<?> clazz, Method method) {
        Type type = method.getGenericReturnType();
        if (!(type instanceof ParameterizedTypeImpl)) {
            return new GenericType(clazz, Collections.emptyList(), Collections.emptyMap());
        }
        Type[] actualTypeArguments = ((ParameterizedTypeImpl) type).getActualTypeArguments();
        if (actualTypeArguments.length == 0) {
            return new GenericType(clazz, Collections.emptyList(), Collections.emptyMap());
        }

        List<Class<?>> collect = Arrays.stream(actualTypeArguments).filter(e -> e instanceof Class).map(e -> (Class<?>) e).collect(Collectors.toList());
        if (!collect.isEmpty()) {
            return new GenericType(clazz, collect, Collections.emptyMap());
        }
        // 向类上获取泛型
        return forClass(clazz);
    }


    /**
     * 获取第一个泛型
     *
     * @return {@link Class}<{@link ?}>
     */
    public Class<?> first() {
        return this.first(null);
    }

    public Class<?> first(Class<?> defaultValue) {
        return this.list.isEmpty() ? defaultValue : this.list.get(0);
    }

    /**
     * 获取最后一个泛型
     *
     * @return {@link Class}<{@link ?}>
     */
    public Class<?> last() {
        return this.last(null);
    }

    public Class<?> last(Class<?> defaultValue) {
        return this.list.isEmpty() ? defaultValue : this.list.get(this.list.size() - 1);
    }

    public Class<?> get(int index) {
        return this.get(index, null);
    }

    public Class<?> get(Class<?> clazz, int index) {
        GenericType genericType = this.getGenericType(clazz);
        if (genericType != null) {
            return genericType.get(index);
        }
        return null;
    }

    public GenericType getGenericType(Class<?> clazz) {
        if (clazz == this.mainClass) {
            return this;
        }
        if (this.superTypes.containsKey(clazz)) {
            return this.superTypes.get(clazz);
        }
        // 遍历获取
        for (GenericType value : this.superTypes.values()) {
            GenericType genericType = value.getGenericType(clazz);
            if (genericType != null) {
                return genericType;
            }
        }
        return null;
    }

    public Class<?> get(int index, Class<?> defaultValue) {
        return this.list.isEmpty() ? defaultValue : this.list.get(index);
    }

}
