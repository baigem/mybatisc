package cmc.mybatisc.strengthen;

import cmc.mybatisc.annotation.Search;
import cmc.mybatisc.base.CodeStandardEnum;
import cmc.mybatisc.model.DelFlag;
import cmc.mybatisc.model.FieldSelectDataSource;
import cmc.mybatisc.model.ParamAnnotation;
import cmc.mybatisc.parser.MapperParser;
import cmc.mybatisc.utils.MapperStrongUtils;
import cmc.mybatisc.utils.SqlUtils;
import cmc.mybatisc.utils.reflect.GenericType;
import lombok.Getter;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.builder.BuilderException;
import org.apache.ibatis.session.SqlSession;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class BaseStrengthen implements Strengthen {
    /**
     * 表增强集合，[表名，增强类]
     */
    public final static Map<String, BaseStrengthen> TABLE_STRENGTHEN = new HashMap<>();
    /**
     * mybatis的sqlSession
     */
    protected final SqlSession sqlSession;
    /**
     * 映射器解析器
     */
    protected final MapperParser mapperParser;
    /**
     * 原始的代理接口对象
     */
    protected final Class<?> mapper;
    /***
     * 处理后的字段字符串
     */
    @Getter
    private final String handleFieldString;

    public BaseStrengthen(SqlSession sqlSession, Class<?> mapper) {
        this.sqlSession = sqlSession;
        this.mapper = mapper;
        this.mapperParser = new MapperParser(mapper);

        // 处理字段名称
        this.handleFieldString = this.mapperParser.getFieldList().stream().map(field -> {
            if (field.startsWith("`") || field.equals("*")) {
                return field;
            }
            return "`" + field + "`";
        }).collect(Collectors.joining(","));
        TABLE_STRENGTHEN.put(this.mapperParser.getTableName(), this);
    }

    /**
     * 获取字段名称
     *
     * @param codeStandardEnum 代码标准枚举
     * @param fieldName        字段名称
     * @param suffix           后缀
     * @return {@link String}
     */
    public static String getFieldName(CodeStandardEnum codeStandardEnum, String fieldName, String suffix) {
        if (suffix == null) {
            return codeStandardEnum.handler(fieldName);
        }
        return SqlUtils.packageField(codeStandardEnum.handler(fieldName.replaceAll(suffix + "$", "")));
    }

    /**
     * 获取表别名
     *
     * @param tableName 表名
     * @return {@link String}
     */
    public static String getTableAlias(String tableName) {
        char[] charArray = tableName.toCharArray();
        StringBuilder sb = new StringBuilder();
        sb.append(charArray[0]);
        for (int i = 0; i < charArray.length; i++) {
            if (charArray[i] == '_') {
                sb.append(charArray[i + 1]);
            }
        }
        return sb.toString();
    }

    /**
     * 获取代码标准枚举
     *
     * @param codeStandardEnum 代码标准枚举
     * @return {@link CodeStandardEnum}
     */
    public CodeStandardEnum getCodeStandardEnum(CodeStandardEnum codeStandardEnum) {
        return (this.mapperParser.getMapperStrong() != null && this.mapperParser.getMapperStrong().nameMode() != CodeStandardEnum.UNDERLINE) ? this.mapperParser.getMapperStrong().nameMode() : codeStandardEnum;
    }

    /**
     * 创建地图
     *
     * @param parameters 参数
     * @param args       args
     * @return {@link Map}<{@link String}, {@link Object}>
     */
    public Map<String, Object> createMap(Parameter[] parameters, Object[] args) {
        HashMap<String, Object> map = new HashMap<>();
        for (int i = 0; i < parameters.length; i++) {
            String name = parameters[i].getName();
            if (parameters[i].isAnnotationPresent(Param.class)) {
                name = parameters[i].getAnnotation(Param.class).value();
            }
            map.put(name, args[i]);
        }
        return map;
    }

    /**
     * 获取删除值
     *
     * @param value 值
     * @return {@link String}
     */
    public DelFlag getDelFlag(DelFlag[] value) {
        if (this.mapperParser.getMapperStrong() != null) {
            value = this.mapperParser.getMapperStrong().delFlag();
        }
        for (DelFlag delFlag : value) {
            if (this.mapperParser.getFieldList().contains(delFlag.fieldName)) {
                return delFlag;
            }
        }
        return null;
    }

    /**
     * 地图键
     *
     * @param method    方法
     * @param fieldName 字段名称
     * @param id        id
     * @return {@link Function}<{@link Object[]}, {@link Object}>
     */
    protected Function<Object[], Object> mapKey(boolean full, Method method, String fieldName, String id) {
        if (!StringUtils.hasText(fieldName) && this.mapperParser.getKeyField() == null) {
            throw new RuntimeException("启动mapping属性，主键id未配置，请配置主键id注解@TableId，或者使用mappingField配置");
        }
        // 获取泛型类型
        GenericType genericType = GenericType.forMethod(this.mapper, method);
        String keyName = StringUtils.hasText(fieldName) ? fieldName : this.mapperParser.getKeyField().getName();
        Class<?> returnTypeClass = genericType.last(method.getReturnType());
        Field key = this.getPrototypeChainField(returnTypeClass, keyName);
        if (key == null) {
            throw new RuntimeException("目标字段在类上不存在，无法构建映射器");
        }
        key.setAccessible(true);
        if (key.getType() != genericType.first()) {
            throw new BuilderException("@FieldQuery注释接口Map中key值类型与实体类中的字段类型不符，无法构建映射器");
        }
        return (p) -> {
            if (!this.checkInputParameters(full, p)) {
                return new HashMap<>();
            }
            List<Object> objects = this.sqlSession.selectList(id, this.createMap(method.getParameters(), p));
            return objects.stream().collect(HashMap::new, (m, v) -> {
                try {
                    m.put(key.get(v), v);
                } catch (IllegalAccessException ex) {
                    throw new RuntimeException("字段内容反射获取失败，不太可能出现", ex);
                }
            }, HashMap::putAll);
        };
    }

    /**
     * 获取显示字段
     *
     * @param search 搜索
     * @param alias  别名
     * @return {@link List}<{@link String}>
     */
    public List<String> getDisplayField(Search search, String alias, Map<String, String> aliasMap) {
        List<String> strings = Arrays.asList(search.excludeField());
        List<String> list = new ArrayList<>(this.mapperParser.getFieldList());
        list.addAll(Arrays.asList(search.addField()));
        // 去重
        return list.stream().distinct().filter(e -> !strings.contains(e)).map(e -> {
            if (e.contains(".")) {
                // 把.前的表名获取出来
                String tableName = e.substring(0, e.indexOf(".")).replaceAll("['`]", "");
                String field = e.substring(e.indexOf(".") + 1).replaceAll("['`]", "");
                if (aliasMap.containsKey(tableName)) {
                    return aliasMap.get(tableName) + "." + field;
                }
                aliasMap.put(tableName, BaseStrengthen.getTableAlias(tableName));
                return aliasMap.get(tableName) + "." + field;
            } else {
                return alias + "." + e;
            }
        }).collect(Collectors.toList());
    }


    /**
     * 生成删除标志
     *
     * @return {@link String}
     */
    public String generateDeleteFlag(String prefix, String suffix, String field, String delFlag, boolean isDeleteDate) {
        // 能进来说明一定有逻辑删除字段
        if (!StringUtils.hasText(delFlag)) {
            return prefix + field + " is null " + suffix;
        } else {
            return prefix + field + " = " + delFlag + " " + suffix;
        }
    }

    /**
     * 生成代理方法
     *
     * @param fieldSelectDataSource 字段查询数据源
     * @return {@link Function}<{@link Object[]}, {@link Object}>
     */
    public Function<Object[], Object> generateProxyMethod(FieldSelectDataSource fieldSelectDataSource) {
        Class<?> returnType = fieldSelectDataSource.getMethod().getReturnType();
        String id = fieldSelectDataSource.getId();
        Method method = fieldSelectDataSource.getMethod();
        boolean isNull = fieldSelectDataSource.isAllowNull();
        if (!isNull) {
            for (Parameter parameter : method.getParameters()) {
                isNull = ParamAnnotation.generate(parameter).isNull;
                if (isNull) {
                    break;
                }
            }
        }

        // 创建一些映射的对象，并返回引用id
        // 判断是否需要进行key->value 处理
        boolean finalIsNull = isNull;
        if (returnType == Map.class && fieldSelectDataSource.getMapping()) {
            return this.mapKey(isNull, method, fieldSelectDataSource.getMappingField(), id);
        } else if (MapperStrongUtils.isListTypeClass(returnType)) {
            return (p) -> this.checkInputParameters(finalIsNull, p) ? this.sqlSession.selectList(id, this.createMap(method.getParameters(), p)) : new ArrayList<>();
        } else if (returnType == Optional.class) {
            // 参数转换
            return (p) -> Optional.ofNullable(this.checkInputParameters(finalIsNull, p) ? this.sqlSession.selectOne(id, this.createMap(method.getParameters(), p)) : null);
        } else {
            return (p) -> this.checkInputParameters(finalIsNull, p) ? this.sqlSession.selectOne(id, this.createMap(method.getParameters(), p)) : null;
        }
    }


    /**
     * 检查输入参数
     */
    public boolean checkInputParameters(boolean state, Object[] obj) {
        if (state || obj == null) {
            return true;
        }
        for (Object o : obj) {
            if (o == null || (o instanceof List && ((List<?>) o).isEmpty())) {
                return false;
            }
        }
        return true;
    }


    /**
     * 获取原型链字段
     *
     * @param clazz 克拉兹
     * @param name  名称
     * @return {@link Field}
     */
    public Field getPrototypeChainField(Class<?> clazz, String name) {
        for (Field declaredField : clazz.getDeclaredFields()) {
            if (declaredField.getName().equals(name)) {
                return declaredField;
            }
        }
        if (clazz.getSuperclass() != null && clazz.getSuperclass() != Object.class) {
            return this.getPrototypeChainField(clazz.getSuperclass(), name);
        }
        return null;
    }
}

