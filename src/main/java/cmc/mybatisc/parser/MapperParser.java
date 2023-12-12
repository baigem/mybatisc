package cmc.mybatisc.parser;

import cmc.mybatisc.annotation.MapperStrong;
import cmc.mybatisc.base.CodeStandardEnum;
import cmc.mybatisc.utils.MapperStrongUtils;
import cmc.mybatisc.utils.reflect.GenericType;
import lombok.Data;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 映射器解析器
 *
 * @author 程梦城
 * @version 1.0.0
 * &#064;date  2023/11/27
 */
@Data
public class MapperParser {
    private MapperStrong mapperStrong;
    private Class<?> mapper;
    private Class<?> entity;
    private String tableName;
    private Field keyField;
    private final List<String> fieldList = new ArrayList<>();

    public MapperParser(Class<?> mapper) {
        this.mapper = mapper;
        this.mapperStrong = mapper.getAnnotation(MapperStrong.class);
        if (this.mapperStrong != null && this.mapperStrong.value() != Class.class) {
            this.entity = this.mapperStrong.value();
        } else if (GenericType.forClass(mapper).first() != null) {
            this.entity = GenericType.forClass(mapper).first();
        } else {
            this.entity = null;
        }

        if (this.entity != null) {
            // 获取主键
            this.keyField = MapperStrongUtils.getKeyField(this.entity);
            // 获取表名
            this.tableName = MapperStrongUtils.getTableName(this.entity, Optional.ofNullable(this.mapperStrong).map(MapperStrong::name).orElse(null));
            // 获取字段名称
            this.fieldList.addAll(MapperStrongUtils.getFieldNames(this.entity, Optional.ofNullable(this.mapperStrong).map(MapperStrong::nameMode).orElse(CodeStandardEnum.UNDERLINE)));
            if (this.fieldList.isEmpty()) {
                this.fieldList.add("*");
            }
        } else {
            this.keyField = null;
            this.tableName = null;
            this.fieldList.add("*");
        }

        // 删除需要排除的字段
        Optional.ofNullable(this.mapperStrong).ifPresent(info -> {
            for (String s : info.ignoreField()) {
                this.fieldList.remove(s);
            }
        });
    }
}
