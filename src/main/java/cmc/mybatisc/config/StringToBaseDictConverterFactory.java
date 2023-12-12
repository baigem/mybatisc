package cmc.mybatisc.config;

import cmc.mybatisc.base.BaseDict;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings({"unchecked", "rawtypes"})
public class StringToBaseDictConverterFactory implements ConverterFactory<String, BaseDict> {
    private final static Map<Class<?>, Converter<String, BaseDict>> CONVERTER_MAP = new HashMap<>();

    @Override
    public <T extends BaseDict> Converter<String, T> getConverter(Class<T> targetType) {
        if (!CONVERTER_MAP.containsKey(targetType)) {
            CONVERTER_MAP.put(targetType, str -> BaseDict.getStrValueDict(targetType, str));
        }
        return (Converter<String, T>) CONVERTER_MAP.get(targetType);
    }
}
