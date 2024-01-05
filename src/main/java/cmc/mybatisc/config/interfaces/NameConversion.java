package cmc.mybatisc.config.interfaces;

import cmc.mybatisc.utils.string.CaseConverter;

import java.lang.reflect.Field;

/**
 * 名称转换
 *
 * @author cmc
 * &#064;date  2024/01/04
 */
public interface NameConversion {

    /**
     * 转换表名称
     *
     * @param name   名称
     * @param ignore 忽视
     * @return {@link String}
     */
    default String conversionTableName(Class<?> ignore, String name){
        return CaseConverter.convertToUnderline(name);
    }

    /**
     * 转换字段名称
     *
     * @param name   名称
     * @param ignore 忽视
     * @return {@link String}
     */
    default String conversionFieldName(Field ignore,String name){
        return CaseConverter.convertToUnderline(name);
    }
}
