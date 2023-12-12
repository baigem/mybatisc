package cmc.mybatisc.core;

import cn.hutool.core.util.ReflectUtil;
import com.baomidou.mybatisplus.core.MybatisParameterHandler;
import com.baomidou.mybatisplus.core.toolkit.PluginUtils;
import com.baomidou.mybatisplus.extension.parser.JsqlParserSupport;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.*;
import org.apache.ibatis.executor.BatchExecutor;
import org.apache.ibatis.executor.ReuseExecutor;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.SqlCommandType;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.Connection;
import java.util.*;
import java.util.stream.Collectors;


/**
 * mybatis的拦截器注册器
 *
 * @author cmc
 * &#064;date  2023/02/11
 */
@Component
@Slf4j
public class MybatisInterceptorRegister extends JsqlParserSupport implements InnerInterceptor, HandlerInterceptor {
    /**
     * 拦截全部表
     */
    public static final String ALL_TABLES = "*";
    /**
     * mybatis拦截器器缓存
     */
    private final Map<Object, MybatisInterceptor> cache = new IdentityHashMap<>();
    /**
     * 本线程需要调用的缓存
     */
    private final ThreadLocal<Map<String, List<MybatisInterceptor>>> mapThreadLocal = new ThreadLocal<>();

    /**
     * mybatis拦截器寄存器
     */
    public MybatisInterceptorRegister() {
        GlobalContextHolder.registerAfterRequestRun(ThreadTask.create(() -> {
            this.removeLocal();
            return null;
        }));
    }

    /**
     * 注册器 (默认覆盖)
     *
     * @param key                表名
     * @param mybatisInterceptor 拦截器
     */
    public void register(Object key, MybatisInterceptor mybatisInterceptor) {
        this.register(key, mybatisInterceptor, true);
    }

    /**
     * 注册器
     *
     * @param key                表名
     * @param mybatisInterceptor 拦截器
     * @param cover              是否覆盖
     */
    public void register(Object key, MybatisInterceptor mybatisInterceptor, boolean cover) {
        if (cover || !this.cache.containsKey(key)) {
            this.cache.put(key, mybatisInterceptor);
        }
    }

    /**
     * 哈希
     *
     * @param key 钥匙
     * @return boolean
     */
    public boolean hash(Object key) {
        return this.cache.containsKey(key);
    }

    /**
     * 获取
     *
     * @param key 钥匙
     * @return {@link MybatisInterceptor}
     */
    public MybatisInterceptor get(Object key) {
        return this.cache.get(key);
    }

    /**
     * 使用某个注册器注册的拦截器
     *
     * @param key 标识符
     */
    public void useRegister(String tableName, MybatisInterceptor key) {
        Map<String, List<MybatisInterceptor>> stringListMap = this.mapThreadLocal.get();
        if (stringListMap == null) {
            stringListMap = new HashMap<>();
            this.mapThreadLocal.set(stringListMap);
        }
        List<MybatisInterceptor> objects = stringListMap.get(tableName);
        if (objects == null) {
            stringListMap.put(tableName, new ArrayList<>());
            objects = stringListMap.get(tableName);
        }
        if (!objects.contains(key)) {
            objects.add(key);
        }
    }


    /**
     * 添加过滤器
     *
     * @param tableNames 表名
     * @param key        数据过滤器
     */
    public void useRegister(List<String> tableNames, MybatisInterceptor key) {
        for (String tableName : tableNames) {
            this.useRegister(tableName, key);
        }
    }

    public void useRegister(String[] tableNames, MybatisInterceptor key) {
        this.useRegister(Arrays.stream(tableNames).collect(Collectors.toList()), key);
    }


    @Override
    public String parserSingle(String sql, Object obj) {
        return super.parserSingle(sql, obj);
    }

    /**
     * 删除正在使用的key
     *
     * @param from 表名
     * @param key  数据过滤器
     */
    public void removeUseRegister(String[] from, MybatisInterceptor key) {
        Map<String, List<MybatisInterceptor>> stringListMap = this.mapThreadLocal.get();
        if (stringListMap == null) {
            stringListMap = new HashMap<>();
            this.mapThreadLocal.set(stringListMap);
        }
        for (String s : from) {
            List<MybatisInterceptor> objects = stringListMap.get(s);
            if (objects != null) {
                objects.remove(key);
            }
        }
    }

    /**
     * 拦截
     */
    protected void intercept(StatementHandlerReflex statementHandlerReflex) {
        try {
            Map<String, List<MybatisInterceptor>> dataFilterMap = this.mapThreadLocal.get();
            if (dataFilterMap == null) {
                return;
            }
            List<MybatisInterceptor> dataFilters = this.getRunMysqlInterceptors(statementHandlerReflex.getTableName());
            // 添加全部表的拦截
            dataFilters.forEach(dataFilterCore -> {
                if (statementHandlerReflex.getSqlCommandType() == SqlCommandType.SELECT) {
                    FromItem fromItem = dataFilterCore.getFromItem(statementHandlerReflex.getTable(), statementHandlerReflex.getMs().getId());
                    if (fromItem != null) {
                        statementHandlerReflex.setFromItem(fromItem);
                    }
                }
                List<Join> joins = dataFilterCore.getJoins(Optional.ofNullable(statementHandlerReflex.getJoins()).orElse(new ArrayList<>()), statementHandlerReflex.getMs().getId());
                if (joins != null && !joins.isEmpty()) {
                    statementHandlerReflex.setJoins(joins);
                }
                Expression where = dataFilterCore.getWhere(statementHandlerReflex, statementHandlerReflex.getWhere(), statementHandlerReflex.getMs().getId());
                if (where != null) {
                    statementHandlerReflex.setWhere(where);
                }
            });
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }


    /**
     * 查询
     * 设置 where 条件
     *
     * @param statementHandlerReflex 语句处理程序反射
     */
    protected void query(StatementHandlerReflex statementHandlerReflex) {
        this.intercept(statementHandlerReflex);
    }


    /**
     * {@link StatementHandler#getBoundSql()} 操作前置处理
     * <p>
     * 只有 {@link BatchExecutor} 和 {@link ReuseExecutor} 才会调用到这个方法
     *
     * @param sh StatementHandler(可能是代理对象)
     */
    @Override
    public void beforePrepare(StatementHandler sh, Connection connection, Integer transactionTimeout) {
        MybatisParameterHandler parameterHandler = (MybatisParameterHandler) sh.getParameterHandler();
        SqlCommandType sqlCommandType = (SqlCommandType) ReflectUtil.getFieldValue(parameterHandler, "sqlCommandType");
        // 新增的删除不拦截
        if (sqlCommandType == SqlCommandType.INSERT || sqlCommandType == SqlCommandType.DELETE) {
            return;
        }
        StatementHandlerReflex statementHandlerReflex = new StatementHandlerReflex(sh);
        PluginUtils.MPBoundSql mpBs = PluginUtils.mpBoundSql(sh.getBoundSql());
        if (statementHandlerReflex.getStatement() != null) {
            switch (sqlCommandType) {
                case UPDATE: {
                    this.intercept(statementHandlerReflex);
                    break;
                }
                case SELECT: {
                    if (statementHandlerReflex.getCurrentStatement() instanceof Select) {
                        SelectBody selectBody = ((Select) statementHandlerReflex.getCurrentStatement()).getSelectBody();
                        if (selectBody instanceof SetOperationList) {
                            SetOperationList setOperationList = (SetOperationList) selectBody;
                            List<SelectBody> selectBodyList = setOperationList.getSelects();
                            // 这里有bug，记得修复
                            selectBodyList.forEach(s -> {
                                statementHandlerReflex.decompose((PlainSelect) s);
                                this.query(statementHandlerReflex);
                            });
                        }
                    } else {
                        this.query(statementHandlerReflex);
                    }
                }
            }
            mpBs.sql(statementHandlerReflex.getStatement().toString());
            // 执行预加载
            statementHandlerReflex.pretreatment();
        } else {
            // 此处需要优化
            mpBs.sql(statementHandlerReflex.getBoundSql().getSql());
        }
    }


    /**
     * 获取运行mysql拦截器
     *
     * @return {@link List}<{@link MybatisInterceptor}>
     */
    public List<MybatisInterceptor> getRunMysqlInterceptors(String tableName) {
        Map<String, List<MybatisInterceptor>> dataFilterMap = this.mapThreadLocal.get();
        if (dataFilterMap == null || dataFilterMap.isEmpty()) {
            return new ArrayList<>();
        }
        List<MybatisInterceptor> dataFilters = Optional.ofNullable(dataFilterMap.get(tableName)).orElse(new ArrayList<>()).stream().filter(Objects::nonNull).collect(Collectors.toList());
        // 添加全部表的拦截
        dataFilters.addAll(Optional.ofNullable(dataFilterMap.get(ALL_TABLES)).orElse(new ArrayList<>()).stream().filter(Objects::nonNull).collect(Collectors.toList()));
        return dataFilters.stream().distinct().collect(Collectors.toList());
    }


    /**
     * 每次线程结束都清空一下临时存储
     *
     * @param request  current HTTP request
     * @param response current HTTP response
     * @param handler  the handler (or {@link HandlerMethod}) that started asynchronous
     *                 execution, for type and/or instance examination
     * @param ex       any exception thrown on handler execution, if any; this does not
     *                 include exceptions that have been handled through an exception resolver
     */
    @Override
    public void afterCompletion(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull
    Object handler, Exception ex) {
        // 在每个请求处理完毕之后执行钩子函数
        this.removeLocal();
    }


    public void removeLocal() {
        this.mapThreadLocal.remove();
    }

}
