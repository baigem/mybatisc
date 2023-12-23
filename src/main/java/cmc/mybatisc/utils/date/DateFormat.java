package cmc.mybatisc.utils.date;

import lombok.Getter;

/**
 * 日期格式
 *
 * @author 程梦城
 * @version 1.0.0
 * &#064;date  2023/12/15
 */
@Getter
public enum DateFormat {
    YYYY("yyyy"),
    YYYYMM("yyyy-MM"),
    YYYYMMDD("yyyy-MM-dd"),
    YYYYMMDDHH("yyyy-MM-dd HH"),
    YYYYMMDDHHMM("yyyy-MM-dd HH:mm"),
    YYYYMMDDHHMMSS("yyyy-MM-dd HH:mm:ss"),
    ;

    private final String format;
    DateFormat(String format) {
        this.format = format;
    }
}
