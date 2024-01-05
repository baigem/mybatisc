package cmc.mybatisc.parser;

import cmc.mybatisc.annotation.SearchField;
import cmc.mybatisc.config.interfaces.MybatiscConfig;
import cmc.mybatisc.config.interfaces.NameConversion;
import cmc.mybatisc.core.util.AliasOperation;
import cmc.mybatisc.core.util.TableStructure;
import cmc.mybatisc.utils.reflect.ReflectUtils;
import lombok.Data;

import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

/**
 * 搜索参数解析器
 *
 * @author 程梦城
 * @version 1.0.0
 * &#064;date  2023/11/27
 */
@Data
public class SearchParameterParser {
    /**
     * mybatisc配置
     */
    private MybatiscConfig mybatiscConfig;
    /**
     * 主表
     */
    private TableStructure mainTable;
    /**
     * 参数
     */
    private Parameter parameter;
    /**
     * 姓名
     */
    private String name;
    /**
     * 搜索字段列表
     */
    private List<SearchFieldParser> searchFieldList = new ArrayList<>();

    public SearchParameterParser(MybatiscConfig mybatiscConfig,TableStructure tableStructure, Parameter parameter) {
        this.mybatiscConfig = mybatiscConfig;
        this.mainTable = tableStructure;
        this.parse(parameter);
    }

    private void parse(Parameter parameter) {
        this.parameter = parameter;
        this.name = parameter.getName();
        Class<?> type = parameter.getType();
        for (Field declaredField : ReflectUtils.getAllField(type)) {
            if (declaredField.isAnnotationPresent(SearchField.class)) {
                this.searchFieldList.add(new SearchFieldParser(this.mybatiscConfig,  this.mainTable,declaredField));
            }
        }
    }
}
