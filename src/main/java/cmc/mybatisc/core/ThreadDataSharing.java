package cmc.mybatisc.core;

/**
 * 线程数据共享
 *
 * @author 程梦城
 * @version 1.0.0
 * &#064;date  2023/12/05
 */
public class ThreadDataSharing implements Runnable{
    private final Runnable runnable;
//    private final Authentication authentication;

    public static ThreadDataSharing getInstance(Runnable runnable) {
        return new ThreadDataSharing(runnable);
    }
    private ThreadDataSharing(Runnable runnable) {
        this.runnable = runnable;
        // 共享用户数据
//        this.authentication = SecurityUtils.getAuthentication();
    }

    @Override
    public void run() {

    }
}
