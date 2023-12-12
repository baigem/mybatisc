package cmc.mybatisc.datafilter.core;


import cmc.mybatisc.core.MybatisInterceptor;
import cmc.mybatisc.core.StatementHandlerReflex;
import lombok.Data;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.Join;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据过滤核心
 */
@Data
public class DataFilterCoreCache implements MybatisInterceptor {
    /**
     * 数据过滤器参数
     */
    protected DataFilterOption dataFilterOption;
    /**
     * 是否预编译
     */
    protected boolean precompile;

    /**
     * 数据过滤器
     */
    public DataFilterCoreCache(DataFilterOption dataFilterOption, boolean precompile) {
        this.dataFilterOption = dataFilterOption;
        this.precompile = precompile;
    }

    /**
     * 表级别
     */
    public Table getFromItem(Table table, String mappedStatementId) {
        return table;
    }

    /**
     * 连表级别
     */
    public List<Join> getJoins(List<Join> joins, String mappedStatementId) {
        if (joins != null) {
            // 过滤
            return dataFilterOption.mergeJoin(joins);
        } else {
            return new ArrayList<>(dataFilterOption.getJoins().values());
        }
    }

    /**
     * 条件级别
     */
    public Expression getWhere(StatementHandlerReflex statementHandlerReflex, Expression where, String mappedStatementId) {
        return dataFilterOption.mergeWhere(statementHandlerReflex, where, precompile);
    }
}
