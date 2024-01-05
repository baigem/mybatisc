package cmc.mybatisc.parser;

import cmc.mybatisc.annotation.Join;
import cmc.mybatisc.config.interfaces.MybatiscConfig;
import cmc.mybatisc.config.interfaces.TableEntity;
import cmc.mybatisc.core.util.TableStructure;
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
     * 名称转换
     */
    private MybatiscConfig mybatiscConfig;
    /**
     * 主表
     */
    private TableStructure mainTable;
    /**
     * 源数据
     */
    private Join join;

    /**
     * 联接表
     */
    private Class<?> joinTable;
    /**
     * 联接表结构
     */
    private TableStructure joinTableStructure;
    /**
     * 关联表的字段
     */
    private String joinField;
    /**
     * join的类型
     */
    private String joinType;
    /**
     * 需要关联的表，默认主表
     */
    private Class<?> onTable;
    /**
     * 链接表映射器解析器
     */
    private TableStructure onTableStructure;

    /**
     * 关联表的字段，默认是field
     */
    private String onField;
    /**
     * 使用数据源
     */
    private Boolean useDataSource;

    public JoinParser(MybatiscConfig mybatiscConfig, TableStructure mainTable, Join join) {
        this.mybatiscConfig = mybatiscConfig;
        this.mainTable = mainTable;
        this.join = join;
        this.parse();
    }

    private void parse() {
        // 开始解析数据
        this.joinTable = join.joinTable();
        this.joinField = this.mybatiscConfig.getNameConversion().conversionFieldName(null,join.joinField());
        this.joinType = join.joinType();
        // 复用主表
        if(join.onTable() == TableEntity.class){
            this.onTable = this.mainTable.getEntity();
        }else{
            this.onTable = join.onTable();
        }
        if(join.onField().isEmpty()){
            this.onField = join.joinField();
        }else{
            this.onField = join.onField();
        }
        this.useDataSource = join.useDataSource();
        if (!StringUtils.hasText(this.onField)) {
            this.onField = this.joinField;
        }
        if (this.onTable == TableEntity.class) {
            this.onTable = mainTable.getEntity();
        }
        this.joinTableStructure = TableStructure.computeIfAbsent(this.mybatiscConfig,this.joinTable);
        this.onTableStructure = TableStructure.computeIfAbsent(this.mybatiscConfig, this.onTable);
    }

    /**
     * 存在
     *
     * @return boolean
     */
    public boolean isPresent() {
        return this.joinTable != null;
    }
}
