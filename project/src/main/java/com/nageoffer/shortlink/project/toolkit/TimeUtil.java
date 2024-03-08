package com.nageoffer.shortlink.project.toolkit;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Time 工具类
 */
public class TimeUtil {

    public static long getRemainSecondsOfDay(){
        // 获取当前日期时间
        LocalDateTime now = LocalDateTime.now();
        // 获取当天结束的时间（即明天的零点）
        LocalTime endOfDay = LocalTime.of(23, 59, 59);
        // 将当前日期和结束时间合并为 LocalDateTime 对象
        LocalDateTime endOfToday = now.toLocalDate().atTime(endOfDay);
        // 计算当前时间到当天结束的剩余时间
        Duration duration = Duration.between(now, endOfToday);
        // 返回剩余时间的秒数
        return duration.getSeconds();
    }
}



