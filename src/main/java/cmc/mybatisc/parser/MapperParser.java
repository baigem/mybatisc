package cmc.mybatisc.parser;

import cmc.mybatisc.annotation.MapperStrong;
import cmc.mybatisc.config.interfaces.MybatiscConfig;
import cmc.mybatisc.config.interfaces.TableEntity;
import cmc.mybatisc.core.util.TableStructure;
import cmc.mybatisc.utils.reflect.GenericType;
import lombok.Data;

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
     * 实体解析器
     */
    private TableStructure tableStructure;

    public MapperParser(MybatiscConfig mybatiscConfig, Class<?> mapper) {
        list.add(this);
        this.mapper = mapper;
        this.mapperStrong = mapper.getAnnotation(MapperStrong.class);
        if (this.mapperStrong != null && this.mapperStrong.value() != TableEntity.class) {
            this.tableStructure = new TableStructure(mybatiscConfig,this.mapperStrong.value());
        } else if (GenericType.forClass(mapper).first() != null) {
            this.tableStructure = new TableStructure(mybatiscConfig,GenericType.forClass(mapper).first());
        } else {
            this.tableStructure = null;
        }
        if (this.tableStructure != null) {
            // 删除需要排除的字段
            Optional.ofNullable(this.mapperStrong).ifPresent(info -> {
                this.tableStructure.removeFields(info.ignoreField());
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
            if(mapperParser.tableStructure != null && tableName.equals(mapperParser.tableStructure.getName())){
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
            if(mapperParser.tableStructure != null && entity == mapperParser.tableStructure.getEntity()){
                return mapperParser;
            }
        }
        return null;
    }
}

