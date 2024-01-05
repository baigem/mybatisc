package cmc.mybatisc.core.util;

import cmc.mybatisc.base.model.MapList;
import lombok.Data;
import org.springframework.util.StringUtils;
import java.util.Optional;

/**
 * 别名操作
 *
 * @author 程梦城
 * @version 1.0.0
 * &#064;date  2023/11/27
 */
public class AliasOperation {
    /**
     * 别名
     */
    private final MapList<String, Alias> aliases = new MapList<>(e->e.originalName);

    /**
     * 设置名称自动生产别名
     *
     * @param name 姓名
     */
    public synchronized String set(String name) {
        if (!StringUtils.hasText(name) || this.aliases.containsKey(name)) {
            return Optional.ofNullable(this.aliases.get(name)).map(e->e.alias).orElse(null);
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
        this.aliases.add(new Alias(name,s.toString()));
        return s.toString();
    }

    /**
     * 获取
     *
     * @param name 姓名
     * @return {@link String}
     */
    public String get(String name) {
       return this.getAlias(name).alias;
    }

    public Alias getAlias(String name) {
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
        return this.get(name);
    }

    @Data
    public static class Alias {
        /**
         * 原名
         */
        private String originalName;
        /**
         * 别名
         */
        private String alias;

        public Alias(String originalName, String alias) {
            this.originalName = originalName;
            this.alias = alias;
        }
    }
}
