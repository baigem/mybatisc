package cmc.mybatisc.base;

import java.util.function.Function;

/**
 * 代码标准枚举
 *
 * @author cmc
 * @date 2023/05/26
 */
public enum CodeStandardEnum {
    /**
     * 小驼峰
     */
    SMALL_HUMP("smallHump", (name) -> {
        StringBuilder lowerCase = new StringBuilder(name.substring(0, 1).toLowerCase());
        char[] charArray = name.substring(1).toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            if (charArray[i] == '_') {
                lowerCase.append(String.valueOf(charArray[++i]).toUpperCase());
            } else {
                lowerCase.append(charArray[i]);
            }
        }
        return lowerCase.toString();
    }),

    /**
     * 伟大驼峰
     */
    GREAT_HUMP("greatHump", (name) -> {
        StringBuilder lowerCase = new StringBuilder(name.substring(0, 1).toUpperCase());
        char[] charArray = name.substring(1).toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            if (charArray[i] == '_') {
                lowerCase.append(String.valueOf(charArray[++i]).toUpperCase());
            } else {
                lowerCase.append(charArray[i]);
            }
        }
        return lowerCase.toString();
    }),
    /**
     * 下划线
     */
    UNDERLINE("underline", (name) -> {
        StringBuilder lowerCase = new StringBuilder(name.substring(0, 1).toLowerCase());
        char[] charArray = name.substring(1).toCharArray();
        for (char c : charArray) {
            // 判断大写字母
            if ((c >= 65 && c <= 90) || (c >= 48 && c <= 57)) {
                lowerCase.append("_").append(String.valueOf(c).toLowerCase());
            } else {
                lowerCase.append(c);
            }
        }
        return lowerCase.toString();
    });

    /**
     * 代码
     */
    public final String code;
    /**
     * 处理程序
     */
    private final Function<String, String> handler;

    CodeStandardEnum(String code, Function<String, String> handler) {
        this.code = code;
        this.handler = handler;
    }


    public String handler(String name) {
        return this.handler.apply(name);
    }
}
