package cmc.mybatisc.parser;

import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * sql解析器
 *
 * @author 程梦城
 * @version 1.0.0
 * &#064;date  2023/12/24
 */
@Data
public class SqlParser {
    /**
     * sql
     */
    private String sql;
    /**
     * 参数
     */
    private Map<String, Function<?,Serializable>> parameters = new HashMap<>();
}
