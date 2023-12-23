package cmc.mybatisc.parser;

import cmc.mybatisc.annotation.SoftDelete;
import cmc.mybatisc.base.CodeStandardEnum;
import cmc.mybatisc.config.interfaces.DelFlag;
import cmc.mybatisc.config.interfaces.TableEntity;
import lombok.Data;

import java.lang.reflect.Method;

/**
 * 字段删除解析器
 *
 * @author 程梦城
 * @version 1.0.0
 * &#064;date  2023/11/27
 */
@Data
public class SoftDeleteParser {
    /**
     * 映射器解析器
     */
    private MapperParser mapperParser;
    /**
     * 别名
     */
    private final AliasParser alias = new AliasParser();

    private String table;

    private String tableAlias;

    private CodeStandardEnum nameMode;

    private String removeSuffix;

    private Class<?> exclude;

    public SoftDeleteParser(SoftDelete fieldDelete, Method method, MapperParser mapperParser) {
        this.parse(fieldDelete, method, mapperParser);
    }

    private void parse(SoftDelete fieldDelete, Method method, MapperParser mapperParser) {
        this.mapperParser = mapperParser;
        if (fieldDelete.table() != TableEntity.class) {
            EntityParser entityParser = new EntityParser(fieldDelete.table());
            this.table = entityParser.getTableName();
        }
        this.nameMode = fieldDelete.nameMode();
        this.removeSuffix = fieldDelete.removeSuffix();
//        this.exclude = fieldDelete.exclude();
    }
}
