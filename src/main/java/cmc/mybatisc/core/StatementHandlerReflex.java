package cmc.mybatisc.core;

import cn.hutool.core.util.ReflectUtil;
import com.baomidou.mybatisplus.core.MybatisParameterHandler;
import com.baomidou.mybatisplus.core.toolkit.ExceptionUtils;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.statement.update.Update;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Invocation;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 语句处理程序反射
 *
 * @author cmc
 * &#064;date  2023/02/09
 */
@Data
@Slf4j
@SuppressWarnings({"unchecked"})
public class StatementHandlerReflex {
    /**
     * sql命令类型
     */
    private SqlCommandType sqlCommandType;
    /**
     * 执行人
     */
    private Executor executor;
    /**
     * 查询主体
     */
    private Statement statement;
    /**
     * 当前声明
     */
    private Object currentStatement;
    /**
     * 表名
     */
    private String tableName;
    /**
     * 映射语句
     */
    private MappedStatement ms;
    /**
     * 参数
     */
    private Map<Object, Object> parameterObject;
    /**
     * 参数映射
     */
    private List<ParameterMapping> parameterMappings;
    /**
     * 参数名称
     */
    private List<String> parameterMappingName;
    /**
     * 绑定sql
     */
    private BoundSql boundSql;
    /**
     * 预编译列表
     */
    private Map<String, String> precompileMap = new HashMap<>();
    /**
     * 缓存执行器
     */
    private Invocation cachingExecutor;
    /**
     * 路由语句处理程序
     */
    private Invocation routingStatementHandler;

    private Field table;
    /**
     * 表
     */
    private Field fromItem;

    /**
     * 连接
     */
    private Field joins;

    /**
     * 哪里
     */
    private Field where;

    public StatementHandlerReflex(StatementHandler statementHandler) {
        this.decompose(statementHandler);
    }

    public StatementHandlerReflex(MappedStatement ms, BoundSql boundSql, Object parameter) {
        this.ms = ms;
        this.boundSql = boundSql;
        try {
            this.statement = CCJSqlParserUtil.parse(boundSql.getSql().replaceAll("\n", ""));
            this.currentStatement = this.statement;
        } catch (JSQLParserException e) {
            log.error("Failed to process, Error SQL: " + boundSql.getSql());
            throw ExceptionUtils.mpe("Failed to process, Error SQL: %s", e.getCause(), boundSql.getSql());
        }
        this.initStatement();
        this.handle(parameter);
    }

    public static PlainSelect getPlainSelect(PlainSelect plainSelect) {
        FromItem fromItem = plainSelect.getFromItem();
        if (fromItem instanceof Table) {
            return plainSelect;
        } else if (fromItem instanceof SubSelect) {
            SelectBody selectBody = ((SubSelect) fromItem).getSelectBody();
            if (selectBody instanceof PlainSelect) {
                return getPlainSelect((PlainSelect) selectBody);
            } else {
                throw new RuntimeException("未知类型，无法获取内部PlainSelect对象");
            }
        }
        throw new RuntimeException("未知类型，无法获取内部PlainSelect对象");
    }

    public void decompose(PlainSelect s) {
        this.table = ReflectUtil.getField(s.getClass(), "fromItem");
        this.fromItem = ReflectUtil.getField(s.getClass(), "fromItem");
        this.joins = ReflectUtil.getField(s.getClass(), "joins");
        this.where = ReflectUtil.getField(s.getClass(), "where");
        this.tableName = this.getTableName(s);
    }

    public void decompose(StatementHandler statementHandler) {
        MybatisParameterHandler parameterHandler = (MybatisParameterHandler) statementHandler.getParameterHandler();
        this.ms = (MappedStatement) ReflectUtil.getFieldValue(parameterHandler, "mappedStatement");
        this.boundSql = statementHandler.getBoundSql();
        try {
            this.statement = CCJSqlParserUtil.parse(this.boundSql.getSql().replaceAll("#[^{].*?[\n\r]|\n", ""));
            this.currentStatement = this.statement;
            this.initStatement();
            this.handle(statementHandler.getParameterHandler().getParameterObject());
        } catch (JSQLParserException e) {
            // 如果只是记数就不进行警报
            if (!this.boundSql.getSql().startsWith("select count(")) {
                log.error("Failed to process, Error SQL: " + this.boundSql.getSql());
//                throw ExceptionUtils.mpe("Failed to process, Error SQL: %s", e.getCause(), this.boundSql.getSql());
            }
        }
    }

    /**
     * 初始化声明
     */
    public void initStatement() {
        if (this.statement instanceof Insert) {
            this.sqlCommandType = SqlCommandType.INSERT;
        } else if (this.statement instanceof Select) {
            this.sqlCommandType = SqlCommandType.SELECT;
            SelectBody selectBody = ((Select) this.statement).getSelectBody();
            if (selectBody instanceof PlainSelect) {
                this.currentStatement = selectBody;
                this.table = ReflectUtil.getField(selectBody.getClass(), "fromItem");
                this.fromItem = ReflectUtil.getField(selectBody.getClass(), "fromItem");
                this.joins = ReflectUtil.getField(selectBody.getClass(), "joins");
                this.where = ReflectUtil.getField(selectBody.getClass(), "where");
                this.tableName = this.getTableName((PlainSelect) selectBody);
            }
        } else if (this.statement instanceof Update) {
            this.sqlCommandType = SqlCommandType.UPDATE;
            this.table = ReflectUtil.getField(this.statement.getClass(), "table");
            this.fromItem = ReflectUtil.getField(this.statement.getClass(), "fromItem");
            this.joins = ReflectUtil.getField(this.statement.getClass(), "joins");
            this.where = ReflectUtil.getField(this.statement.getClass(), "where");
            this.tableName = ((Update) this.statement).getTable().getName();
        } else if (this.statement instanceof Delete) {
            this.sqlCommandType = SqlCommandType.DELETE;
        }
    }

    /**
     * 手柄
     *
     * @param parameter 参数
     */
    public void handle(Object parameter) {
        // 处理入参
        this.handleParameterObject(parameter);
        this.parameterMappings = this.boundSql.getParameterMappings();
        // 空的是列表后面添加数据会报异常
        if (this.parameterMappings.getClass().getName().endsWith("UnmodifiableRandomAccessList") || this.parameterMappings.isEmpty()) {
            this.parameterMappings = new ArrayList<>(this.parameterMappings);
            ReflectUtil.setFieldValue(this.boundSql, "parameterMappings", this.parameterMappings);
        }
        this.parameterMappingName = this.parameterMappings.stream().map(ParameterMapping::getProperty).collect(Collectors.toList());
    }

    public void handleParameterObject(Object obj) {
        if (obj instanceof Map) {
            this.parameterObject = (Map<Object, Object>) obj;
        } else {
            // 单参数
            this.parameterObject = new HashMap<>(2);
            if (this.boundSql.getParameterMappings().isEmpty()) {
                this.parameterObject.put("arg0", obj);
                this.parameterObject.put("param0", obj);
            } else {
                this.parameterObject.put(this.boundSql.getParameterMappings().get(0).getProperty(), obj);
            }
        }
    }

    /**
     * 添加参数映射
     *
     * @param parameter 参数
     */
    @SneakyThrows
    public void addParameterMapping(int index, ParameterMapping parameter) {
        if (this.parameterObject == null) {
            this.parameterObject = new HashMap<>();
            // 进行反射
            ReflectUtil.setFieldValue(this.boundSql, "parameterObject", this.parameterObject);
        }
        this.parameterMappings.add(index, parameter);
    }

    /**
     * 添加参数
     *
     * @param key          钥匙
     * @param value        值
     * @param parameterMap 参数映射
     * @param index        索引
     */
    @SneakyThrows
    public void addParameter(String key, Object value, boolean parameterMap, int index) {
        if (this.parameterMappings == null) {
            this.parameterMappings = new ArrayList<>();
            // 进行反射
            ReflectUtil.setFieldValue(this.boundSql, "parameterMappings", this.parameterMappings);
        }
        // 是否需要进行填充
        if (parameterMap) {
            ParameterMapping build = new ParameterMapping.Builder(this.ms.getConfiguration(), key, value.getClass()).build();
            this.addParameterMapping(index, build);
        }
        this.parameterObject.put(key, value);
    }

    /**
     * 添加预编译
     * 添加表达式列表
     *
     * @param expression 表达
     */
    public void addPrecompile(String key, String expression) {
        this.precompileMap.put(key, expression);
    }

    /**
     * 预处理
     */
    @SneakyThrows
    public void pretreatment() {
        if (this.precompileMap.isEmpty()) {
            return;
        }
        AtomicInteger index;
        // 判断分页插件导致的bug
        if (this.sqlCommandType == SqlCommandType.SELECT) {
            index = new AtomicInteger(0);
        } else {
            index = new AtomicInteger(this.parameterMappings.size());
        }
        this.precompileMap.forEach((key, value) -> {
            int index1 = this.parameterMappingName.indexOf(key);
            if (index1 != -1) {
                index.set(index1);
            } else {
                index1 = index.get();
            }
            // 位置
            this.addParameter(key, value, !this.parameterMappingName.contains(key), index1);
            index.getAndIncrement();
        });
        // 清空
        this.precompileMap.clear();
    }

    public String getTableName(PlainSelect plainSelect) {
        FromItem fromItem = StatementHandlerReflex.getPlainSelect(plainSelect).getFromItem();
        if (fromItem instanceof Table) {
            return ((Table) fromItem).getASTNode().jjtGetFirstToken().image;
        } else if (fromItem instanceof SubSelect) {
            SelectBody selectBody = ((SubSelect) fromItem).getSelectBody();
            if (selectBody instanceof PlainSelect) {
                return this.getTableName((PlainSelect) selectBody);
            } else {
                throw new RuntimeException("未知类型，无法获取查询表名");
            }
        }
        throw new RuntimeException("未知类型，无法获取查询表名");
    }

    public Table getTable() {
        return (Table) ReflectUtil.getFieldValue(this.currentStatement, this.table);
    }

    public void setTable(Table table) {
        ReflectUtil.setFieldValue(this.currentStatement, this.table, table);
    }

    public FromItem getFromItem() {
        return (FromItem) ReflectUtil.getFieldValue(this.currentStatement, this.fromItem);
    }

    public void setFromItem(FromItem fromItem) {
        ReflectUtil.setFieldValue(this.currentStatement, this.fromItem, fromItem);
    }

    public List<Join> getJoins() {
        return (List<Join>) ReflectUtil.getFieldValue(this.currentStatement, this.joins);
    }

    public void setJoins(List<Join> joins) {
        ReflectUtil.setFieldValue(this.currentStatement, this.joins, joins);
    }

    public Expression getWhere() {
        return (Expression) ReflectUtil.getFieldValue(this.currentStatement, this.where);
    }

    public void setWhere(Expression where) {
        ReflectUtil.setFieldValue(this.currentStatement, this.where, where);
    }
}
