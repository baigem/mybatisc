package cmc.mybatisc.parser;

import cmc.mybatisc.annotation.Search;
import cmc.mybatisc.annotation.SearchField;
import cmc.mybatisc.base.CodeStandardEnum;
import cmc.mybatisc.model.QueryFieldCriteria;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 搜索解析器
 *
 * @author 程梦城
 * @version 1.0.0
 * &#064;date  2023/11/27
 */
@Data
@NoArgsConstructor
public class SearchParser {
    private AliasParser alias = new AliasParser();
    /**
     * 查询表
     */
    private String table;
    /**
     * 表别名
     */
    private String tableAlias;
    /**
     * 关联查询
     */
    private List<JoinParser> joinList;
    /**
     * 字段列表
     */
    private List<String> fields;
    /**
     * 排除字段
     */
    private List<String> excludeField;
    /**
     * 添加字段
     */
    private List<String> addField;
    /**
     * 名称转换器
     */
    private CodeStandardEnum nameMode;

    /**
     * 移除后缀
     */
    private String removeSuffix;

    /**
     * 是否映射 主键 对数据
     */
    private Boolean mapping;

    /**
     * 映射字段 默认主键
     */
    private String mappingField;

    /**
     * 参数解析器列表
     */
    private List<SearchParameterParser> parameterParserList = new ArrayList<>();

    /**
     * 映射器解析器
     */
    private MapperParser mapperParser;

    public SearchParser(Search search, Method method, MapperParser mapperParser) {
        this.parse(search, method, mapperParser);
    }

    private void parse(Search search, Method method, MapperParser mapperParser) {
        this.mapperParser = mapperParser;
        this.table = search.table();
        this.fields = Arrays.asList(search.fields());
        this.excludeField = Arrays.asList(search.excludeField());
        this.addField = Arrays.asList(search.addField());
        this.nameMode = search.nameMode();
        this.removeSuffix = search.removeSuffix();
        this.mapping = search.mapping();
        this.mappingField = search.mappingField();
        if (!StringUtils.hasText(this.table)) {
            this.table = mapperParser.getEntityParser().getTableName();
        }
        if (!StringUtils.hasText(this.table)) {
            throw new RuntimeException("Table Name not found");
        }
        this.alias.setMainTable(mapperParser.getEntityParser().getEntity(),this.table);
        // 设置表别名
        this.tableAlias = this.alias.get(this.table);
        this.joinList = Arrays.stream(search.join()).map(info -> new JoinParser(info, this.alias)).filter(JoinParser::isPresent).collect(Collectors.toList());
        // 解析搜索字段列表
        for (Parameter parameter : method.getParameters()) {
            this.parameterParserList.add(new SearchParameterParser(parameter, this.alias));
        }
    }


    public static SearchParser parse(QueryFieldCriteria info) {
        SearchParser searchParser = new SearchParser();
        SearchField annotation = (SearchField) info.getAnnotation();
        searchParser.alias = info.getTableAliasMap();
        searchParser.mapperParser = info.getMapperParser();
        searchParser.table = info.getTableName();
        searchParser.fields = Collections.emptyList();
        searchParser.excludeField = Collections.emptyList();
        searchParser.addField = Collections.emptyList();
        searchParser.joinList = Arrays.stream(annotation.join()).map(e -> new JoinParser(e, info.getTableAliasMap())).filter(JoinParser::isPresent).collect(Collectors.toList());
        searchParser.nameMode = annotation.nameMode();
        searchParser.removeSuffix = "";
        searchParser.mapping = false;
        searchParser.mappingField = null;
        if (!StringUtils.hasText(searchParser.table)) {
            searchParser.table = info.getMapperParser().getEntityParser().getTableName();
        }
        if (!StringUtils.hasText(searchParser.table)) {
            throw new RuntimeException("Table Name not found");
        }
        searchParser.alias.setMainTable(searchParser.mapperParser.getEntity(), searchParser.table);
        // 设置表别名
        searchParser.tableAlias = searchParser.alias.get(searchParser.table);
        // 解析搜索字段列表
        searchParser.parameterParserList.add(new SearchParameterParser(info.getParameter(), info.getTableAliasMap()));
        return searchParser;
    }

    /**
     * 获取显示字段
     *
     * @return {@link List}<{@link String}>
     */
    public List<String> getDisplayField() {
        List<String> list = new ArrayList<>(this.mapperParser.getEntityParser().getFieldList());
        list.addAll(this.addField);
        // 去重
        return list.stream().distinct().filter(e -> !this.excludeField.contains(e)).map(e -> {
            if (e.contains(".")) {
                // 把.前的表名获取出来
                String tableName = e.substring(0, e.indexOf(".")).replaceAll("['`]", "");
                String field = e.substring(e.indexOf(".") + 1).replaceAll("['`]", "");
                return this.alias.computeIfAbsent(tableName) + "." + field;
            } else {
                return this.tableAlias + "." + e;
            }
        }).collect(Collectors.toList());
    }

    public CodeStandardEnum getCodeStandard() {
        return (this.mapperParser.getMapperStrong() != null && this.mapperParser.getMapperStrong().nameMode() != CodeStandardEnum.UNDERLINE) ? this.mapperParser.getMapperStrong().nameMode() : this.nameMode;
    }

    public List<JoinParser> getJoinParserList() {
        List<JoinParser> collect = new ArrayList<>(this.joinList);
        for (SearchParameterParser searchParameterParser : this.parameterParserList) {
            for (SearchFieldParser fieldParser : searchParameterParser.getSearchFieldList()) {
                collect.addAll(fieldParser.getJoinParserList());
            }
        }
        return collect.stream().distinct().collect(Collectors.toList());
    }
}
