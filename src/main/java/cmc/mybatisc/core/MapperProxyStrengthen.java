package cmc.mybatisc.core;

import lombok.Getter;
import org.apache.ibatis.session.SqlSession;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * sql查询
 *
 * @author cmc
 * @date 2023/05/24
 */
public class MapperProxyStrengthen implements InvocationHandler {
    @Getter
    private final Class<?> mapper;
    @Getter
    private final Object target;
    @Getter
    private final SqlSession sqlSession;
    @Getter
    private final Map<Class<?>, String> resultMap = new HashMap<>();
    /**
     * 创建缓存
     */
    private final Map<Method, Function<Object[], Object>> cache = new LinkedHashMap<>();

    public MapperProxyStrengthen(Class<?> mapper, SqlSession sqlSession, Object object) {
        this.sqlSession = sqlSession;
        this.target = object;
        this.mapper = mapper;
    }


    /**
     * 代理
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (cache.containsKey(method)) {
            return cache.get(method).apply(args);
        }
        return method.invoke(target, args);
    }


    public Map<Method, Function<Object[], Object>> getCache() {
        return cache;
    }
}
