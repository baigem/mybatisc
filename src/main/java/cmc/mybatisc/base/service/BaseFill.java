package cmc.mybatisc.base.service;

import cmc.mybatisc.utils.PageUtils;
import cmc.mybatisc.utils.reflect.GenericType;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.map.MapUtil;

import java.io.Serializable;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 基础填充
 *
 * @author 程梦城
 * @version 1.0.0
 * &#064;date  2023/12/28
 */
public interface BaseFill<F,D, P,K extends Serializable>{
    /**
     * 获取数据映射
     * this.listToMap(ids, this::listByIds, ProducePaintStockLog::getId);
     * this.listToMap(ids, this.baseMapper::listByIdList, ProduceTaskDTO::getId);
     *
     * @param ids 身份证
     * @return {@link Map}<{@link ?}, {@link F}>
     */
    default Map<K, D> getDataMap(List<K> ids) {
        return this.getDataMap(ids, null);
    }

    Map<K, D> getDataMap(List<K> ids, P p);

    default Map<K, List<D>> getDataListMap(List<K> mainids) {
        return this.getDataListMap(mainids, null);
    }

    /**
     * 获取数据列表映射
     *
     * @param mainids 忽略
     * @param p       p
     * @return {@link Map}<{@link ?} {@link extends} {@link K}, {@link List}<{@link D}>>
     */
    default Map<K, List<D>> getDataListMap(List<K> mainids, P p) {
        List<Long> mainids1 = (List<Long>) mainids;
        throw new RuntimeException("请重写接口后调用");
    }

    /**
     * 填充外部模型
     *
     * @param list     列表
     * @param getKey   获取密钥
     * @param setValue 设置值
     */
    default <S> void fillExternalModel(List<S> list, Function<S, K> getKey, BiConsumer<S, D> setValue) {
        this.fillExternalModel(list, MapUtil.of(getKey, setValue), null);
    }

    default <S> void fillExternalModel(List<S> list, Function<S, K> getKey, BiConsumer<S, D> setValue, P p) {
        this.fillExternalModel(list, MapUtil.of(getKey, setValue), null, p);
    }

    default <S> void fillExternalModel(List<S> list, Function<S, K> getKey, BiConsumer<S, D> setValue, Function<D, K> getDataId) {
        this.fillExternalModel(list, MapUtil.of(getKey, setValue), getDataId);
    }

    default <S> void fillExternalModel(List<S> list, Function<S, K> getKey, BiConsumer<S, D> setValue, Function<D, K> getDataId, P type) {
        this.fillExternalModel(list, MapUtil.of(getKey, setValue), getDataId, type);
    }

    /**
     * 填充外部模型
     *
     * @param list 列表
     * @param map  地图
     */
    default <S> void fillExternalModel(List<S> list, Map<Function<S, K>, BiConsumer<S, D>> map, Function<D, K> getDataId) {
        list.forEach(this.fillExternalModelUseCallback(list, map, getDataId));
    }

    default <S> void fillExternalModel(List<S> list, Map<Function<S, K>, BiConsumer<S, D>> map, Function<D, K> getDataId, P type) {
        list.forEach(this.fillExternalModelUseCallback(list, map, getDataId, type));
    }

    /**
     * 填充外部模型数据
     *
     * @param list     列表
     * @param getKey   获取密钥
     * @param setValue 设置值
     */
    default <S> void fillExternalModelSubData(List<S> list, Function<S, K> getKey, BiConsumer<S, List<D>> setValue) {
        this.fillExternalModelSubData(list, MapUtil.of(getKey, setValue));
    }

    default <S> void fillExternalModelSubData(List<S> list, Function<S, K> getKey, BiConsumer<S, List<D>> setValue, Function<D, K> getDataId) {
        this.fillExternalModelSubData(list, MapUtil.of(getKey, setValue), getDataId);
    }

    default <S> void fillExternalModelSubData(List<S> list, Function<S, K> getKey, BiConsumer<S, List<D>> setValue, P p) {
        this.fillExternalModelSubData(list, MapUtil.of(getKey, setValue), null, p);
    }

    default <S> void fillExternalModelSubData(List<S> list, Function<S, K> getKey, BiConsumer<S, List<D>> setValue, Function<D, K> getDataId, P p) {
        this.fillExternalModelSubData(list, MapUtil.of(getKey, setValue), getDataId, p);
    }

    default <S> void fillExternalModelSubData(List<S> list, Map<Function<S, K>, BiConsumer<S, List<D>>> map) {
        list.forEach(this.fillExternalModelSubDataUseCallback(list, map));
    }

    default <S> void fillExternalModelSubData(List<S> list, Map<Function<S, K>, BiConsumer<S, List<D>>> map, Function<D, K> getDataId) {
        list.forEach(this.fillExternalModelSubDataUseCallback(list, map, getDataId));
    }

    default <S> void fillExternalModelSubData(List<S> list, Map<Function<S, K>, BiConsumer<S, List<D>>> map, Function<D, K> getDataId, P p) {
        list.forEach(this.fillExternalModelSubDataUseCallback(list, map, getDataId, p));
    }

    /**
     * 填充外部模型使用回调
     *
     * @param list     列表
     * @param getKey   获取密钥
     * @param setValue 设置值
     * @return {@link Consumer}<{@link S}>
     */
    default <S> Consumer<S> fillExternalModelUseCallback(List<S> list, Function<S, K> getKey, BiConsumer<S, D> setValue) {
        return this.fillExternalModelUseCallback(list, MapUtil.of(getKey, setValue), null, null);
    }

    default <S> Consumer<S> fillExternalModelUseCallback(List<S> list, Map<Function<S, K>, BiConsumer<S, D>> map) {
        return this.fillExternalModelUseCallback(list, map, null, null);
    }

    default <S> Consumer<S> fillExternalModelUseCallback(List<S> list, Map<Function<S, K>, BiConsumer<S, D>> map, Function<D, K> getDataId) {
        return this.fillExternalModelUseCallback(list, map, getDataId, null);
    }

    default <S> Consumer<S> fillExternalModelUseCallback(List<S> list, Map<Function<S, K>, BiConsumer<S, D>> map, Function<D, K> getDataId, P type) {
        // 进行过滤去重
        List<K> ids = list.stream().flatMap(info -> map.keySet().stream().map(getKey -> getKey.apply(info))).filter(Objects::nonNull).distinct().collect(Collectors.toList());
        if (ids.isEmpty()) {
            return e -> {
            };
        }
        final Map<K, D> dataMap;
        if(getDataId != null){
            dataMap = this.getDataMap(ids, type).values().stream().collect(Collectors.toMap(getDataId, d -> d));
        }else{
            dataMap = this.getDataMap(ids, type);
        }
        return info -> {
            map.forEach((key, value) -> {
                value.accept(info, dataMap.get(key.apply(info)));
            });
        };
    }

    /**
     * 填充外部模型使用回调
     *
     * @param list     列表
     * @param getKey   获取密钥
     * @param setValue 设置值
     * @return {@link Consumer}<{@link S}>
     */
    default <S> Consumer<S> fillExternalModelSubDataUseCallback(List<S> list, Function<S, K> getKey, BiConsumer<S, List<D>> setValue) {
        return this.fillExternalModelSubDataUseCallback(list, MapUtil.of(getKey, setValue));
    }

    /**
     * 填充外部模型使用回调
     *
     * @param list 列表
     * @param map  地图
     * @return {@link Consumer}<{@link S}>
     */
    default <S> Consumer<S> fillExternalModelSubDataUseCallback(List<S> list, Map<Function<S, K>, BiConsumer<S, List<D>>> map) {
        return this.fillExternalModelSubDataUseCallback(list, map, null, null);
    }

    default <S> Consumer<S> fillExternalModelSubDataUseCallback(List<S> list, Map<Function<S, K>, BiConsumer<S, List<D>>> map, Function<D, K> getDataId) {
        return this.fillExternalModelSubDataUseCallback(list, map, getDataId, null);
    }

    default <S> Consumer<S> fillExternalModelSubDataUseCallback(List<S> list, Map<Function<S, K>, BiConsumer<S, List<D>>> map, Function<D, K> getDataId, P type) {
        List<K> ids = list.stream().flatMap(info -> map.keySet().stream().map(getKey -> getKey.apply(info))).filter(Objects::nonNull).distinct().collect(Collectors.toList());
        if (ids.isEmpty()) {
            return e -> {
            };
        }
        final Map<K, List<D>> dataMap;
        if(getDataId == null){
            dataMap = this.getDataListMap(ids, type);
        }else{
            dataMap = this.getDataListMap(ids, type).values().stream().flatMap(Collection::stream).collect(Collectors.groupingBy(getDataId));
        }
        return info -> {
            map.forEach((key, value) -> {
                value.accept(info, Optional.ofNullable(dataMap.get(key.apply(info))).orElse(Collections.emptyList()));
            });
        };
    }

    default <R extends F> R fill(R obj) {
        return this.fill(Collections.singletonList(obj)).stream().findFirst().orElse(null);
    }

    default <R extends F> R fill(R obj,P p) {
        return this.fill(Collections.singletonList(obj),p).stream().findFirst().orElse(null);
    }

    /**
     * 填充
     *
     * @param obj   obj
     * @param clazz 克拉兹
     * @return {@link R}
     */
    default <R extends F> R fill(Object obj, Class<R> clazz) {
        if (obj == null) {
            return null;
        }
        return this.fill(Collections.singletonList(BeanUtil.copyProperties(obj, clazz))).stream().findFirst().orElse(null);
    }

    default <R extends F> R fill(Object obj, Class<R> clazz, P p) {
        if (obj == null) {
            return null;
        }
        return this.fill(Collections.singletonList(BeanUtil.copyProperties(obj, clazz)),p).stream().findFirst().orElse(null);
    }

    /**
     * 填充
     *
     * @param list  列表
     * @param clazz 克拉兹
     * @return {@link List}<{@link R}>
     */
    default <R extends F> List<R> fill(List<?> list, Class<R> clazz) {
        return PageUtils.toPage(list, this.fill(BeanUtil.copyToList(list, clazz)));
    }
    /**
     * 填充
     *
     * @param list 列表
     * @return {@link List}<{@link R}>
     */
    default  <R extends F> List<R> fill(List<R> list){
        return this.fill(list, (P) null);
    }

    /**
     * 核心实现
     *
     * @param list      列表
     * @param parameter 参数
     * @return {@link List}<{@link R}>
     */
    <R extends F> List<R> fill(List<R> list,P parameter);

    default <R> Map<K, List<D>> listToListMap(List<K> ids, Function<List<K>, List<R>> get, Function<D, K> getKey) {
        return this.listToListMap(ids, get, getKey, true);
    }

    default <R> Map<K, List<D>> listToListMap(List<K> ids, Function<List<K>, List<R>> get, Function<D, K> getKey, boolean isFill) {
        return this.listToListMap(ids, get, getKey, isFill,null);
    }
    default <R> Map<K, List<D>> listToListMap(List<K> ids, Function<List<K>, List<R>> get, Function<D, K> getKey, P p) {
        return this.listToListMap(ids, get, getKey, true,p);
    }
    default <R> Map<K, List<D>> listToListMap(List<K> ids, Function<List<K>, List<R>> get, Function<D, K> getKey, boolean isFill, P p) {
        List<D> ds = this.of(ids, get, isFill,p);
        return ds.stream().collect(Collectors.groupingBy(getKey));
    }

    default <R> Map<K, D> listToMap(List<K> ids, Function<List<K>, List<R>> get, Function<D, K> getKey) {
        return this.listToMap(ids, get, getKey, true);
    }

    default <R> Map<K, D> listToMap(List<K> ids, Function<List<K>, List<R>> get, Function<D, K> getKey, boolean isFill) {
        return this.listToMap(ids, get,getKey, isFill,null);
    }
    default <R> Map<K, D> listToMap(List<K> ids, Function<List<K>, List<R>> get, Function<D, K> getKey, P p) {
        return this.listToMap(ids, get,getKey, true,p);
    }
    default <R> Map<K, D> listToMap(List<K> ids, Function<List<K>, List<R>> get, Function<D, K> getKey, boolean isFill, P p) {
        List<D> ds = this.of(ids, get, isFill,p);
        return ds.stream().collect(Collectors.toMap(getKey, d -> d));
    }

    @SuppressWarnings("unchecked")
    default <R> List<D> of(List<K> ids, Function<List<K>, List<R>> get, boolean isFill, P p) {
        if (ids.isEmpty()) {
            return Collections.emptyList();
        }
        List<R> apply = get.apply(ids);
        // 获取泛型
        GenericType genericType = GenericType.forClass(this.getClass());
        Class<?> fillClass = genericType.get(BaseFill.class, "F");
        Class<?> aClass = genericType.get(BaseFill.class, "D");
        if (apply.isEmpty()) {
            return Collections.emptyList();
        }
        List<D> list;
        Object o = apply.get(0);
        // 进行自动类型转换
        if (aClass.isAssignableFrom(o.getClass())) {
            list = (List<D>) apply;
        } else {
            list = (List<D>) BeanUtil.copyToList(apply, aClass);
        }
        if (isFill && fillClass.isAssignableFrom(aClass)) {
            this.fill((List<? extends F>) list,p);
        }
        return list;
    }
}
