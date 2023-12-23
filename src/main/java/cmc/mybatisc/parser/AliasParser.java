package cmc.mybatisc.parser;

import lombok.Getter;
import org.springframework.util.StringUtils;

import java.util.HashMap;

/**
 * 别名解析器
 *
 * @author 程梦城
 * @version 1.0.0
 * &#064;date  2023/11/27
 */
public class AliasParser {
    /**
     * 别名
     */
    private final HashMap<String, String> aliases = new HashMap<>();
    /**
     * 主表
     */
    @Getter
    private String mainTable;
    @Getter
    private Class<?> mainTableClass;

    /**
     * 设置名称自动生产别名
     *
     * @param name 姓名
     */
    public synchronized String set(String name) {
        if (!StringUtils.hasText(name) || this.aliases.containsKey(name)) {
            return this.aliases.get(name);
        }
        // 获取别名
        String[] split = name.split("_");
        StringBuilder s = new StringBuilder();
        if (split.length == 1) {
            split = name.split("[A-Z]");
        }
        for (String string : split) {
            s.append(string.charAt(0));
        }
        String index = "";
        while (this.aliases.containsKey(s + index)) {
            index = String.valueOf(Integer.parseInt(StringUtils.hasText(index) ? index : "1") + 1);
        }
        s.append(index);
        this.aliases.put(name, s.toString());
        return s.toString();
    }

    /**
     * 设置主表
     *
     * @param name 姓名
     */
    public void setMainTable(Class<?> clazz,String name) {
        this.mainTable = name;
        this.mainTableClass = clazz;
        this.set(name);
    }

    /**
     * 获取主表别名
     *
     * @return {@link String}
     */
    public String getMainTableAlias() {
        return this.get(this.mainTable);
    }

    /**
     * 获取
     *
     * @param name 姓名
     * @return {@link String}
     */
    public String get(String name) {
        // 获取别名
        if (!this.aliases.containsKey(name)) {
            throw new RuntimeException("别名不存在" + name);
        }

        return this.aliases.get(name);
    }

    /**
     * 如果不存在，则计算
     *
     * @param name 姓名
     * @return {@link String}
     */
    public synchronized String computeIfAbsent(String name) {
        if (!this.aliases.containsKey(name)) {
            this.set(name);
        }
        return this.aliases.get(name);
    }
}
