package com.pdm.mapper;

import com.pdm.dto.AlertSummary;
import com.pdm.dto.ExpireAlert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AlertMapper {

    @Select("SELECT * FROM v_expire_alert WHERE days_left <= #{days} ORDER BY days_left ASC LIMIT 500")
    List<ExpireAlert> listExpiring(@Param("days") int days);

    @Select("SELECT src_type AS srcType, COUNT(*) AS cnt, MIN(days_left) AS minDays "
            + "FROM v_expire_alert WHERE days_left <= #{days} GROUP BY src_type")
    List<AlertSummary> summary(@Param("days") int days);
}