package cmc.mybatisc.datafilter.core;


import cmc.mybatisc.core.MybatisInterceptor;
import cmc.mybatisc.core.MybatisInterceptorRegister;
import cmc.mybatisc.core.StatementHandlerReflex;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.Join;

import java.util.List;

@Slf4j
public class DataFilterCoreProxy implements MybatisInterceptor {
    /**
     * 数据权限注入
     */
    protected MybatisInterceptorRegister mybatisInterceptorRegister;

    /**
     * 数据过滤器参数
     */
    @Setter
    protected DataFilterOption dataFilterOption;
    /**
     * 是否预编译
     */
    @Setter
    protected boolean precompile = false;

    public DataFilterCoreProxy(MybatisInterceptorRegister mybatisInterceptorRegister) {
        this.mybatisInterceptorRegister = mybatisInterceptorRegister;
    }

    @Override
    public Table getFromItem(Table table, String mappedStatementId) {
        dataFilterOption.setFrom(table);
        // 默认是对主表进行限权
        dataFilterOption.getLeftSql().setTable(table);
        dataFilterOption.setDataScopeTable(table);
        return table;
    }

    /**
     * @param joins             原SQL join 条件表达式
     * @param mappedStatementId Mapper接口方法ID
     */
    @Override
    public List<Join> getJoins(List<Join> joins, String mappedStatementId) {
        // 注解没有join直接退出方法
        if (dataFilterOption.getDataSource().getJoin().isEmpty()) {
            return joins;
        }
        return dataFilterOption.mergeJoin(joins);
    }

    @Override
    public Expression getWhere(StatementHandlerReflex statementHandlerReflex, Expression where, String mappedStatementId) {
        // 创建缓存
        DataFilterCoreCache dataFilterCoreCache = new DataFilterCoreCache(dataFilterOption, precompile);
        // 进行注册
        mybatisInterceptorRegister.register(dataFilterOption.getDataSource().getAnnotation(), dataFilterCoreCache);
        // 删除临时过滤器
        mybatisInterceptorRegister.removeUseRegister(dataFilterOption.getDataSource().getFrom(), this);
        // 新增缓存
        mybatisInterceptorRegister.useRegister(dataFilterOption.getDataSource().getFrom(), dataFilterCoreCache);
        return dataFilterCoreCache.getWhere(statementHandlerReflex, where, null);
    }
}
