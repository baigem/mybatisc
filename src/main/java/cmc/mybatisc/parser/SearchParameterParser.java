package cmc.mybatisc.parser;

import cmc.mybatisc.annotation.SearchField;
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
    private Parameter parameter;
    private String name;
    private List<SearchFieldParser> searchFieldList = new ArrayList<>();

    public SearchParameterParser(Parameter parameter, AliasParser aliasParser) {
        this.parse(parameter, aliasParser);
    }

    private void parse(Parameter parameter, AliasParser aliasParser) {
        this.parameter = parameter;
        this.name = parameter.getName();
        Class<?> type = parameter.getType();
        for (Field declaredField : type.getDeclaredFields()) {
            if (declaredField.isAnnotationPresent(SearchField.class)) {
                this.searchFieldList.add(new SearchFieldParser(aliasParser, declaredField.getAnnotation(SearchField.class), declaredField));
            }
        }
    }
}
