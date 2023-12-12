package cmc.mybatisc.core;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Component;

import java.sql.Connection;

/**
 * 加拦截器
 *
 * @author cmc
 * &#064;date  2023/02/10
 */
@Intercepts(
        {
                @Signature(type = org.apache.ibatis.executor.statement.StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class}),
                @Signature(type = org.apache.ibatis.executor.statement.StatementHandler.class, method = "getBoundSql", args = {}),
                @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
                @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class}),
                @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
        }
)
@Component
public class PlusInterceptor extends MybatisPlusInterceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        return super.intercept(invocation);
    }
}

