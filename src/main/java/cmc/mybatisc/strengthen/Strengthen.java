package cmc.mybatisc.strengthen;

import cmc.mybatisc.parser.SqlParser;

import java.lang.reflect.Method;
import java.util.function.Function;

/**
 * 加强
 *
 * @author cmc
 * @date 2023/05/25
 */
public interface Strengthen {
    /**
     * 创建代理方法
     *
     * @param method 方法
     * @return {@link Function}<{@link Object[]}, {@link Object}>
     */
    Function<Object[], Object> createdProxyMethod(Method method);

    /**
     * 创建sql
     *
     * @param method 方法
     * @return {@link String}
     */
    SqlParser createdSql(Method method);
}
