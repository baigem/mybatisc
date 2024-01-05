package cmc.mybatisc.web;

import cmc.mybatisc.utils.PageUtils;
import cmc.mybatisc.utils.string.StringTools;
import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * web层通用数据处理
 *
 * @author 程梦城
 * @version 1.0.0
 * &#064;date  2023/12/11
 */
public class BaseController <C extends IService<?>>
{
    /**
     * 基础服务
     */
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired(required = false)
    protected C baseService;

    /**
     * 设置请求分页数据
     */
    protected void startPage()
    {
        PageUtils.startPage();
    }


    /**
     * 清理分页的线程变量
     */
    protected void clearPage()
    {
        PageUtils.clearPage();
    }

    /**
     * 响应请求分页数据
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected Result getDataTable(List<?> list)
    {
        Result rspData = new Result(HttpStatus.SUCCESS,"查询成功");
        rspData.put("rows",list);
        rspData.put("total",new PageInfo(list).getTotal());
        return rspData;
    }

    /**
     * 返回成功
     */
    public Result success()
    {
        return Result.success();
    }

    /**
     * 返回失败消息
     */
    public Result error()
    {
        return Result.error();
    }

    /**
     * 返回成功消息
     */
    public Result success(String message)
    {
        return Result.success(message);
    }
    
    /**
     * 返回成功消息
     */
    public Result success(Object data)
    {
        return Result.success(data);
    }

    /**
     * 返回失败消息
     */
    public Result error(String message)
    {
        return Result.error(message);
    }

    /**
     * 返回警告消息
     */
    public Result warn(String message)
    {
        return Result.warn(message);
    }

    /**
     * 响应返回结果
     * 
     * @param rows 影响行数
     * @return 操作结果
     */
    protected Result toAjax(int rows)
    {
        return rows > 0 ? Result.success() : Result.error();
    }

    /**
     * 响应返回结果
     * 
     * @param result 结果
     * @return 操作结果
     */
    protected Result toAjax(boolean result)
    {
        return result ? success() : error();
    }

    /**
     * 页面跳转
     */
    public String redirect(String url)
    {
        return StringTools.format("redirect:{}", url);
    }
}
