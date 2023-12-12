package cmc.mybatisc.parser;

import cmc.mybatisc.base.CodeStandardEnum;
import cmc.mybatisc.utils.MapperStrongUtils;
import lombok.Data;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * 实体解析器
 *
 * @author 程梦城
 * @version 1.0.0
 * &#064;date  2023/11/27
 */
@Data
public class EntityParser {
    private Class<?> entity;
    private String tableName;
    private Field keyField;
    private final List<String> fieldList = new ArrayList<>();

    public EntityParser(Class<?> entity) {
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
    }
}
