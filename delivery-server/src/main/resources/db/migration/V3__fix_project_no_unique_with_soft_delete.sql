-- V3：修复「删除项目后，同一个项目编号无法再次使用」
--
-- 现象：新建项目保存时报 Duplicate entry '3' for key 'biz_project.uk_project_no'。
-- 原因：项目是逻辑删除（deleted=1），行还在表里；而 uk_project_no 直接建在 project_no 上，
--       已删除的行同样参与唯一性判断，于是这个编号被永久占用。
--       应用层的查重（ProjectService#ensureNoUnique）走 MyBatis-Plus，会自动补 deleted=0 条件，
--       所以查重能通过、最后卡在数据库唯一索引上，报的还是「服务器内部错误」。
--
-- 处理：把唯一约束改挂到生成列 project_no_active 上——活跃行取 project_no，已删除行为 NULL。
--       InnoDB 的唯一索引允许多个 NULL，因此已删除的编号可以重新使用；
--       活跃行之间依然由数据库强制编号唯一，不依赖应用层查重。
--
-- 注意：V1/V2 已执行过，不能修改（Flyway 会校验 checksum）；结构变更一律新增 V{n} 脚本。

-- 1) 生成列：仅活跃行有值
ALTER TABLE `biz_project`
  ADD COLUMN `project_no_active` VARCHAR(50)
    GENERATED ALWAYS AS (IF(`deleted` = 0, `project_no`, NULL)) VIRTUAL
    COMMENT '仅活跃行取 project_no，已删除行为 NULL，供唯一索引使用';

-- 2) 去掉建在 project_no 上的全量唯一索引
ALTER TABLE `biz_project` DROP INDEX `uk_project_no`;

-- 3) 唯一约束改挂生成列（沿用原索引名，便于和文档对照）
ALTER TABLE `biz_project` ADD UNIQUE KEY `uk_project_no` (`project_no_active`);
