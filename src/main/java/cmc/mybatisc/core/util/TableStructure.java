package cmc.mybatisc.core.util;

import cmc.mybatisc.config.MybatisScannerConfigurer;
import cmc.mybatisc.config.interfaces.MybatiscConfig;
import cmc.mybatisc.config.interfaces.NameConversion;
import cmc.mybatisc.config.interfaces.TableEntity;
import cmc.mybatisc.utils.SqlUtils;
import cmc.mybatisc.utils.reflect.ReflectUtils;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.SneakyThrows;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;

/**
 * 表结构
 *
 * @author cmc
 * &#064;date  2024/01/04
 */
@Data
public class TableStructure {
    /**
     * 隐藏物
     */
    private static final Map<Class<?>, TableStructure> cache = new HashMap<>();

    /**
     * mybatisc配置
     */
    private MybatiscConfig mybatiscConfig;
    /**
     * 表实体
     */
    private Class<?> entity;

    /**
     * 名称 [解析好的名称]
     */
    private String name;

    /**
     * 别名
     */
    private String alias;

    /**
     * 主键名称
     */
    private String primaryKeyName;

    /**
     * 主键
     */
    private Field primaryKey;

    /**
     * 字段映射
     */
    private Map<String,Field> fieldMap;
    /**
     * 字段名称 [解析好的名称]
     */
    private List<String> fieldNames = new ArrayList<>();

    /**
     * 字段集合
     */
    private List<Field> fields = new ArrayList<>();

    public TableStructure(MybatiscConfig mybatiscConfig, Class<?> entity){
        this.mybatiscConfig = mybatiscConfig;
        this.entity = entity;
        cache.put(entity,this);
        this.parse(mybatiscConfig);
    }

    /**
     * 设置键字段值
     *
     * @param obj   obj
     * @param value 值
     */
    @SneakyThrows
    public void setPrimaryKeyValue(Object obj, Object value) {
        primaryKey.setAccessible(true);
        primaryKey.set(obj,value);
    }

    /**
     * 获取关键字段值
     *
     * @param obj obj
     * @return {@link Serializable}
     */
    @SneakyThrows
    @SuppressWarnings("unchecked")
    public <R extends Serializable> R getPrimaryKeyValue(Object obj) {
        primaryKey.setAccessible(true);
        return (R) primaryKey.get(obj);
    }

    /**
     * 删除字段
     *
     * @param fields 领域
     */
    public void removeFields(String[] fields){
        for (String field : fields) {
            if(!this.fieldMap.containsKey(field)){
                return;
            }
            this.fieldMap.remove(field);
            this.fields.remove(this.fieldMap.get(field));
            this.fieldNames.remove(field);
        }
    }

    public void parse(MybatiscConfig mybatiscConfig){
        NameConversion nameConversion = mybatiscConfig.getNameConversion();
        // 获取主键
        this.primaryKey = TableStructure.getPrimaryKey(this.entity);
        // 解析主键名称
        if(this.primaryKey != null){
            this.primaryKeyName = nameConversion.conversionFieldName(this.primaryKey,this.primaryKey.getName());
        }
        // 获取表名
        this.name = TableStructure.getTableName(this.entity, null);
        // 设置表别称
        this.alias = mybatiscConfig.getAlias().computeIfAbsent(this.name);
        // 获取字段名称
        this.fieldMap = TableStructure.getTableFields(this.entity, mybatiscConfig.getNameConversion());
        this.fields.addAll(this.fieldMap.values());
        this.fieldNames.addAll(this.fieldMap.keySet());
        if (this.fieldNames.isEmpty()) {
            this.fieldNames.add("*");
        }
    }

    /**
     * 获取表结构
     *
     * @param clazz 拍手
     * @return {@link TableStructure}
     */
    public static TableStructure getTableStructure(Class<?> clazz,TableStructure defaultValue){
        if(clazz == null || clazz == TableEntity.class){
            return defaultValue;
        }
        return new TableStructure(defaultValue.mybatiscConfig, clazz);
    }

    /**
     * 缺席时计算
     *
     * @param entity         实体
     * @param mybatiscConfig mybatisc配置
     * @return {@link TableStructure}
     */
    public static TableStructure computeIfAbsent(MybatiscConfig mybatiscConfig, Class<?> entity){
        return cache.computeIfAbsent(entity, e->new TableStructure(mybatiscConfig, entity));
    }

    /**
     * 获取关键字段
     *
     * @param entity 实体
     * @return {@link Field}
     */
    public static Field getPrimaryKey(Class<?> entity) {
        Field[] declaredFields = entity.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            if (declaredField.isAnnotationPresent(TableId.class)) {
                return declaredField;
            }
        }
        // 获取父级
        if (entity.getSuperclass() != null && entity.getSuperclass() != Object.class) {
            return getPrimaryKey(entity.getSuperclass());
        }
        return null;
    }

    /**
     * 获取表名
     *
     * @param entity 实体
     * @param name   名称
     * @return {@link String}
     */
    public static String getTableName(Class<?> entity, String name) {
        if(entity == TableEntity.class){
            return "";
        }
        MybatiscConfig config = MybatisScannerConfigurer.getBeanFactory().getBean(MybatiscConfig.class);
        TableName tableName = entity.getAnnotation(TableName.class);
        if (tableName != null && StringUtils.hasText(tableName.value())) {
            return tableName.value();
        }
        if (StringUtils.hasText(name)) {
            return name;
        }
        return config.getNameConversion().conversionTableName(entity,entity.getSimpleName());
    }


    /**
     * 获取表字段
     *
     * @param entity         实体
     * @param nameConversion 名称转换
     * @return {@link List}<{@link String}>
     */
    public static Map<String, Field> getTableFields(Class<?> entity, NameConversion nameConversion) {
        List<Field> declaredFields = ReflectUtils.getAllField(entity);
        Map<String, Field> stringMap = new LinkedHashMap<>(declaredFields.size());
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(TableId.class)) {
                TableId tableId = field.getAnnotation(TableId.class);
                if (StringUtils.hasText(tableId.value())) {
                    stringMap.put(nameConversion.conversionFieldName(field,tableId.value()), field);
                    continue;
                }
            } else if (field.isAnnotationPresent(TableField.class)) {
                TableField tableField = field.getAnnotation(TableField.class);
                // 不属于表结构，进行下一个
                if (!tableField.exist()) {
                    continue;
                }
                if (StringUtils.hasText(tableField.value())) {
                    stringMap.put(nameConversion.conversionFieldName(field,tableField.value()), field);
                    continue;
                }
            }
            // 把字段名当做表名称
            stringMap.put(nameConversion.conversionFieldName(field,field.getName()), field);
        }
        return stringMap;
    }

    /**
     * 获取完整字段名
     * 获取字段名
     *
     * @param fieldName    字段名称
     * @param removeSuffix 删除后缀
     * @return {@link String}
     */
    public String getCompleteFieldName(String fieldName, String removeSuffix) {
        return SqlUtils.packageField(this.alias+"."+this.getFieldName(fieldName,removeSuffix));
    }

    /**
     * 获取字段名
     *
     * @param fieldName    字段名称
     * @param removeSuffix 删除后缀
     * @return {@link String}
     */
    public String getFieldName(String fieldName, String removeSuffix) {
        if (removeSuffix != null) {
            fieldName = fieldName.replaceAll(removeSuffix + "$", "");
        }
        fieldName = mybatiscConfig.getNameConversion().conversionFieldName(null,fieldName);
        return SqlUtils.packageField(fieldName);
    }
}
