package cmc.mybatisc.base.model;

import cmc.mybatisc.utils.SplicingTools;
import cmc.mybatisc.utils.list.ListUtil;
import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import lombok.*;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 字符串列表
 *
 * @author 程梦城
 * @version 1.0.0
 * &#064;date  2023/12/01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@JsonDeserialize(using = StringList.StringListDeserializer.class)
@JsonSerialize(using = StringList.StringListSerializer.class)
@NoArgsConstructor
@SuppressWarnings({"unchecked", "rawtypes"})
public class StringList<T extends Serializable> extends ArrayList<T> implements CharacterString{
    /**
     * 分隔符
     */
    private String delimiter = SplicingTools.DELIMITER;


    public static <T extends Serializable,R> StringList<T> create(List<R> list, Function<R,T> get){
        return new StringList<>(ListUtil.extract(list,get));
    }

    public StringList(List<T> list) {
        super(list.size());
        this.addAll(list);
    }

    public StringList(T... array) {
        super(array.length);
        this.addAll(Arrays.asList(array));
    }

    public StringList(String values, Class<T> first) {
        if(values == null) return;
        if(values.startsWith("[") && values.endsWith("]")){
            super.addAll(JSON.parseArray(values,first));
        }else{
            // 最后一位为分隔符
            this.delimiter = values.charAt(values.length() - 1)+"";
            super.addAll(SplicingTools.parse(values.substring(0,values.length()-1),this.delimiter,first));
        }
    }

    public StringList(List<String> list, Class<T> first) {
        super.addAll(list.stream().map(e->JSON.parseObject(e,first)).collect(Collectors.toList()));
    }

    /**
     * 单例列表
     *
     * @param o o
     * @return {@link StringList}<{@link T}>
     */
    public static <T extends Serializable> StringList<T> singletonList(T o){
        return new StringList<>(o);
    }
    /**
     * 或转换
     *
     * @param clazz 克拉兹
     * @return {@link StringList}<{@link T}>
     */
    public StringList<T> orConversion(Class<T> clazz){
        // 元素为空或者元素是目标类的子类就不进行转换
        if(this.isEmpty() || clazz.isAssignableFrom(this.get(0).getClass())){
            return this;
        }
        List<T> list = new ArrayList<>(this.size());
        this.removeIf((Object e)-> list.add(JSON.parseObject(String.valueOf(e), clazz)));
        this.addAll(list);
        return this;
    }

    @Override
    public String toString() {
        return SplicingTools.generate(this.stream().map(String::valueOf).collect(Collectors.toList()),delimiter)+this.delimiter;
    }

    @NoArgsConstructor
    static
    class StringListDeserializer extends JsonDeserializer<StringList> implements ContextualDeserializer {
        @Getter
        private JavaType type;
        private Class<?> typeClass;

        public StringListDeserializer(JavaType type) {
            this.type = type;
            this.typeClass = type.getContentType().getRawClass();
        }


        @Override
        public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) {
            JavaType type = ctxt.getContextualType() != null
                    ? ctxt.getContextualType()
                    : property.getMember().getType();
            return new StringListDeserializer(type);
        }

        @Override
        @SneakyThrows
        public StringList deserialize(JsonParser parser, DeserializationContext ctxt) {
            if (parser.currentToken() == JsonToken.START_ARRAY) {
                List<String> list = new ArrayList<>();
                // 移动到数组的结束位置
                while (parser.nextToken() != JsonToken.END_ARRAY) {
                    list.add(parser.getText());
                }
                return new StringList(list,typeClass);
            } else {
                return new StringList(parser.getValueAsString(),typeClass);
            }
        }
    }

    /**
     * BaseDict的序列化程序
     *
     * @author 程梦城
     * @version 1.0.0
     * &#064;date  2023/11/22
     */
    static class StringListSerializer extends JsonSerializer<StringList> {
        @Override
        public void serialize(StringList value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeObject(new ArrayList<>(value));
        }
    }
}
