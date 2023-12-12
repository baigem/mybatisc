package cmc.mybatisc.core;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 线程任务
 *
 * @author 程梦城
 * @version 1.0.0
 * &#064;date  2023/07/07
 */
public class ThreadTask<T> implements Future<T> {
    private final Supplier<T> supplier;

    public ThreadTask(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    /**
     * 创建
     *
     * @param supplier 供应商
     * @return {@link ThreadTask}<{@link T}>
     */
    public static <T> ThreadTask<T> create(Supplier<T> supplier) {
        return new ThreadTask<>(supplier);
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public T get() {
        return supplier.get();
    }

    @Override
    public T get(long timeout,TimeUnit unit) {
        return supplier.get();
    }
}
