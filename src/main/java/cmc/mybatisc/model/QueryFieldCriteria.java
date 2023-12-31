package cmc.mybatisc.model;

import cmc.mybatisc.annotation.DataScope;
import cmc.mybatisc.core.util.AliasOperation;
import cmc.mybatisc.core.util.TableStructure;
import cmc.mybatisc.parser.*;
import lombok.Builder;
import lombok.Data;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

/**
 * 查询字段条件
 *
 * @author 程梦城
 * @version 1.0.0
 * &#064;date  2023/11/27
 */
@Data
@Builder
public class QueryFieldCriteria {
    /**
     * 映射分析器
     */
    private MapperParser mapperParser;
    /**
     * 表别名
     */
    private AliasOperation aliasOperation;
    /**
     * 别名
     */
    private String alias;
    /**
     * 注释
     */
    private Annotation annotation;
    /**
     * 表名
     */
    private String tableName;
    /**
     * 字段名称
     */
    private String fieldName;
    /**
     * 全称
     */
    private String fullName;
    /**
     * 参数类型
     */
    private Class<?> fieldClass;
    /**
     * 参数
     */
    private Parameter parameter;
    /**
     * 查询模式
     */
    private String modeName;
    /**
     * 查询结束范围
     */
    private String endName;
    /**
     * 是否模糊查询
     */
    private boolean like;
    /**
     * 是否是范围查询
     */
    private boolean range;
    /**
     * 是否排序
     */
    private boolean sort;
    /**
     * 是否分组
     */
    private boolean group;

    /**
     * 排序规则
     */
    private String sortRule;
    /**
     * 排序序号
     */
    private int sortNo;
    /**
     * 默认都是自定义实体，这个属于第二判断条件
     */
    private boolean customEntity;
    /**
     * 和
     */
    private boolean and;
    /**
     * 分组
     */
    private String grouping;

    /**
     * 比较
     */
    private DataScope.Compare compare;

    /**
     * 按搜索生成
     *
     * @param searchParser 搜索解析器
     * @return {@link List}<{@link QueryFieldCriteria}>
     */
    public static List<QueryFieldCriteria> buildBySearch(SearchParser searchParser) {
        List<QueryFieldCriteria> list = new ArrayList<>();
        for (SearchParameterParser searchParameterParser : searchParser.getParameterParserList()) {
            for (SearchFieldParser fieldParser : searchParameterParser.getSearchFieldList()) {
                TableStructure table = fieldParser.getJoinList().isEmpty() ? searchParser.getTableStructure() : fieldParser.getJoinList().get(fieldParser.getJoinList().size() - 1).getJoinTableStructure();
                QueryFieldCriteria build = QueryFieldCriteria.builder()
                        .parameter(searchParameterParser.getParameter())
                        .mapperParser(searchParser.getMapperParser())
                        .tableName(table.getName())
                        .alias(table.getAlias())
                        .aliasOperation(searchParser.getMybatiscConfig().getAlias())
                        .fieldName(fieldParser.getName())
                        .like(fieldParser.getLike())
                        .range(fieldParser.getRange())
                        .endName(searchParameterParser.getName() + "." + fieldParser.getField().getName() + fieldParser.getRangeEndSuffix())
                        .fieldClass(fieldParser.getType())
                        .fullName(searchParameterParser.getName() + "." + fieldParser.getField().getName())
                        .modeName(searchParameterParser.getName() + "." + fieldParser.getField().getName() + fieldParser.getRangeModeSuffix())
                        .sort(fieldParser.getSort())
                        .sortRule(fieldParser.getSortRule())
                        .sortNo(fieldParser.getSortNo())
                        .group(fieldParser.getGroup())
                        .annotation(fieldParser.getAnnotation())
                        .customEntity(fieldParser.getCustomEntity())
                        .and(fieldParser.getAnd())
                        .grouping(fieldParser.getGrouping())
                        .compare(fieldParser.getCompare())
                        .build();
                list.add(build);
            }
        }
        return list;
    }
}
