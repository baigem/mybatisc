package cmc.mybatisc.utils;

import cmc.mybatisc.annotation.FieldSelect;
import cmc.mybatisc.base.CodeStandardEnum;
import cmc.mybatisc.config.interfaces.TableEntity;
import cmc.mybatisc.utils.reflect.GenericType;
import cmc.mybatisc.utils.reflect.ReflectUtils;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.scripting.xmltags.XMLLanguageDriver;
import org.apache.ibatis.session.SqlSession;
import org.springframework.util.StringUtils;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.*;

/**
 * 映射语句utils
 *
 * @author cmc
 * &#064;date  2023/05/25
 */
public class MapperStrongUtils {
    public static String create(SqlSession sqlSession, Class<?> mapper, Method method, String sql) {
        FieldSelect annotation = method.getAnnotation(FieldSelect.class);
        GenericType genericType = GenericType.forMethod(mapper, method);
        Class<?> returnType = genericType.last(method.getReturnType());

        XMLLanguageDriver xmlLanguageDriver = new XMLLanguageDriver();
        if (method.getReturnType() == Map.class) {
            // 只有映射为false的时候 returnType才是Map.class,不然就是对应的value实体
            if (annotation != null && !annotation.mapping()) {
                returnType = method.getReturnType();
            }
        }
        SqlSource sqlSource = xmlLanguageDriver.createSqlSource(sqlSession.getConfiguration(), sql, Map.class);
        String id = mapper.getName() + "." + method.getName() + "_strong";

        ArrayList<ResultMapping> objects = new ArrayList<>();
        Class<?> returnTypeClassBottom = genericType.last(method.getReturnType());

        // 遍历进行映射，防止mybatis每次都自动映射，浪费性能
        CodeStandardEnum handler = annotation != null ? annotation.nameMode() : CodeStandardEnum.UNDERLINE;
        for (Field declaredField : ReflectUtils.getAllField(returnTypeClassBottom)) {
            try {
                ResultMapping build = new ResultMapping.Builder(sqlSession.getConfiguration(), declaredField.getName(), handler.handler(declaredField.getName()), declaredField.getType()).build();
                objects.add(build);
            } catch (Exception ignore) {
            }
        }
        ResultMap.Builder builder = new ResultMap.Builder(sqlSession.getConfiguration(), id, returnType, objects);

        MappedStatement mappedStatement = new MappedStatement.Builder(sqlSession.getConfiguration(), id, sqlSource, SqlCommandType.SELECT)
                .resultMaps(Collections.singletonList(builder.build())).build();
        sqlSession.getConfiguration().addMappedStatement(mappedStatement);
        return id;
    }

    public static Type[] getType(Method method) {
        Type type = method.getGenericReturnType();
        if (type instanceof ParameterizedTypeImpl) {
            return ((ParameterizedTypeImpl) type).getActualTypeArguments();
        }
        return new Type[]{};
    }

    /**
     * 判断指定类是否是List的子类
     *
     * @param clz clz
     * @return boolean
     */
    public static boolean isListTypeClass(Class<?> clz) {
        try {
            if (clz == List.class || clz == Set.class || clz.getName().equals("[Ljava.lang.Long;")) {
                return true;
            }
            return clz.newInstance() instanceof Collection;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取表名
     *
     * @param entity 实体
     * @param name   名称
     * @return {@link String}
     */
    public static String getTableName(Class<?> entity, String name) {
        TableName tableName = entity.getAnnotation(TableName.class);
        if (tableName != null && StringUtils.hasText(tableName.value())) {
            return tableName.value();
        }
        if (StringUtils.hasText(name)) {
            return name;
        }
        return CodeStandardEnum.UNDERLINE.handler(entity.getSimpleName());
    }

    /**
     * 获取关键字字段
     *
     * @param entity 实体
     * @return {@link Field}
     */
    public static Field getKeyField(Class<?> entity) {
        Field[] declaredFields = entity.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            if (declaredField.isAnnotationPresent(TableId.class)) {
                return declaredField;
            }
        }
        // 获取父级
        if (entity.getSuperclass() != null && entity.getSuperclass() != Object.class) {
            return getKeyField(entity.getSuperclass());
        }
        return null;
    }

    public static String getTableName(String na, String name) {
        if (StringUtils.hasText(name)) {
            return name;
        }
        return na;
    }

    /**
     * 获取字段名称
     *
     * @param entity 实体
     * @return {@link List}<{@link String}>
     */
    public static List<String> getFieldNames(Class<?> entity, CodeStandardEnum codeStandardEnum) {
        List<String> list = new ArrayList<>();
        List<Field> declaredFields = ReflectUtils.getAllField(entity);
        for (Field declaredField : declaredFields) {
            if (declaredField.isAnnotationPresent(TableId.class)) {
                if (StringUtils.hasText(declaredField.getAnnotation(TableId.class).value())) {
                    list.add(declaredField.getAnnotation(TableId.class).value());
                    continue;
                }
            } else if (declaredField.isAnnotationPresent(TableField.class)) {
                TableField annotation = declaredField.getAnnotation(TableField.class);
                // 不属于表结构，进行下一个
                if (!annotation.exist()) {
                    continue;
                }
                if (StringUtils.hasText(annotation.value())) {
                    list.add(declaredField.getAnnotation(TableField.class).value());
                    continue;
                }
            } else {
                continue;
            }
            // 把字段名当做表名称
            list.add(codeStandardEnum.handler(declaredField.getName()));
        }
        return list;
    }
}
