package cmc.mybatisc.base;

import cmc.mybatisc.annotation.Dict;
import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * 基本字典
 *
 * @author 程梦城
 * @version 1.0.0
 * &#064;date  2023/07/07
 */
@SuppressWarnings("unchecked")
@JsonDeserialize(using = BaseDict.BaseDictDeserializer.class)
@JsonSerialize(using = BaseDict.BaseDictSerializer.class)
public interface BaseDict<T extends Serializable> extends IEnum<T> {
    /**
     * 设置状态规则
     *
     * @param arr         arr
     * @param targetState 目标状态
     */
    static void registerChangeRule(BaseDict<?> targetState, BaseDict<?>... arr) {
        BaseEnum.setStateRule(targetState, arr);
    }

    /**
     * 寄存器不可变
     */
    static void registerImmutable(BaseDict<?> dict, String msg) {
        BaseEnum.IMMUTABLE.put(dict, msg);
    }

    /**
     * 状态变化
     *
     * @param targetState  目标状态
     * @param currentState 电流状态
     */
    @SuppressWarnings("unused")
    static <T extends Serializable> void changeCheck(BaseDict<T> targetState, T currentState) {
        changeCheck(targetState, (BaseDict<T>) BaseDict.getDict(targetState.getClass(), currentState));
    }

    /**
     * 状态变化
     *
     * @param targetState  目标状态
     * @param currentState 电流状态
     */
    static <T extends Serializable, B extends BaseDict<T>> void changeCheck(B targetState, B currentState) {
        // 判断是否为不可变状态
        String msg = BaseEnum.IMMUTABLE.get(currentState);
        if (msg != null) {
            throw new RuntimeException(msg);
        }
        for (BaseDict<T> tBaseDict : BaseEnum.getStateRule(targetState)) {
            if (tBaseDict.equals(currentState)) {
                return;
            }
        }
        String name = Optional.ofNullable(targetState.getClass().getAnnotation(Dict.class)).map(Dict::value).orElse(targetState.getClass().getSimpleName());
        // 进行异常报警
        throw new RuntimeException(name + "不允许从[" + currentState.getMsg() + "]变更为[" + targetState.getMsg() + "]");
    }

    /**
     * 通过key获取对应的字典类型
     *
     * @param key 钥匙
     * @return {@link BaseDict}<{@link T}>
     */
    static <T extends Serializable, C extends BaseDict<T>> BaseDict<T> getDict(Class<C> clazz, T key) {
        return getStrValueDict(clazz, String.valueOf(key));
    }

    static <T extends Serializable, C extends BaseDict<T>> BaseDict<T> getStrValueDict(Class<C> clazz, String key) {
        for (C c : clazz.getEnumConstants()) {
            if (BaseEnum.get(c).getStrValue().equals(key)) {
                return c;
            }
        }
        throw new RuntimeException("未知的字典值：" + key);
    }

    /**
     * 初始化
     *
     * @param msg   味精
     * @param value 值
     */
    @SneakyThrows
    default void initialize(String msg, T value) {
        if (BaseEnum.get(this) != null) {
            throw new RuntimeException("每个枚举的初始化只允许执行一次");
        }
        BaseEnum.put(this, value, msg);
    }

    @Override
    default T getValue() {
        return BaseEnum.<T>get(this).getValue();
    }
    default String getStrValue() {
        return BaseEnum.<T>get(this).getStrValue();
    }

    default String getMsg() {
        return BaseEnum.<T>get(this).getMsg();
    }

    /**
     * 下一个
     *
     * @param target 目标
     */
    default void changeCheck(BaseDict<T> target) {
        BaseDict.changeCheck(target, this);
    }

    /**
     * 获取字典对象
     *
     * @param key 钥匙
     * @return {@link BaseDict}<{@link T}>
     */
    @SuppressWarnings("unused")
    default <V extends BaseDict<T>> V getDict(T key) {
        return null;
    }

    String toString();

    /**
     * 基本枚举
     *
     * @author 程梦城
     * @version 1.0.0
     * &#064;date  2023/07/07
     */
    @Getter
    class BaseEnum<T> {
        /**
         * 枚举容器列表
         */
        private final static Map<BaseDict<?>, BaseEnum<?>> ENUM_CONTAINER_MAP = new HashMap<>();
        /**
         * 状态规则
         */
        private final static Map<BaseDict<?>, BaseDict<?>[]> STATE_RULE = new HashMap<>();
        /**
         * 不可变状态
         */
        protected final static Map<BaseDict<?>, String> IMMUTABLE = new HashMap<>();

        /**
         * 说明
         */
        private final String msg;
        /**
         * 内容
         */
        private final T value;

        private final String strValue;

        public BaseEnum(String msg, T value) {
            this.msg = msg;
            this.value = value;
            this.strValue = String.valueOf(value);
        }

        public static <T> void put(BaseDict<?> baseDict, T value, String msg) {
            ENUM_CONTAINER_MAP.put(baseDict, new BaseEnum<>(msg, value));
        }

        @SuppressWarnings("unchecked")
        public static <T> BaseEnum<T> get(BaseDict<?> baseDict) {
            return (BaseEnum<T>) ENUM_CONTAINER_MAP.get(baseDict);
        }


        /**
         * 获取某个字典中所有的字段
         *
         * @param clazz 克拉兹
         * @return {@link List}<{@link C}>
         */
        @SuppressWarnings("unchecked")
        public static <T extends Serializable, C extends BaseDict<T>> List<C> getList(Class<C> clazz) {
            return (List<C>) ENUM_CONTAINER_MAP.keySet().stream().filter(baseDict -> baseDict.getClass() == clazz).collect(Collectors.toList());
        }

        /**
         * 设置状态规则
         *
         * @param targetState 目标状态
         * @param allowState  允许状态
         */
        public static void setStateRule(BaseDict<?> targetState, BaseDict<?>[] allowState) {
            STATE_RULE.put(targetState, allowState);
        }

        /**
         * 获取状态规则
         *
         * @param targetState 目标状态
         */
        @SuppressWarnings("unchecked")
        public static <T extends Serializable> BaseDict<T>[] getStateRule(BaseDict<T> targetState) {
            return (BaseDict<T>[]) STATE_RULE.get(targetState);
        }
    }


    /**
     * BaseDict的反序列化器
     *
     * @author 程梦城
     * @version 1.0.0
     * &#064;date  2023/11/22
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    @NoArgsConstructor
    class BaseDictDeserializer extends JsonDeserializer<BaseDict> implements ContextualDeserializer {
        @Getter
        private JavaType type;
        private Class<BaseDict> typeClass;

        public BaseDictDeserializer(JavaType type) {
            this.type = type;
            this.typeClass = (Class<BaseDict>) type.getRawClass();
        }

        @Override
        @SneakyThrows
        public BaseDict deserialize(JsonParser p, DeserializationContext ctxt) {
            return BaseDict.getStrValueDict(this.typeClass, p.getValueAsString());
        }

        @Override
        public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) {
            JavaType type = ctxt.getContextualType() != null
                    ? ctxt.getContextualType()
                    : property.getMember().getType();
            return new BaseDictDeserializer(type);
        }
    }

    /**
     * BaseDict的序列化程序
     *
     * @author 程梦城
     * @version 1.0.0
     * &#064;date  2023/11/22
     */
    @SuppressWarnings({"rawtypes"})
    class BaseDictSerializer extends JsonSerializer<BaseDict> {
        @Override
        public void serialize(BaseDict value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeObject(value.getValue());
        }
    }
}
