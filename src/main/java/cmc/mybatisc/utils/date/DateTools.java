package cmc.mybatisc.utils.date;

import lombok.SneakyThrows;

import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * 日期时间工具类
 *
 * @author 程梦城
 * @version 1.0.0
 * &#064;date  2023/12/15
 */
public class DateTools {

    /**
     * 获取日分钟数
     *
     * @return int
     */
    public static int getDayMinutes(Date date){
        // 使用 Instant 和 ZoneId 创建 LocalTime
        LocalTime localTime = date.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();
        // 计算当前时间属于当天的第几分钟
        return localTime.getHour() * 60 + localTime.getMinute();
    }

    /**
     * 格式
     *
     * @param format 格式
     * @param date   日期
     * @return {@link String}
     */
    public static String format(DateFormat format,Date date){
        return new SimpleDateFormat(format.getFormat()).format(date);
    }

    /**
     * 解析
     *
     * @param format 格式
     * @param date   日期
     * @return {@link Date}
     */
    @SneakyThrows
    public static Date parse(DateFormat format, String date){
        return new SimpleDateFormat(format.getFormat()).parse(date);
    }

}
