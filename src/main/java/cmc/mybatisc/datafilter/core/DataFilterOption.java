package cmc.mybatisc.datafilter.core;


import cmc.mybatisc.core.StatementHandlerReflex;
import cmc.mybatisc.model.DelFlag;
import cn.hutool.core.util.ReflectUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
import net.sf.jsqlparser.expression.operators.relational.RegExpMatchOperatorType;
import net.sf.jsqlparser.expression.operators.relational.RegExpMySQLOperator;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;
import org.apache.ibatis.mapping.SqlCommandType;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Supplier;

/**
 * 数据过滤器参数选项
 *
 * @author cmc
 * &#064;date  2023/02/04
 */
@Data
@EqualsAndHashCode
@SuppressWarnings(value = {"unchecked"})
public class DataFilterOption {
    /**
     * 正则匹配
     */
    public static final String REGEXP = "regexp";
    /**
     * 完全匹配
     */
    public static final String TO = "to";
    /**
     * 模糊查询
     */
    public static final String LINK = "link";

    /**
     * 数据源
     */
    private final DataScopeSourceData dataSource;

    /**
     * 查询的表
     */
    private Table from;

    /**
     * 数据范围表
     * 需要进行限权的表
     */
    private Table dataScopeTable;

    /**
     * 连表查询, 只是注解部分
     */
    private LinkedHashMap<String, Join> joins;
    /**
     * 左sql
     */
    private Column leftSql;
    /**
     * where缓存
     */
    private Expression whereCache;
    /**
     * 数据范围值
     */
    private Supplier<String> dataScopeValue;
    /**
     * 默认正则查询
     */
    private String queryMode;

    public DataFilterOption(Supplier<String> dataScopeValue, String queryMode, DataScopeSourceData dataScopeSourceData) {
        this.dataScopeValue = dataScopeValue;
        this.queryMode = queryMode;
        this.dataSource = dataScopeSourceData;
        this.leftSql = new Column(dataScopeSourceData.getField());
    }

    /**
     * 获取动态sql
     *
     * @param precompile 预编译
     * @return {@link BinaryExpression}
     */
    public BinaryExpression dynamicWhere(StatementHandlerReflex statementHandlerReflex, Boolean precompile) {
        Expression value;
        // 判断是否需要预编译
        if (precompile) {
            value = new JdbcParameter();
            statementHandlerReflex.addPrecompile("dataPermissions", this.dataScopeValue.get());
        } else {
            value = new StringValue(this.dataScopeValue.get());
        }
        switch (this.queryMode) {
            case REGEXP: {
                RegExpMySQLOperator itemsList = new RegExpMySQLOperator(RegExpMatchOperatorType.MATCH_CASEINSENSITIVE);
                itemsList.setLeftExpression(this.leftSql);
                itemsList.setRightExpression(value);
                return itemsList;
            }
            case TO: {
                return new EqualsTo(this.leftSql, value);
            }
            case LINK: {
                LikeExpression likeExpression = new LikeExpression();
                likeExpression.setLeftExpression(this.leftSql);
                likeExpression.setRightExpression(value);
                return likeExpression;
            }
            default: {
                throw new IllegalArgumentException("unknown of query mode " + this.queryMode);
            }
        }
    }

    /**
     * 合并联接
     *
     * @param joins 连接
     * @return {@link List}<{@link Join}>
     */
    public List<Join> mergeJoin(List<Join> joins) {
        if (this.joins == null) {
            this.handleJoin();
        }
        List<Join> list = new ArrayList<>(this.joins.values());
        // 遍历，存在的就默认，不存在就增加进去
        joins.forEach(e -> {
            Table table = this.getTable(e);
            try {
                String name = this.getTableName(table);
                if (!this.joins.containsKey(name)) {
                    list.add(this.joins.get(name));
                } else {
                    Table rightItem = (Table) this.joins.get(name).getRightItem();
                    if (!rightItem.getAlias().getName().equals(table.getAlias().getName())) {
                        rightItem.setAlias(table.getAlias());
                    }
                }
            } catch (Exception ee) {
                throw new RuntimeException(ee);
            }

        });
        return list;
    }

    public Expression mergeWhere(StatementHandlerReflex statementHandlerReflex, Expression where, boolean precompile) {
        BinaryExpression expression = this.dynamicWhere(statementHandlerReflex, precompile);
        // 搜索是左合并
        if (statementHandlerReflex.getSqlCommandType() == SqlCommandType.SELECT) {
            return this.leftMergeWhere(expression, where);
        }
        // 其他是右合并
        return this.rightMergeWhere(expression, where);
    }

    /**
     * 左合并位置
     *
     * @param where      哪里
     * @param expression 表达
     * @return {@link Expression}
     */
    public Expression leftMergeWhere(BinaryExpression expression, Expression where) {
        where = where == null ? expression : new AndExpression(expression, where);
        if (this.getWhereCache() != null) {
            return new AndExpression(where, this.getWhereCache());
        }
        return where;
    }

    /**
     * 右合并在哪里
     *
     * @param where      哪里
     * @param expression 表达
     * @return {@link Expression}
     */
    public Expression rightMergeWhere(BinaryExpression expression, Expression where) {
        where = where == null ? expression : new AndExpression(where, expression);
        if (this.getWhereCache() != null) {
            return new AndExpression(this.getWhereCache(), where);
        }
        return where;
    }


    public void handleJoin() {
        // 处理join连接
        LinkedHashMap<String, Join> objectObjectLinkedHashMap = new LinkedHashMap<>();
        this.dataSource.getJoin().forEach(join -> {
            // 判断原join中是否存在需要连的表
            Join jo = new Join();
            Table table = new Table(join.table());
            table.setAlias(new Alias("range_" + join.table()));
            jo.setRightItem(table);
            // 有自定义的关联表就自定义，没有就默认主表
            Table joinTable = StringUtils.hasText(join.linkTable()) ? new Table(join.linkTable()) : this.from;
            // on的字段名称有就用，没有就默认关联表的字段名名称
            String field = StringUtils.hasText(join.linkField()) ? join.linkField() : join.field();
            Column left = new Column((Table) jo.getRightItem(), join.field());
            Column right = new Column(joinTable, field);
            jo.addOnExpression(new EqualsTo(left, right));
            objectObjectLinkedHashMap.put(join.table(), jo);

            // 判断是否需要进行逻辑删除过滤
            for (DelFlag delFlag : join.delFlag()) {
                Column leftDel = new Column(table, delFlag.fieldName);
                EqualsTo equalsTo = new EqualsTo(leftDel, new Column(delFlag.notDeleteValue.toString()));
                this.whereCache = this.whereCache == null ? equalsTo : new AndExpression(equalsTo, this.whereCache);
            }

            // 判断是不是数据权限的表
            if (join.isDataScope()) {
                // 设置权限限制表
                this.leftSql.setTable(table);
                this.dataScopeTable = table;
            }
        });
        this.joins = objectObjectLinkedHashMap;
    }

    /**
     * @return {@link Table}
     */
    public Table getTable(Join join) {
        FromItem rightItem = join.getRightItem();
        if (rightItem instanceof Table) {
            return (Table) rightItem;
        } else {
            throw new RuntimeException("未知类型，无法获取查询表名");
        }
    }

    @SneakyThrows
    public String getTableName(Table table) {
        if (table.getASTNode() != null) {
            return table.getASTNode().jjtGetFirstToken().image;
        } else {
            // 使用反射
            Object partItems = ReflectUtil.getFieldValue(table, "partItems");
            if (partItems instanceof List && !((List<?>) partItems).isEmpty()) {
                return ((List<String>) partItems).get(0);
            }
        }
        throw new RuntimeException("无法获取查询表名");
    }
}

