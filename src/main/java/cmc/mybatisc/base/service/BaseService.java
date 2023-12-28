package cmc.mybatisc.base.service;

import cmc.mybatisc.utils.list.ListUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * iservice加强
 *
 * @author 程梦城
 * @version 1.0.0
 * &#064;date  2023/12/24
 */
public interface BaseService<B extends BaseMapper<T>, T> extends IService<T> {
    @Override
    B getBaseMapper();

    default <A, E, M, K> boolean fullUpdate(List<A> list, Function<A, M> getSuperKey, Function<A, K> getListKey, Function<List<M>, List<E>> getAll, Function<E, K> getKey, FullUpdateOftenHandler<A, K> handler) {
        return this.fullUpdateList(list, getSuperKey, getListKey, getAll, getKey, (a, u, r) -> handler.handler(a, u, ListUtil.extract(r, getKey)), false);
    }

    default <A, E, M, K> boolean fullUpdate(List<A> list, Function<A, M> getSuperKey, Function<A, K> getListKey, Function<List<M>, List<E>> getAll, Function<E, K> getKey, FullUpdateOftenHandler<A, K> handler, boolean isGroup) {
        return this.fullUpdateList(list, getSuperKey, getListKey, getAll, getKey, (a, u, r) -> handler.handler(a, u, ListUtil.extract(r, getKey)), isGroup);
    }

    /**
     * 全量更新
     *
     * @param list        列表
     * @param getSuperKey 获取列表数据的父id
     * @param getListKey  获取数据的主键
     * @param getAll      获取数据参数是getMainKey获取到的集合
     * @param handler     处理程序
     * @return boolean
     */
    default <A, E, M, K> boolean fullUpdateList(List<A> list, Function<A, M> getSuperKey, Function<A, K> getListKey, Function<List<M>, List<E>> getAll, Function<E, K> getKey, FullUpdateHandler<A, E> handler, boolean isGroup) {
        if (list.isEmpty()) {
            return true;
        }
        // 新增的数据
        List<A> addData = new ArrayList<>();
        // 修改的数据
        List<A> updateData = new ArrayList<>();

        // 订单详情id
        List<M> ids = list.stream().map(e -> {
            M apply = getSuperKey.apply(e);
            if (apply == null) {
                throw new RuntimeException("全量更新，子数据中缺少父级id:" + JSON.toJSONString(e));
            }
            return apply;
        }).distinct().collect(Collectors.toList());
        // 已存在的数据
        List<E> all = getAll.apply(ids);
        List<K> keys = ListUtil.extract(all, getKey);
        // 遍历处理
        list.forEach(info -> {
            K apply = getListKey.apply(info);
            if (apply == null) {
                addData.add(info);
            }
            keys.remove(apply);
            updateData.add(info);
        });
        List<E> deleteData = all.stream().filter(e -> keys.contains(getKey.apply(e))).collect(Collectors.toList());
        if (isGroup) {
            Map<M, List<A>> addMap = addData.stream().collect(Collectors.groupingBy(getSuperKey));
            Map<M, List<A>> updateMap = updateData.stream().collect(Collectors.groupingBy(getSuperKey));
            Map<K, List<E>> deleteMap = deleteData.stream().collect(Collectors.groupingBy(getKey));
            for (M id : ids) {
                handler.handler(Optional.ofNullable(addMap.get(id)).orElse(Collections.emptyList()), Optional.ofNullable(updateMap.get(id)).orElse(Collections.emptyList()), Optional.ofNullable(deleteMap.get(id)).orElse(Collections.emptyList()));
            }
        } else {
            handler.handler(addData, updateData, deleteData);
        }
        return true;
    }

    /**
     * 完整更新处理程序
     *
     * @author 程梦城
     * @version 1.0.0
     * &#064;date  2023/07/07
     */
    interface FullUpdateOftenHandler<T, K> {
        /**
         * 处理程序
         *
         * @param addList    添加列表
         * @param updateList 更新列表
         * @param removtList removt列表
         */
        void handler(List<T> addList, List<T> updateList, List<K> removtList);
    }

    /**
     * 完整更新处理程序
     *
     * @author 程梦城
     * @version 1.0.0
     * &#064;date  2023/07/07
     */
    interface FullUpdateHandler<T, E> {
        /**
         * 处理程序
         *
         * @param addList    添加列表
         * @param updateList 更新列表
         * @param removtList removt列表
         */
        void handler(List<T> addList, List<T> updateList, List<E> removtList);
    }
}
