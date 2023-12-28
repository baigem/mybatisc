package cmc.mybatisc.base.model;

import lombok.NoArgsConstructor;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * 地图列表
 *
 * @author 程梦城
 * @version 1.0.0
 * &#064;date  2023/12/29
 */
@NoArgsConstructor
public class MapList<K,T> extends ArrayList<T> {
    private final Map<K,T> map = new HashMap<>();
    private Function<T,K> keyFunction = e->null;

    public MapList(Function<T,K> function) {
        keyFunction = function;
    }

    @Override
    public boolean add(T t) {
        map.put(keyFunction.apply(t),t);
        return super.add(t);
    }

    @Override
    public void add(int index, T element) {
        map.put(keyFunction.apply(element),element);
        super.add(index, element);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        c.forEach(info->map.put(keyFunction.apply(info),info));
        return super.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        c.forEach(info->map.put(keyFunction.apply(info),info));
        return super.addAll(index, c);
    }


    /**
     * 获取
     *
     * @param k k
     * @return {@link T}
     */
    public T get(K k){
        return map.get(k);
    }

    public MapList<K,T> filter(Predicate<? super T> predicate){
        MapList<K,T> mapList = new MapList<>(this.keyFunction);
        for (T t : this) {
            if(predicate.test(t)){
                mapList.add(t);
            }
        }
        return mapList;
    }

    public MapList<K,T> distinct(Function<T,Object> function){
        List<Object> list = new ArrayList<>();
        MapList<K,T> mapList = new MapList<>(this.keyFunction);
        for (T t : this) {
            Object apply = function.apply(t);
            if(list.contains(apply)){
                continue;
            }
            mapList.add(t);
            list.add(apply);
        }
        return mapList;
    }

    /**
     * 要映射
     *
     * @return {@link Map}<{@link K}, {@link T}>
     */
    public Map<K, T> toMap(){
        return map;
    }
}
