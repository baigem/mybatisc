package cmc.mybatisc.strengthen;

import cmc.mybatisc.config.MybatisScannerConfigurer;
import cmc.mybatisc.config.interfaces.MybatiscConfig;
import cmc.mybatisc.model.FieldSelectDataSource;
import cmc.mybatisc.model.ParamAnnotation;
import cmc.mybatisc.parser.MapperParser;
import cmc.mybatisc.parser.SqlParser;
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
     * mybatisc配置
     */
    protected MybatiscConfig mybatiscConfig;
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
        // 获取配置bean
        this.mybatiscConfig = MybatisScannerConfigurer.getBeanFactory().getBean(MybatiscConfig.class);
        this.sqlSession = sqlSession;
        this.mapper = mapper;
        this.mapperParser = new MapperParser(this.mybatiscConfig, mapper);

        // 处理字段名称
        this.handleFieldString = this.mapperParser.getTableStructure().getFieldNames().stream().map(field -> {
            if (field.startsWith("`") || field.equals("*")) {
                return field;
            }
            return "`" + field + "`";
        }).collect(Collectors.joining(","));
        TABLE_STRENGTHEN.put(this.mapperParser.getTableStructure().getName(), this);
    }

    /**
     * 创建地图
     *
     * @param parameters 参数
     * @param args       args
     * @return {@link Map}<{@link String}, {@link Object}>
     */
    public Map<String, Object> createMap(Parameter[] parameters, Object[] args, SqlParser sqlParser) {
        HashMap<String, Object> map = new HashMap<>();
        for (int i = 0; i < parameters.length; i++) {
            String name = parameters[i].getName();
            if (parameters[i].isAnnotationPresent(Param.class)) {
                name = parameters[i].getAnnotation(Param.class).value();
            }
            map.put(name, args[i]);
        }
        // 添加动态参数
        sqlParser.getParameters().forEach((k,v)->{
            map.put(k,v.apply(null));
        });
        return map;
    }

    /**
     * 地图键
     *
     * @param method    方法
     * @param fieldName 字段名称
     * @param id        id
     * @return {@link Function}<{@link Object[]}, {@link Object}>
     */
    protected Function<Object[], Object> mapKey(boolean full, Method method, String fieldName, String id,SqlParser sqlParser) {
        if (!StringUtils.hasText(fieldName) && this.mapperParser.getTableStructure().getPrimaryKey() == null) {
            throw new RuntimeException("启动mapping属性，主键id未配置，请配置主键id注解@TableId，或者使用mappingField配置");
        }
        // 获取泛型类型
        GenericType genericType = GenericType.forMethod(this.mapper, method);
        String keyName = StringUtils.hasText(fieldName) ? fieldName : this.mapperParser.getTableStructure().getPrimaryKeyName();
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
            List<Object> objects = this.sqlSession.selectList(id, this.createMap(method.getParameters(), p, sqlParser));
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
     * 生成代理方法
     *
     * @param fieldSelectDataSource 字段查询数据源
     * @return {@link Function}<{@link Object[]}, {@link Object}>
     */
    public Function<Object[], Object> generateProxyMethod(FieldSelectDataSource fieldSelectDataSource,SqlParser sqlParser) {
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
            return this.mapKey(isNull, method, fieldSelectDataSource.getMappingField(), id, sqlParser);
        } else if (MapperStrongUtils.isListTypeClass(returnType)) {
            return (p) -> this.checkInputParameters(finalIsNull, p) ? this.sqlSession.selectList(id, this.createMap(method.getParameters(), p,sqlParser)) : new ArrayList<>();
        } else if (returnType == Optional.class) {
            // 参数转换
            return (p) -> Optional.ofNullable(this.checkInputParameters(finalIsNull, p) ? this.sqlSession.selectOne(id, this.createMap(method.getParameters(), p, sqlParser)) : null);
        } else {
            return (p) -> this.checkInputParameters(finalIsNull, p) ? this.sqlSession.selectOne(id, this.createMap(method.getParameters(), p, sqlParser)) : null;
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

