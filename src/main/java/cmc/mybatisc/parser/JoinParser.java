package cmc.mybatisc.parser;

import cmc.mybatisc.annotation.Join;
import cmc.mybatisc.model.DelFlag;
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
    private String table;
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
    private String linkTable;
    private String linkTableAlias;
    /**
     * 关联表的字段，默认是field
     */
    private String linkField;
    /**
     * 此表是否是返回数据的表
     */
    private Boolean isDataScope;
    /**
     * 此表的逻辑删除，后期优化掉
     */
    private DelFlag[] delFlag;

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
        this.delFlag = join.delFlag();
        if (!StringUtils.hasText(this.linkField)) {
            this.linkField = this.field;
        }
        if (!StringUtils.hasText(this.linkTable)) {
            this.linkTable = aliasParser.getMainTable();
        }
        // 设置别名
        aliasParser.set(this.table);
        aliasParser.set(this.linkTable);

        // 回写别名
        this.tableAlias = aliasParser.get(this.table);
        this.linkTableAlias = aliasParser.get(this.linkTable);
    }

    /**
     * 存在
     *
     * @return boolean
     */
    public boolean isPresent() {
        return StringUtils.hasText(this.table);
    }
}
