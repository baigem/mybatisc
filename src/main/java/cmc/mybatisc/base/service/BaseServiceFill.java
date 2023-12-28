package cmc.mybatisc.base.service;

import cmc.mybatisc.parser.EntityParser;
import cmc.mybatisc.utils.reflect.GenericType;
import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.beans.factory.InitializingBean;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 基本服务填充
 *
 * @author 程梦城
 * @version 1.0.0
 * &#064;date  2023/12/28
 */
@SuppressWarnings("unused")
public interface BaseServiceFill<M extends BaseMapper<E>, E,F,D,P,K extends Serializable> extends BaseService<M,E>, BaseFill<F,D,P,K>, InitializingBean {
    @SuppressWarnings("rawtypes")
    Map<BaseServiceFill,EntityParser> BASE_SERVICE_FILL_ENTITY_PARSER_MAP = new HashMap<>();
    Map<BaseServiceFill,GenericType> BASE_SERVICE_FILL_GENERIC_TYPE_MAP = new HashMap<>();
    /**
     * 获取数据映射
     *
     * @param ids 身份证
     * @param p   p
     * @return {@link Map}<{@link K}, {@link E}>
     */
    default Map<K, D> getDataMap(List<K> ids, P p){
        EntityParser entityParser = BASE_SERVICE_FILL_ENTITY_PARSER_MAP.get(this);
        Class<?> aClass = BASE_SERVICE_FILL_GENERIC_TYPE_MAP.get(this).get(BaseFill.class, "D");
        Field declaredField = aClass.getDeclaredFields()[0];
        declaredField.setAccessible(true);
        return this.listToMap(ids, this::listByIds, (e)-> {
            try {
                return (K) declaredField.get(e);
            } catch (IllegalAccessException ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    /**
     * 获取数据列表映射
     *
     * @param mainids mainids
     * @param p       p
     * @return {@link Map}<{@link K}, {@link List}<{@link D}>>
     */

    default Map<K, List<D>> getDataListMap(List<K> mainids, P p) {
        EntityParser entityParser = BASE_SERVICE_FILL_ENTITY_PARSER_MAP.get(this);
        Class<?> aClass = BASE_SERVICE_FILL_GENERIC_TYPE_MAP.get(this).get(BaseFill.class, "D");
        Field declaredField = aClass.getDeclaredFields()[0];
        declaredField.setAccessible(true);
        return this.listToListMap(mainids, this::listByIds, (e)-> {
            try {
                return (K) declaredField.get(e);
            } catch (IllegalAccessException ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    /**
     * 填充
     *
     * @param list      列表
     * @param parameter 参数
     * @return {@link List}<{@link R}>
     */
    default  <R extends F> List<R> fill(List<R> list,P parameter){
        return list;
    }

    /**
     * 属性设置后进行初始化
     */
    default void afterPropertiesSet(){
        GenericType genericType = GenericType.forClass(this.getClass());
        BASE_SERVICE_FILL_GENERIC_TYPE_MAP.put(this,genericType);
        BASE_SERVICE_FILL_ENTITY_PARSER_MAP.put(this,EntityParser.computeIfAbsent(genericType.get(1)));
    }

    /**
     * 长密钥
     *
     * @author 程梦城
     * @version 1.0.0
     * &#064;date  2023/12/29
     */
    interface KLong<M extends BaseMapper<E>, E,D,F,P> extends BaseServiceFill<M,E,D,F,P, java.lang.Long>{

    }
}
