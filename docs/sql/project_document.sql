CREATE TABLE `project_document` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `project_id` BIGINT(20) NOT NULL COMMENT '项目ID',
  `doc_type` VARCHAR(20) NOT NULL COMMENT '文档类型：FUNCTION_LIST-功能清单, ACCEPTANCE-验收材料, CONTRACT-合同文档, TECHNICAL-技术方案, OTHER-其他',
  `doc_name` VARCHAR(200) NOT NULL COMMENT '文档名称',
  `file_name` VARCHAR(200) NOT NULL COMMENT '原始文件名',
  `file_path` VARCHAR(500) NOT NULL COMMENT '文件存储路径',
  `file_size` BIGINT(20) NOT NULL COMMENT '文件大小(字节)',
  `file_ext` VARCHAR(20) DEFAULT NULL COMMENT '文件扩展名',
  `upload_by` BIGINT(20) NOT NULL COMMENT '上传人ID',
  `upload_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
  `remark` TEXT COMMENT '备注说明',
  `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_project_id` (`project_id`),
  KEY `idx_doc_type` (`doc_type`),
  KEY `idx_upload_time` (`upload_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目文档表';