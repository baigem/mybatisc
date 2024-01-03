package cmc.mybatisc.utils.sugar.custom;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 状态
 *
 * @author 程梦城
 * @version 1.0.0
 * &#064;date  2023/12/30
 */
public class State {
    private boolean state;
    public State(boolean state) {
        this.state = state;
    }

    /**
     * 状态
     *
     * @param state 状态
     * @return {@link State}
     */
    public State state(boolean state){
        return new State(state);
    }

    /**
     * 真
     *
     * @return {@link State}
     */
    public State really(boolean state){
        return this;
    }

    /**
     * 真
     *
     * @return {@link State}
     */
    public State really(Supplier<?> consumer){
        if(state){
            consumer.get();
        }
        return this;
    }

    /**
     * 失败
     *
     * @return {@link State}
     */
    public State fail(Consumer<State> consumer){
        if(!state){
            consumer.accept(new State(false));
        }
        return this;
    }
}
