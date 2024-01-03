package cmc.mybatisc.utils.sugar.java;

/**
 * 开关
 *
 * @author 程梦城
 * @version 1.0.0
 * &#064;date  2023/12/30
 */
public class Switch {
    private final Object value;
    private boolean end = false;

    public Switch(Object value) {
        this.value = value;
    }

    /**
     * 案例一次
     *
     * @param value 值
     * @return {@link Switch}
     */
    public Switch Case(Object value,Runnable runnable){
        if(end) return this;
        end = true;
        if(value.equals(this.value)){
            runnable.run();
        }
        return this;
    }

    /**
     * 案例结束
     *
     * @param value    值
     * @param runnable 可运行
     * @return {@link Switch}
     */
    public Switch CaseEnd(Object value,Runnable runnable){
        if(end) return this;
        if(value.equals(this.value)){
            runnable.run();
        }
        return this;
    }
}
