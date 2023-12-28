package cmc.mybatisc.utils.reflect;

import cmc.mybatisc.base.model.MapList;
import cmc.mybatisc.utils.list.ListUtil;
import cmc.mybatisc.utils.map.MapUtil;
import lombok.Data;
import lombok.Getter;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
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
    private final MapList<String,Type<?>> list;
    private final Map<Class<?>, GenericType> superTypes;

    /**
     * 通用类型
     *
     * @param list 列表
     */
    private GenericType(Class<?> mainClass, MapList<String,Type<?>> list, Map<Class<?>, GenericType> superTypes) {
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
        return forClass(clazz,Collections.emptyMap());
    }
    private static GenericType forClass(Class<?> clazz,Map<String,Type<?>> typeMap) {
        if (clazz == null) {
            return new GenericType(clazz, new MapList<>(), Collections.emptyMap());
        }
        Map<Class<?>, GenericType> map = new HashMap<>();

        List<java.lang.reflect.Type> types = new CopyOnWriteArrayList<>(Arrays.asList(clazz.getGenericInterfaces()));
        types.add(clazz.getGenericSuperclass());
        // 遍历处理
        MapList<String,Type<?>> list = new MapList<>(Type::getName);
        ListUtil.forEach(types,(type,add)->{
            if (type instanceof Class<?>) {
                map.put((Class<?>) type, forClass((Class<?>) type, list.toMap()));
            } else if (type instanceof ParameterizedType) {
                MapList<String,Type<?>> temp = new MapList<>(Type::getName);

                ParameterizedType parameterizedType = (ParameterizedType) type;
                java.lang.reflect.Type rawType = parameterizedType.getRawType();
                if(rawType == clazz || !(rawType instanceof Class<?>)){
                    return;
                }
                TypeVariable<? extends Class<?>>[] typeParameters = ((Class<?>) rawType).getTypeParameters();
                java.lang.reflect.Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                // 插入泛型
                for (int i = 0; i < actualTypeArguments.length; i++) {
                    if(actualTypeArguments[i] instanceof Class){
                        temp.add(new Type<>((Class<?>) rawType,(Class<?>) actualTypeArguments[i],typeParameters[i],i));
                    }else if(actualTypeArguments[i] instanceof TypeVariable){
                        String name = ((TypeVariable<?>) actualTypeArguments[i]).getName();
                        Type<?> type1 = typeMap.containsKey(name) ? typeMap.get(name) : list.get(name);
                        // 原始的泛型对象
                        if(type1 == null){
                            type1 = new Type<>((Class<?>) rawType,Object.class,typeParameters[i],i);
                        }
                        temp.add(new Type<>((Class<?>) rawType,type1,typeParameters[i].getName(),i));
                    }
                }
                map.put((Class<?>) rawType, new GenericType((Class<?>) rawType, temp, Collections.emptyMap()));
                list.addAll(temp);
                add.add(((Class<?>) rawType).getGenericInterfaces());
            }
        });
        // 去重
        return new GenericType(clazz, list.distinct(Type::getTypeVariable), map);
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
        return forMethod(clazz,method,Collections.emptyMap());
    }
    public static GenericType forMethod(Class<?> clazz, Method method,Map<String,Type<?>> typeMap) {
        java.lang.reflect.Type type = method.getGenericReturnType();
        if (!(type instanceof ParameterizedTypeImpl)) {
            return new GenericType(clazz, new MapList<>(), Collections.emptyMap());
        }
        java.lang.reflect.Type[] actualTypeArguments = ((ParameterizedTypeImpl) type).getActualTypeArguments();
        if (actualTypeArguments.length == 0) {
            return new GenericType(clazz, new MapList<>(), Collections.emptyMap());
        }
        TypeVariable<? extends Class<?>>[] typeParameters = ((ParameterizedTypeImpl) type).getRawType().getTypeParameters();
        // 插入泛型
        MapList<String,Type<?>> list = new MapList<>(Type::getName);
        for (int i = 0; i < actualTypeArguments.length; i++) {
            if(actualTypeArguments[i] instanceof Class){
                list.add(new Type<>(((ParameterizedTypeImpl) type).getRawType(),(Class<?>) actualTypeArguments[i],typeParameters[i],i));
            }else if(actualTypeArguments[i] instanceof TypeVariable){
                Type<?> type1 = typeMap.get(((TypeVariable<?>) actualTypeArguments[i]).getName());
                list.add(new Type<>(((ParameterizedTypeImpl) type).getRawType(),type1,typeParameters[i].getName(),i));
            }
        }
        return new GenericType(clazz, list, Collections.emptyMap());
//        if (!list.isEmpty()) {
//        }
//        // 向类上获取泛型
//        return forClass(clazz);
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
        return this.list.isEmpty() ? defaultValue : this.list.get(0).getClazz();
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
        return this.list.isEmpty() ? defaultValue : this.list.get(this.list.size() - 1).getClazz();
    }

    public Class<?> get(int index) {
        return this.get(index, null);
    }

    public Class<?> get(String name) {
        return this.get(name, null);
    }

    public Class<?> get(Class<?> clazz, int index) {
        GenericType genericType = this.getGenericType(clazz);
        if (genericType != null) {
            return genericType.get(index);
        }
        return null;
    }

    public Class<?> get(Class<?> clazz, String name) {
        GenericType genericType = this.getGenericType(clazz);
        if (genericType != null) {
            return genericType.get(name);
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
        return this.list.isEmpty() ? defaultValue : this.list.get(index).getClazz();
    }

    public Class<?> get(String name, Class<?> defaultValue) {
        return this.list.isEmpty() ? defaultValue : this.list.get(name).getClazz();
    }


    /**
     * 通用范式
     *
     * @author 程梦城
     * @version 1.0.0
     * &#064;date  2023/12/29
     */
    @Data
    public static class Type<D extends GenericDeclaration>{
        /**
         * 泛型的名称
         */
        private String name;
        /**
         * 索引
         */
        private Integer index;

        /**
         * 克拉兹
         */
        private Class<?> clazz;

        /**
         * 原始类型
         */
        private Class<?> rawType;

        /**
         * 类型变量
         */
        private TypeVariable<D> typeVariable;

        public Type(Class<?> rawType,Class<?> clazz, TypeVariable<D> typeVariable,Integer index) {
            this.rawType = rawType;
            this.index = index;
            this.clazz = clazz;
            this.typeVariable = typeVariable;
            if(typeVariable != null){
                this.name = typeVariable.getName();
            }
        }

        public Type(Class<?> rawType,Type<D> type,String name, int index) {
            this.rawType = rawType;
            this.index  = index;
            this.name = name;
            this.clazz = type.getClazz();
            this.typeVariable = type.getTypeVariable();
        }
    }
}
