package cmc.mybatisc.parser;

import cmc.mybatisc.annotation.Join;
import cmc.mybatisc.config.interfaces.TableEntity;
import lombok.Data;
import org.springframework.util.StringUtils;

/**
 * Join解析器
 *
 * @author 程梦城
 * @version 1.0.0
 * &#064;date  2023/11/27
 */
@Data
public class JoinParser {
    /**
     * 源数据
     */
    private Join join;

    /**
     * 关联的表
     */
    private Class<?> table;
    /**
     * 映射器解析器
     */
    private EntityParser entityParser;
    /**
     * 表别名
     */
    private String tableAlias;
    /**
     * 关联表的字段
     */
    private String field;
    /**
     * join的类型
     */
    private String joinType;
    /**
     * 需要关联的表，默认主表
     */
    private Class<?> linkTable;
    /**
     * 链接表映射器解析器
     */
    private EntityParser linkTableEntityParser;
    /**
     * 链接表别名
     */
    private String linkTableAlias;
    /**
     * 关联表的字段，默认是field
     */
    private String linkField;
    /**
     * 此表是否是返回数据的表
     */
    private Boolean isDataScope;

    public JoinParser(Join join, AliasParser aliasParser) {
        this.parse(join, aliasParser);
    }

    private void parse(Join join, AliasParser aliasParser) {
        // 开始解析数据
        this.join = join;
        this.table = join.table();
        this.field = join.field();
        this.joinType = join.joinType();
        this.linkTable = join.linkTable();
        this.linkField = join.linkField();
        this.isDataScope = join.isDataScope();
        if (!StringUtils.hasText(this.linkField)) {
            this.linkField = this.field;
        }
        if (this.linkTable == TableEntity.class) {
            this.linkTable = aliasParser.getMainTableClass();
        }
        this.entityParser = EntityParser.computeIfAbsent(this.table);
        this.linkTableEntityParser = EntityParser.computeIfAbsent(this.linkTable);
        if(this.entityParser != null){
            // 设置别名
            aliasParser.set(this.entityParser.getTableName());
            // 回写别名
            this.tableAlias = aliasParser.get(this.entityParser.getTableName());
        }
        if(this.linkTableEntityParser != null){
            this.linkTableAlias = aliasParser.set(this.linkTableEntityParser.getTableName());
        }
    }

    /**
     * 存在
     *
     * @return boolean
     */
    public boolean isPresent() {
        return this.table != null;
    }
}
