package cmc.mybatisc.strengthen.imp;

import cmc.mybatisc.annotation.FieldSelect;
import cmc.mybatisc.base.CodeStandardEnum;
import cmc.mybatisc.model.DelFlag;
import cmc.mybatisc.model.FieldSelectDataSource;
import cmc.mybatisc.model.ParamAnnotation;
import cmc.mybatisc.strengthen.BaseStrengthen;
import cmc.mybatisc.utils.MapperStrongUtils;
import cmc.mybatisc.utils.SqlUtils;
import org.apache.ibatis.session.SqlSession;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
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
        // 创建一些映射的对象，并返回引用id
        String id = MapperStrongUtils.create(this.sqlSession, this.mapper, method, this.createdSql(method));
        FieldSelectDataSource fieldQuery = FieldSelectDataSource.generate(method.getAnnotation(FieldSelect.class));
        fieldQuery.setMethod(method);
        fieldQuery.setId(id);
        // 判断是否需要进行key->value 处理
        return super.generateProxyMethod(fieldQuery);
    }

    /**
     * 创建sql
     *
     * @param method 方法
     * @return {@link String}
     */
    @Override
    public String createdSql(Method method) {
        FieldSelect fieldSelect = method.getAnnotation(FieldSelect.class);
        // 表名
        String table = MapperStrongUtils.getTableName(this.mapperParser.getTableName(), fieldSelect.table());
        if (!StringUtils.hasText(table)) {
            throw new IllegalArgumentException("table name is not empty");
        }
        // 是否逻辑删除
        DelFlag delFlag = super.getDelFlag(fieldSelect.delFlag());
        // 名称处理器
        CodeStandardEnum codeStandardEnum = this.getCodeStandardEnum(fieldSelect.nameMode());
        // 获取入参
        StringBuilder sql = new StringBuilder();
        Class<?> returnType = method.getReturnType();
        String fieldName = super.getHandleFieldString();
        if (returnType == Integer.class || returnType == Long.class || returnType == int.class || returnType == long.class) {
            fieldName = "count(*)";
        }
        sql.append("<script> select ").append(fieldName).append(" from ").append(table).append(" <where> ");

        if (delFlag != null) {
            // 添加逻辑删除
            sql.append(this.generateDeleteFlag("", " ", delFlag.fieldName, delFlag.notDeleteValue.toString(), delFlag.isDeleteTime));
        }

        Parameter[] parameters = method.getParameters();
        // 排序列表
        List<ParamAnnotation> sortList = new ArrayList<>();

        for (Parameter parameter : parameters) {
            // 判断是否是列表
            ParamAnnotation param = ParamAnnotation.generate(parameter);

            if (param.sort) {
                sortList.add(param);
            }
            String field = SqlUtils.packageField(getFieldName(codeStandardEnum, param.value, fieldSelect.removeSuffix()));
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
                String field = SqlUtils.packageField(getFieldName(codeStandardEnum, paramAnnotation.value, fieldSelect.removeSuffix()));
                final String s = "%s %s";
                sql.append(String.format(s, field, paramAnnotation.sortRule)).append(", ");
            }
        }
        return sql.toString().replaceAll(" not *$| or *$| and *$|where *$|, $", "") + "</script>";
    }

}
