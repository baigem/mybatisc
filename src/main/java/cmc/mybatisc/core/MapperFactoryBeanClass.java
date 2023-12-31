package cmc.mybatisc.core;

import cmc.mybatisc.annotation.*;
import cmc.mybatisc.strengthen.imp.*;
import org.apache.ibatis.session.SqlSession;
import org.mybatis.spring.mapper.MapperFactoryBean;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 映射器工厂bean类
 *
 * @author 程梦城
 * @version 1.0.0
 * &#064;date  2023/12/24
 */
@SuppressWarnings(value = {"unchecked"})
public class MapperFactoryBeanClass<T> extends MapperFactoryBean<T> {

    @SuppressWarnings("unused")
    public MapperFactoryBeanClass() {
        // intentionally empty
    }

    @SuppressWarnings("unused")
    public MapperFactoryBeanClass(Class<T> mapperInterface) {
        super.setMapperInterface(mapperInterface);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T getObject() throws Exception {
        T object = super.getObject();
        // 接口文件
        Class<T> mapperInterface = super.getMapperInterface();
        // 判断是否需要进行增强
        List<Method> collect = Arrays.stream(mapperInterface.getMethods())
                .filter(method -> method.isAnnotationPresent(FieldSelect.class) ||
                        method.isAnnotationPresent(FieldEmpty.class) ||
                        method.isAnnotationPresent(Search.class) ||
                        method.isAnnotationPresent(SoftDelete.class)
                )
                .collect(Collectors.toList());
        if (collect.isEmpty()) {
            return object;
        }
        logger.debug("Strengthen " + super.getMapperInterface().getSimpleName() + " Success");
        // 再次进行动态代理，对mybatis进行增强
        SqlSession sqlSession = getSqlSession();
        MapperProxyStrengthen<T> mapperProxyStrengthen = new MapperProxyStrengthen<>(mapperInterface, sqlSession, object);
        FieldSelectHandle fieldSelectHandle = new FieldSelectHandle(sqlSession, mapperInterface);
        FieldEmptyHandle fieldEmptyHandle = new FieldEmptyHandle(sqlSession, mapperInterface);
        SearchHandle searchHandle = new SearchHandle(sqlSession, mapperInterface);
        SoftDeleteHandle softDeleteHandle = new SoftDeleteHandle(sqlSession, mapperInterface);
        // 创建代理缓存
        collect.forEach(method -> {
            if (method.isAnnotationPresent(FieldSelect.class)) {
                mapperProxyStrengthen.getCache().put(method, fieldSelectHandle.createdProxyMethod(method));
            } else if (method.isAnnotationPresent(Search.class)) {
                mapperProxyStrengthen.getCache().put(method, searchHandle.createdProxyMethod(method));
            } else if (method.isAnnotationPresent(FieldEmpty.class)) {
                mapperProxyStrengthen.getCache().put(method, fieldEmptyHandle.createdProxyMethod(method));
            } else if (method.isAnnotationPresent(SoftDelete.class)) {
                mapperProxyStrengthen.getCache().put(method, softDeleteHandle.createdProxyMethod(method));
            }
        });
        return (T) Proxy.newProxyInstance(mapperInterface.getClassLoader(), new Class[]{mapperInterface}, mapperProxyStrengthen);
    }
}
