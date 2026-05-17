package com.joj.common.web.handler.mybatis;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/4/24 14:20
 */

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 通用 JSON 类型处理器抽象基类。
 * <p>
 * 作用：
 * 1. Java 对象 -> JSON 字符串 -> 存入数据库 JSON/TEXT 字段
 * 2. 数据库 JSON/TEXT 字段 -> JSON 字符串 -> Java 对象
 * <p>
 * 支持：
 * 1. 普通对象，例如 JudgeConfig
 * 2. 泛型集合，例如 List<Sample>、List<String>
 * <p>
 * 使用方式：
 * <p>
 * public class SampleListTypeHandler extends AbstractJsonTypeHandler<List<Sample>> {
 * }
 * <p>
 * 注意：
 * 1. 不能直接在 MyBatis 中使用 AbstractJsonTypeHandler
 * 2. 必须创建具体子类，并在子类上写明泛型类型
 * 3. 具体子类应直接继承 AbstractJsonTypeHandler<T>
 */
public abstract class AbstractJsonTypeHandler<T> extends BaseTypeHandler<T> {

    /**
     * Jackson ObjectMapper。
     * <p>
     * findAndRegisterModules() 会自动注册 classpath 中存在的 Jackson 模块。
     * 如果项目中引入了 jackson-datatype-jsr310，
     * 它可以支持 Java 8 的 LocalDate、LocalDateTime 等时间类型。
     */
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    /**
     * Jackson 反序列化时需要的完整类型信息。
     * <p>
     * 不能只用 Class<?>，因为 Class<?> 无法表达 List<Sample> 这种泛型类型。
     * JavaType 可以保存完整泛型信息，例如：
     * <p>
     * List<Sample>
     * List<String>
     * Map<String, Object>
     */
    private final JavaType javaType;

    /**
     * 构造方法。
     * <p>
     * 这里通过反射读取子类声明的泛型类型。
     * <p>
     * 例如：
     * <p>
     * public class SampleListTypeHandler extends AbstractJsonTypeHandler<List<Sample>> {
     * }
     * <p>
     * getClass().getGenericSuperclass() 可以拿到：
     * <p>
     * AbstractJsonTypeHandler<List<Sample>>
     * <p>
     * 然后再把 List<Sample> 转换成 Jackson 可识别的 JavaType。
     */
    protected AbstractJsonTypeHandler() {
        Type genericSuperclass = getClass().getGenericSuperclass();

        if (!(genericSuperclass instanceof ParameterizedType)) {
            throw new IllegalArgumentException("JsonTypeHandler must directly specify generic type, for example: AbstractJsonTypeHandler<List<Sample>>");
        }

        ParameterizedType parameterizedType = (ParameterizedType) genericSuperclass;

        Type actualType = parameterizedType.getActualTypeArguments()[0];

        this.javaType = MAPPER.getTypeFactory().constructType(actualType);
    }

    /**
     * Java 对象写入数据库前会调用这个方法。
     * <p>
     * MyBatis 在执行 insert/update 时，会把实体字段传进来。
     * 这里将 Java 对象序列化成 JSON 字符串，然后写入 PreparedStatement。
     * <p>
     * 对于 MySQL json 字段，ps.setString(...) 通常可以正常写入；
     * MySQL 会校验该字符串是否是合法 JSON。
     */
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, T parameter, JdbcType jdbcType) throws SQLException {
        try {
            String json = MAPPER.writeValueAsString(parameter);
            ps.setString(i, json);
        } catch (JsonProcessingException e) {
            throw new SQLException("Failed to serialize object to JSON", e);
        }
    }

    /**
     * 根据字段名读取数据库结果。
     * <p>
     * MyBatis 查询 resultMap 映射时可能会调用这个方法。
     */
    @Override
    public T getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return parse(rs.getString(columnName));
    }

    /**
     * 根据字段下标读取数据库结果。
     * <p>
     * 某些场景下 MyBatis 会按 columnIndex 读取字段值。
     */
    @Override
    public T getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return parse(rs.getString(columnIndex));
    }

    /**
     * 从存储过程结果中读取字段。
     * <p>
     * 普通业务一般用不到，但 BaseTypeHandler 要求实现。
     */
    @Override
    public T getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return parse(cs.getString(columnIndex));
    }

    /**
     * JSON 字符串反序列化成 Java 对象。
     * <p>
     * 如果数据库字段为 null 或空字符串，这里返回 null。
     * <p>
     * 注意：
     * 如果你的字段是 List<Sample>，返回 null 意味着业务层需要判空。
     * 如果你希望空值返回空 List，可以为 List 类型单独写 Handler。
     */
    private T parse(String json) throws SQLException {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }

        try {
            return MAPPER.readValue(json, javaType);
        } catch (JsonProcessingException e) {
            throw new SQLException("Failed to deserialize JSON to " + javaType, e);
        }
    }
}


