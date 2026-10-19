package com.pdm.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface StatsMapper {

    @Select("SELECT COUNT(*) FROM biz_project WHERE deleted = 0")
    long projectCount();

    @Select("SELECT COUNT(*) FROM biz_project WHERE deleted = 0 AND archived = 0")
    long activeProjectCount();

    @Select("SELECT COUNT(*) FROM biz_customer WHERE deleted = 0")
    long customerCount();

    @Select("SELECT COUNT(*) FROM biz_document WHERE deleted = 0")
    long documentCount();

    @Select("SELECT COUNT(*) FROM biz_credential WHERE deleted = 0")
    long credentialCount();

    @Select("SELECT status, COUNT(*) AS cnt FROM biz_project WHERE deleted = 0 GROUP BY status")
    List<Map<String, Object>> projectStatusGroup();

    @Select("SELECT project_type AS projectType, COUNT(*) AS cnt FROM biz_project WHERE deleted = 0 GROUP BY project_type")
    List<Map<String, Object>> projectTypeGroup();

    @Select("SELECT COUNT(*) FROM biz_project WHERE deleted = 0 AND archived = 0 "
            + "AND plan_accept_date IS NOT NULL "
            + "AND plan_accept_date BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 30 DAY)")
    long acceptingIn30Days();

    @Select("SELECT COUNT(*) FROM v_expire_alert WHERE days_left <= #{days}")
    long alertCount(int days);

    @Select("SELECT ROUND(AVG(doc_rate), 0) FROM biz_project WHERE deleted = 0 AND archived = 0")
    Double avgDocRate();

    @Select("SELECT p.id, p.project_no AS projectNo, p.name, p.status, p.health, p.manager, "
            + "DATE_FORMAT(p.plan_accept_date, '%Y-%m-%d') AS planAcceptDate, p.doc_rate AS docRate, c.name AS customerName "
            + "FROM biz_project p LEFT JOIN biz_customer c ON c.id = p.customer_id "
            + "WHERE p.deleted = 0 ORDER BY p.update_time DESC LIMIT #{limit}")
    List<Map<String, Object>> recentProjects(int limit);
}