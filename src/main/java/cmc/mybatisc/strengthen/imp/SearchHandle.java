package cmc.mybatisc.strengthen.imp;

import cmc.mybatisc.annotation.Search;
import cmc.mybatisc.config.DelFlagConfig;
import cmc.mybatisc.config.MybatisScannerConfigurer;
import cmc.mybatisc.config.interfaces.DelFlag;
import cmc.mybatisc.model.FieldSelectDataSource;
import cmc.mybatisc.model.QueryFieldCriteria;
import cmc.mybatisc.parser.JoinParser;
import cmc.mybatisc.parser.SearchParser;
import cmc.mybatisc.parser.SqlParser;
import cmc.mybatisc.strengthen.BaseStrengthen;
import cmc.mybatisc.utils.MapperStrongUtils;
import cmc.mybatisc.utils.SqlUtils;
import cn.hutool.extra.spring.SpringUtil;
import org.apache.ibatis.session.SqlSession;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SearchHandle extends BaseStrengthen {

    public SearchHandle(SqlSession sqlSession, Class<?> mapper) {
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
        FieldSelectDataSource search = FieldSelectDataSource.generate(method.getAnnotation(Search.class));
        SqlParser sqlParser = this.createdSql(method);
        // 创建一些映射的对象，并返回引用id
        String id = MapperStrongUtils.create(this.sqlSession, this.mapper, method, sqlParser.getSql());
        search.setId(id);
        search.setMethod(method);
        // 判断是否需要进行key->value 处理
        return super.generateProxyMethod(search,sqlParser);
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
        SearchParser searchParser = new SearchParser(method.getAnnotation(Search.class), method, this.mapperParser);
        // 表名
        String table = searchParser.getTable();

        String fieldName = String.join(",", searchParser.getDisplayField());

        Class<?> returnType = method.getReturnType();
        if (Number.class.isAssignableFrom(returnType)) {
            fieldName = "count(*)";
        }

        StringBuilder sql = new StringBuilder();
        sql.append("<script> select ").append(fieldName).append(" from ").append(table).append(" ").append(searchParser.getTableAlias());
        // 生成join
        for (JoinParser join : searchParser.getJoinParserList()) {
            sql.append(" ").append(join.getJoinType()).append(" join ")
                    .append(join.getTable()).append(" ")
                    .append(join.getTableAlias()).append(" on ")
                    .append(join.getTableAlias()).append(".").append(SqlUtils.packageField(join.getField())).append(" = ")
                    .append(join.getLinkTableAlias()).append(".").append(join.getLinkField());
        }
        sql.append(" <where> ");
        for (JoinParser joinParser : searchParser.getJoinParserList()) {
            // 添加逻辑删除
            sql.append(delFlagConfig.generateSelectSql(dy,joinParser.getEntityParser(),"",""));
        }
        // 添加逻辑删除
        sql.append(delFlagConfig.generateSelectSql(dy,searchParser.getMapperParser().getEntityParser(),"",""));
        // 需要进行查询的字段集合
        List<QueryFieldCriteria> parameterList = QueryFieldCriteria.buildBySearch(searchParser);
        if (!parameterList.isEmpty()) {
            // 生成sql
            sql.append(SqlUtils.generateSql(parameterList));
        }
        sql.append("</where>");
        // 分组
        List<QueryFieldCriteria> groupList = parameterList.stream().filter(QueryFieldCriteria::isGroup).collect(Collectors.toList());
        if (!groupList.isEmpty()) {
            sql.append(SqlUtils.generateGroupSql(groupList));
        }
        // 排序
        List<QueryFieldCriteria> sortList = parameterList.stream().filter(QueryFieldCriteria::isSort).sorted(Comparator.comparingInt(QueryFieldCriteria::getSortNo)).collect(Collectors.toList());
        // 判断是否需要逻辑删除判断
        if (!sortList.isEmpty()) {
            sql.append(SqlUtils.generateSortSql(sortList));
        }

        sql.append("</script>");
        sqlParser.setSql(sql.toString());
        return sqlParser;
    }
}
