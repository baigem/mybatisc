package cmc.mybatisc.parser;

import cmc.mybatisc.annotation.DataScope;
import cmc.mybatisc.annotation.SearchField;
import cmc.mybatisc.base.CodeStandardEnum;
import lombok.Data;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 搜索字段解析器
 *
 * @author 程梦城
 * @version 1.0.0
 * &#064;date  2023/11/27
 */
@Data
public class SearchFieldParser {
    /**
     * 字段名称
     */
    private String name;
    /**
     * 字段类型
     */
    private Class<?> type;
    /**
     * 字段注解
     */
    private SearchField annotation;
    /**
     * join列表
     */
    private List<JoinParser> joinList;
    /**
     * 名称转换器
     */
    private CodeStandardEnum nameMode;
    /**
     * 模糊查询
     */
    private Boolean like;
    /**
     * 范围查询
     */
    private Boolean range;
    /**
     * 排序
     */
    private Boolean sort;
    /**
     * 序号
     */
    private Integer sortNo;
    /**
     * 排序规则
     */
    private String sortRule;
    /**
     * 分组
     */
    private Boolean group;
    /**
     * 自定义实体
     */
    private Boolean customEntity;
    /**
     * 是否必须
     */
    private Boolean and;
    /**
     * 分组查询
     */
    private String grouping;
    /**
     * 连接符
     */
    private DataScope.Compare compare;
    /**
     * 范围查询模式后缀
     */
    private String rangeModeSuffix;
    /**
     * 范围查询结束后缀
     */
    private String rangeEndSuffix;
    /**
     * 入参后缀
     */
    private String listTypeSuffix;

    private Field field;

    public SearchFieldParser(AliasParser aliasParser, SearchField searchField, Field field) {
        this.parse(aliasParser, searchField, field);
    }

    private void parse(AliasParser aliasParser, SearchField searchField, Field field) {
        // 开始解析数据
        this.field = field;
        this.annotation = searchField;
        this.type = field.getType();
        this.nameMode = searchField.nameMode();
        this.name = StringUtils.hasText(searchField.value()) ? searchField.value() : field.getName();
        this.joinList = Arrays.stream(searchField.join()).map(info -> new JoinParser(info, aliasParser)).collect(Collectors.toList());
        this.like = searchField.like();
        this.range = searchField.range();
        this.sort = searchField.sort();
        this.sortNo = searchField.sortNo();
        this.sortRule = searchField.sortRule();
        this.group = searchField.group();
        this.customEntity = searchField.customEntity();
        this.and = searchField.and();
        this.grouping = searchField.grouping();
        this.compare = searchField.compare();
        this.rangeModeSuffix = searchField.rangeModeSuffix();
        this.rangeEndSuffix = searchField.rangeEndSuffix();
        this.listTypeSuffix = searchField.listTypeSuffix();

        // 设置表别名
        for (JoinParser joinParser : this.joinList) {
            aliasParser.set(joinParser.getEntityParser().getTableName());
            aliasParser.set(joinParser.getLinkTableEntityParser().getTableName());
        }
    }

    /**
     * 获取联接解析器列表
     *
     * @return {@link List}<{@link JoinParser}>
     */
    public List<JoinParser> getJoinParserList() {
        return this.joinList.stream().filter(JoinParser::isPresent).collect(Collectors.toList());
    }
}
