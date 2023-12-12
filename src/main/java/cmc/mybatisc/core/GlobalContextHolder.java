package cmc.mybatisc.core;

import cmc.mybatisc.utils.string.StringUtils;
import cn.hutool.core.thread.ThreadUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * 全局的上下文执行者
 * 包含异步任务
 * 以及错误报告
 * 所有的任务会在关键点等待所有任务完成
 */
@Slf4j
public class GlobalContextHolder {

    /**
     * 项目初始化任务
     */
    private static final List<Future<?>> EXECUTION_COMPLETED_BEFORE_PROJECT_LAUNCH = new ArrayList<>();

    /**
     * 异步任务
     */
    private static final ThreadLocal<List<Future<?>>> ASYNC_TASK = new ThreadLocal<>();

    /**
     * 请求结束后任务
     */
    private static final List<Future<?>> AFTER_REQUEST_ENDS_TASK = new ArrayList<>();

    /**
     * 在结束请求之前
     */
    private static final List<Future<?>> BEFORE_END_THE_REQUEST = new ArrayList<>();

    /**
     * 线程本地
     */
    private static final ThreadLocal<Map<String, Object>> THREAD_LOCAL = new ThreadLocal<>();
    /**
     * 锁
     */
    private static boolean lock = false;


    /**
     * 注册项目初始化任务
     *
     * @param runnable 可运行
     */
    public static void registerProjectInitializationTask(Runnable runnable){
        EXECUTION_COMPLETED_BEFORE_PROJECT_LAUNCH.add(ThreadUtil.execAsync(runnable));
    }

    /**
     * 等待项目完成初始化任务
     */
    public static void waitingProjectForInitializationTaskToComplete(){
        EXECUTION_COMPLETED_BEFORE_PROJECT_LAUNCH.removeIf(task->{
            try {
                task.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
            return true;
        });
    }

    /**
     * 请求运行前注册
     *
     * @param future 未来
     */
    public static void registerBeforeRequestRun(Future<?> future) {
        BEFORE_END_THE_REQUEST.add(future);
    }

    /**
     * 请求运行后注册
     *
     * @param future 未来
     */
    public static void registerAfterRequestRun(Future<?> future) {
        AFTER_REQUEST_ENDS_TASK.add(future);
    }

    /**
     * 请求前等待任务完成
     */
    public static void waitingForTaskCompletionBeforeRequest() {
        for (Future<?> future : BEFORE_END_THE_REQUEST) {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                log.error("请求前的任务出现异常，请尽快解决");
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 请求后等待任务完成
     */
    public static void waitForTaskToCompleteAfterRequest() {
        for (Future<?> future : AFTER_REQUEST_ENDS_TASK) {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                log.error("请求前的任务出现异常，请尽快解决");
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 添加异步任务
     */
    public static void addFuture(Future<?> future) {
        while (lock) {
            ThreadUtil.sleep(100);
        }
        getSyncTask().add(future);
    }

    /**
     * 获取当前线程的错误信息
     */
    public static String getErrorMag() {
        return get(GlobalConstants.ERROR_MSG, "");
    }

    /**
     * 设置当前线程的错误信息
     */
    public static void setErrorMag(String msg) {
        set(GlobalConstants.ERROR_MSG, msg);
    }

    /**
     * 获取当前线程的错误信息
     */
    public static Exception getError() {
        return get(GlobalConstants.ERROR, new Exception("无错误"));
    }

    /**
     * 设置当前线程的错误信息
     */
    public static void setError(Exception error) {
        set(GlobalConstants.ERROR, error);
    }

    /**
     * 设置当前线程的错误信息
     */
    public static void setError(String msg, Exception error) {
        set(GlobalConstants.ERROR_MSG, msg);
        set(GlobalConstants.ERROR, error);
    }

    /**
     * 等待任务全部完成
     */
    public synchronized static void waitingForTaskCompletion() {
        // 加锁
        lock = true;
        getSyncTask().removeIf(e -> {
            try {
                e.get();
            } catch (InterruptedException | ExecutionException ex) {
                throw new RuntimeException(ex);
            }
            return true;
        });
        lock = false;
    }


    /**
     * 获取本地地图
     *
     * @return {@link Map}<{@link String}, {@link Object}>
     */
    protected static List<Future<?>> getSyncTask() {
        List<Future<?>> futures = ASYNC_TASK.get();
        if (futures == null) {
            futures = new ArrayList<>();
            ASYNC_TASK.set(futures);
        }
        return futures;
    }


    /**
     * 获取本地地图
     *
     * @return {@link Map}<{@link String}, {@link Object}>
     */
    protected static Map<String, Object> getLocalMap() {
        Map<String, Object> map = THREAD_LOCAL.get();
        if (map == null) {
            map = new ConcurrentHashMap<>();
            THREAD_LOCAL.set(map);
        }
        return map;
    }


    protected static void set(String key, Object value) {
        Map<String, Object> map = getLocalMap();
        map.put(key, value == null ? StringUtils.EMPTY : value);
    }

    /**
     * 获取
     *
     * @param key 钥匙
     * @return {@link T}
     */
    protected static <T> T get(String key) {
        return StringUtils.cast(getLocalMap().getOrDefault(key, StringUtils.EMPTY));
    }

    /**
     * 获取
     *
     * @param key          钥匙
     * @param defaultValue 默认值
     * @return {@link T}
     */
    protected static <T> T get(String key, Object defaultValue) {
        Map<String, Object> map = getLocalMap();
        return StringUtils.cast(map.getOrDefault(key, defaultValue));
    }

}
