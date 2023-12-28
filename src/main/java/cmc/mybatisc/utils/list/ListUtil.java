package cmc.mybatisc.utils.list;


import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 列表util
 *
 * @author 程梦城
 * @version 1.0.0
 * &#064;date  2023/07/07
 */
public class ListUtil {

    /**
     * 提取
     *
     * @param list     列表
     * @param function 功能
     * @return {@link List}<{@link V}>
     */
    public static <T, V> List<V> extract(Collection<T> list, Function<T, V> function) {
        return list.stream().map(function).distinct().filter(Objects::nonNull).collect(Collectors.toList());
    }

    public static <T> T find(Collection<T> list, Function<T, Boolean> iF) {
        if (list.isEmpty()) {
            return null;
        }
        for (T t : list) {
            if (iF.apply(t)) {
                return t;
            }
        }
        return null;
    }

    /**
     * @param pares pares
     * @param o     o
     * @return {@link List}<{@link R}>
     */
    public static <T, R> List<R> of(T pares, Function<T, List<R>> o) {
        if (pares instanceof List && !((List<?>) pares).isEmpty()) {
            return o.apply(pares);
        } else if (pares instanceof Map && !((Map<?, ?>) pares).isEmpty()) {
            return o.apply(pares);
        }
        return Collections.emptyList();
    }

    /**
     * of后转map
     *
     * @param pares pares
     * @param o     o
     * @param key   钥匙
     * @return {@link Map}<{@link K},{@link R}>
     */
    public static <T, R, K> Map<K, R> ofMap(T pares, Function<T, List<R>> o, Function<R, K> key) {
        return of(pares, o).stream().collect(Collectors.toMap(key, e -> e));
    }

    /**
     * 地图组
     *
     * @param pares pares
     * @param o     o
     * @param key   钥匙
     * @return {@link Map}<{@link K},{@link List}<{@link R}>>
     */
    public static <T, R, K> Map<K, List<R>> ofMapGroup(T pares, Function<T, List<R>> o, Function<R, K> key) {
        return of(pares, o).stream().collect(Collectors.groupingBy(key));
    }

    /**
     * 为空
     *
     * @param list 列表
     * @return boolean
     */
    public static boolean isEmpty(List<?> list) {
        return list == null || list.isEmpty();
    }

    /**
     * 遍历列表
     *
     * @param list     列表
     * @param consumer 消费者
     */
    public static <T> void forEach(List<T> list, BiConsumer<T,Add<T>> consumer){
        List<T> next = list;
        while (true){
            List<T> add = new ArrayList<>();
            next.forEach(info->{
                consumer.accept(info,e->add.addAll(Arrays.asList(e)));
            });
            if(add.isEmpty()){
                return;
            }
            next = add;
            list.addAll(add);
        }
    }

    /**
     * 新增
     *
     * @author 程梦城
     * @version 1.0.0
     * &#064;date  2023/12/29
     */
    public interface Add<T> {
        void add(T... t);
    }
}
