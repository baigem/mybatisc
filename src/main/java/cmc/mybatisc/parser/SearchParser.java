package cmc.mybatisc.parser;

import cmc.mybatisc.annotation.Search;
import cmc.mybatisc.annotation.SearchField;
import cmc.mybatisc.config.MybatisScannerConfigurer;
import cmc.mybatisc.config.interfaces.MybatiscConfig;
import cmc.mybatisc.config.interfaces.TableEntity;
import cmc.mybatisc.core.util.TableStructure;
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
    /**
     * mybatisc配置
     */
    private MybatiscConfig mybatiscConfig;
    /**
     * 表结构
     */
    private TableStructure tableStructure;
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

    public SearchParser(MybatiscConfig mybatiscConfig,Method method, MapperParser mapperParser) {
        this.mybatiscConfig = mybatiscConfig;
        this.parse(method.getAnnotation(Search.class), method, mapperParser);
    }

    private void parse(Search search, Method method, MapperParser mapperParser) {
        this.mapperParser = mapperParser;
        this.fields = Arrays.asList(search.fields());
        this.excludeField = Arrays.asList(search.excludeField());
        this.addField = Arrays.asList(search.addField());
        this.removeSuffix = search.removeSuffix();
        this.mapping = search.mapping();
        this.mappingField = search.mappingField();
        // 设置表结构
        if (search.table() != TableEntity.class) {
            this.tableStructure = new TableStructure(mybatiscConfig,search.table());
        }else{
            this.tableStructure = mapperParser.getTableStructure();
        }
        if (this.tableStructure != null && !StringUtils.hasText(this.tableStructure.getName())) {
            throw new RuntimeException("Table Name not found");
        }
        this.joinList = Arrays.stream(search.join())
                .map(info -> new JoinParser(this.mybatiscConfig, tableStructure, info))
                .filter(JoinParser::isPresent).collect(Collectors.toList());
        // 解析搜索字段列表
        for (Parameter parameter : method.getParameters()) {
            this.parameterParserList.add(new SearchParameterParser(this.mybatiscConfig,this.tableStructure,parameter));
        }
    }


    public static SearchParser parse(QueryFieldCriteria info) {
        SearchParser searchParser = new SearchParser();
        SearchField annotation = (SearchField) info.getAnnotation();
        searchParser.mybatiscConfig = MybatisScannerConfigurer.getBeanFactory().getBean(MybatiscConfig.class);
        searchParser.mapperParser = info.getMapperParser();
        searchParser.fields = Collections.emptyList();
        searchParser.excludeField = Collections.emptyList();
        searchParser.addField = Collections.emptyList();
        if (StringUtils.hasText(info.getTableName())) {
            searchParser.tableStructure = new TableStructure(searchParser.mybatiscConfig, info.getFieldClass());
        }else{
            searchParser.tableStructure = info.getMapperParser().getTableStructure();
        }
        // 校验表名称
        if (!StringUtils.hasText(searchParser.tableStructure.getName())) {
            throw new RuntimeException("Table Name not found");
        }
        searchParser.joinList = Arrays.stream(annotation.join())
                .map(e -> new JoinParser(searchParser.mybatiscConfig, searchParser.tableStructure,e))
                .filter(JoinParser::isPresent).collect(Collectors.toList());
        searchParser.removeSuffix = "";
        searchParser.mapping = false;
        searchParser.mappingField = null;
        // 解析搜索字段列表
        searchParser.parameterParserList.add(new SearchParameterParser(searchParser.mybatiscConfig, searchParser.tableStructure, info.getParameter()));
        return searchParser;
    }

    /**
     * 获取显示字段
     *
     * @return {@link List}<{@link String}>
     */
    public List<String> getDisplayField() {
        List<String> list = new ArrayList<>(this.mapperParser.getTableStructure().getFieldNames());
        list.addAll(this.addField);
        // 去重
        return list.stream().distinct().filter(e -> !this.excludeField.contains(e)).map(e -> {
            if (e.contains(".")) {
                // 把.前的表名获取出来
                String tableName = e.substring(0, e.indexOf(".")).replaceAll("['`]", "");
                String field = e.substring(e.indexOf(".") + 1).replaceAll("['`]", "");
                return this.mybatiscConfig.getAlias().computeIfAbsent(tableName) + "." + field;
            } else {
                return this.tableStructure.getAlias() + "." + e;
            }
        }).collect(Collectors.toList());
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
