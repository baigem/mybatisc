package cmc.mybatisc.config;

import cmc.mybatisc.base.StringList;
import cmc.mybatisc.utils.reflect.GenericType;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * 字符串到字符串列表转换器工厂
 *
 * @author 程梦城
 * @version 1.0.0
 * &#064;date  2023/12/01
 */
@SuppressWarnings({"unchecked","rawtypes"})
public class StringToStringListConverterFactory implements ConverterFactory<String, StringList> {
    private final static Map<Class<?>, Converter<String, StringList>> CONVERTER_MAP = new HashMap<>();

    @Override
    public <T extends StringList> Converter<String, T> getConverter( Class<T> targetType) {
        if (!CONVERTER_MAP.containsKey(targetType)) {
            Class<T> first = (Class<T>) GenericType.forClass(targetType).first();
            CONVERTER_MAP.put(targetType, str-> new StringList(str,first));
        }
        return (Converter<String, T>) CONVERTER_MAP.get(targetType);
    }
}
