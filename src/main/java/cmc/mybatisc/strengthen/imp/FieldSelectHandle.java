package cmc.mybatisc.strengthen.imp;

import cmc.mybatisc.annotation.FieldSelect;
import cmc.mybatisc.config.DelFlagConfig;
import cmc.mybatisc.config.MybatisScannerConfigurer;
import cmc.mybatisc.core.util.TableStructure;
import cmc.mybatisc.model.FieldSelectDataSource;
import cmc.mybatisc.model.ParamAnnotation;
import cmc.mybatisc.parser.SqlParser;
import cmc.mybatisc.strengthen.BaseStrengthen;
import cmc.mybatisc.utils.MapperStrongUtils;
import cmc.mybatisc.utils.SqlUtils;
import org.apache.ibatis.session.SqlSession;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * 字段查询句柄
 *
 * @author cmc
 * &#064;date  2023/05/25
 */
public class FieldSelectHandle extends BaseStrengthen {
    public FieldSelectHandle(SqlSession sqlSession, Class<?> mapper) {
        super(sqlSession, mapper);
    }

    /**
     * 创建代理方法
     *
     * @param method 方法
     * @return {@link Function}<{@link Object[]}, {@link Object}>
     */
    @Override
    public Function<Object[], Object> createdProxyMethod(Method method) {
        SqlParser sqlParser = this.createdSql(method);
        // 创建一些映射的对象，并返回引用id
        String id = MapperStrongUtils.create(this.sqlSession, this.mapper, method, sqlParser.getSql());
        FieldSelectDataSource fieldQuery = FieldSelectDataSource.generate(method.getAnnotation(FieldSelect.class));
        fieldQuery.setMethod(method);
        fieldQuery.setId(id);
        // 判断是否需要进行key->value 处理
        return super.generateProxyMethod(fieldQuery,sqlParser);
    }

    /**
     * 创建sql
     *
     * @param method 方法
     * @return {@link String}
     */
    @Override
    public SqlParser createdSql(Method method) {
        SqlParser sqlParser = new SqlParser();
        Map<String, Function<?, Serializable>> dy = sqlParser.getParameters();
        DelFlagConfig delFlagConfig = MybatisScannerConfigurer.getBeanFactory().getBean(DelFlagConfig.class);
        FieldSelect fieldSelect = method.getAnnotation(FieldSelect.class);
        TableStructure table = TableStructure.getTableStructure(fieldSelect.table(), mapperParser.getTableStructure());
        if (table == null || !StringUtils.hasText(table.getName())) {
            throw new IllegalArgumentException("table name is not empty");
        }
        // 获取入参
        StringBuilder sql = new StringBuilder();
        Class<?> returnType = method.getReturnType();
        String fieldName = super.getHandleFieldString();
        if (returnType == Integer.class || returnType == Long.class || returnType == int.class || returnType == long.class) {
            fieldName = "count(*)";
        }
        sql.append("<script> select ").append(fieldName).append(" from ").append(table).append(" as ").append(table.getAlias()).append(" <where> ");
        // 添加逻辑删除
        sql.append(delFlagConfig.generateSelectSql(dy,super.mapperParser.getTableStructure(),"",""));

        Parameter[] parameters = method.getParameters();
        // 排序列表
        List<ParamAnnotation> sortList = new ArrayList<>();
        // 遍历入参
        for (Parameter parameter : parameters) {
            // 判断是否是列表
            ParamAnnotation param = ParamAnnotation.generate(parameter);
            // 判断是否排序
            if (param.sort) {
                sortList.add(param);
            }
            // 获取完整的字段
            String field = table.getCompleteFieldName(param.value, fieldSelect.removeSuffix());
            if (MapperStrongUtils.isListTypeClass(parameter.getType())) {
                if (fieldSelect.allowNull() || param.isNull) {
                    if (param.isNull) {
                        sql.append("<choose>");
                        sql.append("<when test=\"").append(param.value).append(".size() != 0 \">");
                        sql.append(param.oan.getValue()).append(" ").append(field).append(" in ").append("<foreach item='item' collection='").append(param.value).append("' open='(' separator=',' close=')'>#{item}</foreach>");
                        sql.append("</when>");
                        sql.append("<otherwise>");
                        if (parameter.getType() == String.class || parameter.getType() == char.class) {
                            sql.append(param.oan.getValue()).append(" ").append("(").append(field).append(" is null or ").append(field).append(" = '' )");
                        } else {
                            sql.append(param.oan.getValue()).append(" ").append(field).append(" is null");
                        }
                        sql.append("</otherwise>");
                        sql.append("</choose>");
                    } else {
                        sql.append("<if test=\"").append(param.value).append(".size() != 0 \">");
                        sql.append(param.oan.getValue()).append(" ").append(field).append(" in ").append("<foreach item='item' collection='").append(param.value).append("' open='(' separator=',' close=')'>#{item}</foreach>");
                        sql.append("</if>");
                    }
                } else {
                    sql.append(param.oan.getValue()).append(" ").append(param.left.value)
                            .append(field).append(" in ").append("<foreach item='item' collection='")
                            .append(param.value).append("' open='(' separator=',' close=')'>#{item}</foreach>")
                            .append(param.right.value);
                }
            } else {
                // 判断是否模糊搜索
                String sq;
                if (fieldSelect.like() || param.like) {
                    sq = param.oan.getValue() + " " + param.left.value + field + " like concat('%',#{" + param.value + "},'%') " + param.right.value;
                } else {
                    sq = param.oan.getValue() + " " + param.left.value + field + " = #{" + param.value + "} " + param.right.value;
                }
                if (fieldSelect.allowNull() || param.isNull) {
                    if (param.isNull) {
                        sql.append("<choose>");
                        if (parameter.getType() == String.class || parameter.getType() == char.class) {
                            sql.append("<when test=\"").append(param.value).append(" !=null and ").append(param.value).append(" != '' \">");
                        } else {
                            sql.append("<when test=\"").append(param.value).append(" !=null \">");
                        }
                        sql.append(sq);
                        sql.append("</when>");
                        sql.append("<otherwise>");
                        if (parameter.getType() == String.class || parameter.getType() == char.class) {
                            sql.append(param.oan.getValue()).append(" ").append(param.left.value).append("(").append(field).append(" is null or ").append(field).append(" = '' )").append(param.right.value);
                        } else {
                            sql.append(param.oan.getValue()).append(" ").append(param.left.value).append(field).append(" is null").append(param.right.value);
                        }
                        sql.append("</otherwise>");
                        sql.append("</choose>");
                    } else {
                        sql.append("<if test=\"").append(param.value).append("!= null \">");
                        sql.append(sq);
                        sql.append("</if>");
                    }
                } else {
                    sql.append(sq);
                }
            }
        }
        sql.append("</where>");
        if (!sortList.isEmpty()) {
            sql.append("ORDER BY ");
            for (ParamAnnotation paramAnnotation : sortList) {
                String field = table.getCompleteFieldName(paramAnnotation.value, fieldSelect.removeSuffix());
                sql.append(String.format("%s %s", field, paramAnnotation.sortRule)).append(", ");
            }
        }
        sqlParser.setSql(sql.toString().replaceAll(" not *$| or *$| and *$|where *$|, $", "") + "</script>");
        return sqlParser;
    }

}
