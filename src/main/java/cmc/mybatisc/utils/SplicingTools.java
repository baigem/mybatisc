package cmc.mybatisc.utils;

import com.alibaba.fastjson2.JSON;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 拼接工具
 *
 * @author 程梦城
 * @version 1.0.0
 * &#064;date  2023/07/07
 */
public class SplicingTools {
    /**
     * 分隔符
     */
    private static final String DELIMITER = ",";


    /**
     * 生成拼接的字符串
     *
     * @param list 列表
     * @return {@link String}
     */
    public static String generate(List<String> list) {
        return SplicingTools.generate(list, DELIMITER);
    }

    public static <T> String generate(List<T> list, Function<T, String> getStr) {
        return SplicingTools.generate(list.stream().map(getStr).distinct().filter(Objects::nonNull).collect(Collectors.toList()), DELIMITER);
    }

    public static <T> String generate(List<T> list, Function<T, String> getStr, String delimiter) {
        return SplicingTools.generate(list.stream().map(getStr).distinct().filter(Objects::nonNull).collect(Collectors.toList()), delimiter);
    }

    public static String generate(List<String> list, String delimiter) {
        if (list == null || list.isEmpty()) {
            return "";
        }
        // 净化原始内容中的字符
        return list.stream().map(str -> str.replaceAll(delimiter, "\\\\" + delimiter).replaceAll("\\\\", "\\\\\\\\")).collect(Collectors.joining(delimiter));
    }

    /**
     * 解析成原始的字符串数组
     *
     * @param str str
     * @return {@link List}<{@link String}>
     */
    public static List<String> parse(String str) {
        return SplicingTools.parse(str, DELIMITER);
    }

    public static List<String> parse(String str, String delimiter) {
        if (!StringUtils.hasText(str)) {
            return Collections.emptyList();
        }
        return Arrays.stream(str.split("(?<=[^\\\\])" + delimiter)).map(s -> s.replaceAll("\\\\" + delimiter, delimiter).replaceAll("\\\\\\\\", "\\\\")).collect(Collectors.toList());
    }

    public static <T> List<T> parse(String str, Class<T> tClass) {
        return SplicingTools.parse(str, DELIMITER, tClass);
    }

    /**
     * 解析
     *
     * @param str    str
     * @param tClass t类
     * @return {@link List}<{@link T}>
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> parse(String str, String delimiter, Class<T> tClass) {
        if (!StringUtils.hasText(str)) {
            return Collections.emptyList();
        }
        List<String> list = Arrays.stream(str.split("(?<=[^\\\\])" + delimiter)).map(s -> s.replaceAll("\\\\" + delimiter, delimiter).replaceAll("\\\\\\\\", "\\\\")).collect(Collectors.toList());
        try{
            return list.stream().map(e -> JSON.parseObject(e, tClass)).collect(Collectors.toList());
        }catch (Exception e){
            if( tClass == null || tClass == String.class){
                return (List<T>) list;
            }
            throw e;
        }
    }
}
