package cmc.mybatisc.utils;

import cmc.mybatisc.utils.page.PageDomain;
import cmc.mybatisc.utils.page.TableSupport;
import cn.hutool.core.bean.BeanUtil;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

import java.util.List;

/**
 * 分页工具类
 *
 * @author 程梦城
 * @version 1.0.0
 * &#064;date  2023/12/11
 */
public class PageUtils extends PageHelper
{
    /**
     * 设置请求分页数据
     */
    public static void startPage()
    {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        Integer pageNum = pageDomain.getPageNum();
        Integer pageSize = pageDomain.getPageSize();
        Boolean reasonable = pageDomain.getReasonable();
        PageHelper.startPage(pageNum, pageSize, null).setReasonable(reasonable);
    }

    /**
     * 清理分页的线程变量
     */
    public static void clearPage()
    {
        PageHelper.clearPage();
    }

    /**
     * 转成分页
     *
     * @param source 来源
     * @param target 目标
     * @return {@link List}<{@link T}>
     */
    public static <T> List<T> toPage(List<?> source, List<T> target) {
        if (source instanceof Page) {
            Page<?> page = (Page<?>) source;
            Page<T> ts = new Page<>(page.getPageNum(), page.getPageSize());
            ts.setTotal(page.getTotal());
            ts.addAll(target);
            return ts;
        } else {
            return target;
        }
    }

    /**
     * 到页面
     *
     * @param list   列表
     * @param tClass t类
     * @return {@link List}<{@link T}>
     */
    public static <T> List<T> toPage(List<?> list, Class<T> tClass) {
        return toPage(list, BeanUtil.copyToList(list, tClass));
    }
}
