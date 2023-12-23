package cmc.mybatisc.core;

import com.baomidou.mybatisplus.core.override.MybatisMapperProxy;
import lombok.Getter;
import org.apache.ibatis.session.SqlSession;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Collections;
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
@Getter
public class MapperProxyStrengthen<T> extends MybatisMapperProxy<T> implements InvocationHandler {
    private final Class<T> mapper;
    private final Object target;
    private final SqlSession sqlSession;
    private final Map<Class<?>, String> resultMap = new HashMap<>();
    /**
     * 创建缓存
     */
    private final Map<Method, Function<Object[], Object>> cache = new LinkedHashMap<>();

    public MapperProxyStrengthen(Class<T> mapper, SqlSession sqlSession, Object object) {
        super(sqlSession,mapper, Collections.emptyMap());
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


}
