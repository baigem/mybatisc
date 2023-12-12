package cmc.mybatisc.core;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.Join;

import java.util.List;

/**
 * mybatis拦截器
 */
public interface MybatisInterceptor {

    /**
     * 表级别
     */
    Table getFromItem(Table table, String mappedStatementId);

    /**
     * 连表级别
     */
    List<Join> getJoins(List<Join> joins, String mappedStatementId);

    /**
     * 条件级别
     */
    Expression getWhere(StatementHandlerReflex statementHandlerReflex, Expression where, String mappedStatementId);

}
