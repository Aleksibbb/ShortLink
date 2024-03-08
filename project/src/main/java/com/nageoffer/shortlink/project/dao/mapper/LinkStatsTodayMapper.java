package com.nageoffer.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nageoffer.shortlink.project.dao.entity.LinkStatsTodayDO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;

/**
 * 短链接今日统计持久层
 */
public interface LinkStatsTodayMapper extends BaseMapper<LinkStatsTodayDO> {

    /**
     * 记录今日访问监控数据
     */
    @Insert("INSERT INTO t_link_stats_today (full_short_url, gid, date, today_pv, today_uv, today_uip, create_time, update_time, del_flag) " +
            "VALUES( #{linkStatsToday.fullShortUrl}, #{linkStatsToday.gid}, #{linkStatsToday.date}, #{linkStatsToday.todayPv}, #{linkStatsToday.todayUv}, #{linkStatsToday.todayUip}, NOW(), NOW(), 0) " +
            "ON DUPLICATE KEY UPDATE today_pv = today_pv +  #{linkStatsToday.todayPv}, today_uv = today_uv +  #{linkStatsToday.todayUv}, today_uip = today_uip +  #{linkStatsToday.todayUip};")
    void shortLinkTodayStats(@Param("linkStatsToday") LinkStatsTodayDO linkStatsTodayDO);
}
