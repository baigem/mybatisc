package cmc.mybatisc.parser;

import cmc.mybatisc.base.CodeStandardEnum;
import cmc.mybatisc.utils.MapperStrongUtils;
import lombok.Data;
import lombok.Getter;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 实体解析器
 *
 * @author 程梦城
 * @version 1.0.0
 * &#064;date  2023/11/27
 */
@Data
public class EntityParser {
    /**
     * 全局别名控制器
     */
    @Getter
    private static final AliasParser alias = new AliasParser();
    private static final Map<Class<?>, EntityParser> entities = new HashMap<>();

    /**
     * 实体
     */
    private Class<?> entity;
    /**
     * 表名
     */
    private String tableName;
    /**
     * 表别名
     */
    private String tableAlias;
    /**
     * 关键字段
     */
    private Field keyField;
    /**
     * 字段列表
     */
    private final List<String> fieldList = new ArrayList<>();

    public EntityParser(Class<?> entity) {
        EntityParser.entities.put(entity,this);
        this.parse(entity);
    }

    private void parse(Class<?> entity) {
        this.entity = entity;
        // 获取主键
        this.keyField = MapperStrongUtils.getKeyField(this.entity);
        // 获取表名
        this.tableName = MapperStrongUtils.getTableName(this.entity, null);
        // 获取字段名称
        this.fieldList.addAll(MapperStrongUtils.getFieldNames(this.entity, CodeStandardEnum.UNDERLINE));
        if (this.fieldList.isEmpty()) {
            this.fieldList.add("*");
        }
        // 设置表别称
        this.tableAlias = alias.set(this.tableName);
    }


    public static EntityParser computeIfAbsent(Class<?> entity){
        return entities.computeIfAbsent(entity, EntityParser::new);
    }
}
