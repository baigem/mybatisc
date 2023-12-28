package cmc.mybatisc.typehandler;

import cmc.mybatisc.base.model.StringList;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.io.Serializable;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StringListTypeHandler<T extends Serializable> extends BaseTypeHandler<StringList<T>> {

    @Override
    public void setNonNullParameter(PreparedStatement preparedStatement, int i, StringList<T> stringList, JdbcType jdbcType) throws SQLException {
        // 设置非空参数到 PreparedStatement
        preparedStatement.setString(i, stringList.toString());
    }

    @Override
    public StringList<T> getNullableResult(ResultSet resultSet, String s) throws SQLException {
        // 从 ResultSet 获取数据库数据并映射到 Java 对象
        return new StringList<>(resultSet.getString(s), null);
    }

    @Override
    public StringList<T> getNullableResult(ResultSet resultSet, int i) throws SQLException {
        // 从 ResultSet 获取数据库数据并映射到 Java 对象
        return new StringList<>(resultSet.getString(i), null);
    }

    @Override
    public StringList<T> getNullableResult(CallableStatement callableStatement, int i) throws SQLException {
        // 从 CallableStatement 获取数据库数据并映射到 Java 对象
        return new StringList<>(callableStatement.getString(i), null);
    }
}
