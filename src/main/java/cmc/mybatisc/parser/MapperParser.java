package cmc.mybatisc.parser;

import cmc.mybatisc.annotation.MapperStrong;
import cmc.mybatisc.base.CodeStandardEnum;
import cmc.mybatisc.utils.MapperStrongUtils;
import cmc.mybatisc.utils.reflect.GenericType;
import lombok.Data;
import lombok.Getter;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
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
    /**
     * 全局储存器
     */
    private static final List<MapperParser> list = new ArrayList<>();

    /**
     * mapper增强注解
     */
    private MapperStrong mapperStrong;
    /**
     * mapper代理对象
     */
    private Class<?> mapper;
    /**
     * 表实体对象
     */
    private Class<?> entity;

    /**
     * 实体解析器
     */
    private EntityParser entityParser;

    public MapperParser(Class<?> mapper) {
        list.add(this);
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
            this.entityParser = new EntityParser(this.entity);
            List<String> fieldList = this.entityParser.getFieldList();
            // 删除需要排除的字段
            Optional.ofNullable(this.mapperStrong).ifPresent(info -> {
                Arrays.stream(info.ignoreField()).forEach(fieldList::remove);
            });
        }
    }

    /**
     * 获取
     *
     * @param tableName 表名
     * @return {@link MapperParser}
     */
    public static MapperParser get(String tableName){
        for (MapperParser mapperParser : MapperParser.list) {
            if(tableName.equals(mapperParser.entityParser.getTableName())){
                return mapperParser;
            }
        }
        return null;
    }

    /**
     * 获取
     *
     * @param entity 实体
     * @return {@link MapperParser}
     */
    public static MapperParser get(Class<?> entity){
        for (MapperParser mapperParser : MapperParser.list) {
            if(entity == mapperParser.entity){
                return mapperParser;
            }
        }
        return null;
    }
}

