package cmc.mybatisc.utils.sugar;

import cmc.mybatisc.utils.sugar.custom.State;
import cmc.mybatisc.utils.sugar.java.Switch;

import java.util.function.Function;

/**
 * 如果 未完成
 *
 * @author 程梦城
 * @version 1.0.0
 * &#064;date  2023/12/30
 */
public class If<T>{
    private T value;

    public If(T value) {
        this.value = value;
    }

    public static <T> If<T> of(T value) {
        return new If<>(value);
    }

    /**
     * 开关c
     *
     * @param value 值
     * @return {@link If}
     */
    public Switch Switch(Object value) {
        return new Switch(value);
    }

    /**
     * 状态
     *
     * @param state 状态
     * @return {@link State}
     */
    public static State state(boolean state){
        return new State(state);
    }

    @SuppressWarnings("unchecked")
    public <R> If<R> map(Function<T,R> function){
        if(value != null){
            this.value = (T) function.apply(this.value);
        }
        return (If<R>) this;
    }

}
