-- ========================================
-- 浜や粯椤圭洰绠＄悊绯荤粺鏁版嵁搴撳垵濮嬪寲鑴氭湰
-- ========================================

-- 鍒涘缓鏁版嵁搴?CREATE DATABASE IF NOT EXISTS delivery_management DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE delivery_management;

-- ========================================
-- 1. 缁勭粐涓庣敤鎴风鐞?-- ========================================

-- 鐢ㄦ埛琛?DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '鐢ㄦ埛ID',
  username VARCHAR(50) NOT NULL COMMENT '鐢ㄦ埛鍚?,
  password VARCHAR(100) NOT NULL COMMENT '瀵嗙爜',
  real_name VARCHAR(50) COMMENT '鐪熷疄濮撳悕',
  email VARCHAR(100) COMMENT '閭',
  phone VARCHAR(20) COMMENT '鎵嬫満鍙?,
  dept_id BIGINT COMMENT '閮ㄩ棬ID',
  status TINYINT DEFAULT 1 COMMENT '鐘舵€侊細0-绂佺敤 1-鍚敤',
  avatar VARCHAR(255) COMMENT '澶村儚',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  create_by BIGINT COMMENT '鍒涘缓浜?,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
  update_by BIGINT COMMENT '鏇存柊浜?,
  deleted TINYINT DEFAULT 0 COMMENT '鍒犻櫎鏍囪瘑锛?-鏈垹闄?1-宸插垹闄?,
  PRIMARY KEY (id),
  UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='鐢ㄦ埛琛?;

-- 閮ㄩ棬琛?DROP TABLE IF EXISTS sys_department;
CREATE TABLE sys_department (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '閮ㄩ棬ID',
  dept_name VARCHAR(50) NOT NULL COMMENT '閮ㄩ棬鍚嶇О',
  parent_id BIGINT DEFAULT 0 COMMENT '鐖堕儴闂↖D',
  dept_code VARCHAR(50) COMMENT '閮ㄩ棬缂栫爜',
  sort INT DEFAULT 0 COMMENT '鎺掑簭',
  leader VARCHAR(50) COMMENT '璐熻矗浜?,
  phone VARCHAR(20) COMMENT '鑱旂郴鐢佃瘽',
  status TINYINT DEFAULT 1 COMMENT '鐘舵€侊細0-绂佺敤 1-鍚敤',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  create_by BIGINT,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_by BIGINT,
  deleted TINYINT DEFAULT 0,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='閮ㄩ棬琛?;

-- ========================================
-- 2. 瑙掕壊涓庢潈闄愮鐞?-- ========================================

-- 瑙掕壊琛?DROP TABLE IF EXISTS sys_role;
CREATE TABLE sys_role (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '瑙掕壊ID',
  role_code VARCHAR(50) NOT NULL COMMENT '瑙掕壊缂栫爜',
  role_name VARCHAR(50) NOT NULL COMMENT '瑙掕壊鍚嶇О',
  description VARCHAR(200) COMMENT '鎻忚堪',
  status TINYINT DEFAULT 1 COMMENT '鐘舵€侊細0-绂佺敤 1-鍚敤',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  create_by BIGINT,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_by BIGINT,
  deleted TINYINT DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='瑙掕壊琛?;

-- 鐢ㄦ埛瑙掕壊鍏宠仈琛?DROP TABLE IF EXISTS sys_user_role;
CREATE TABLE sys_user_role (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL COMMENT '鐢ㄦ埛ID',
  role_id BIGINT NOT NULL COMMENT '瑙掕壊ID',
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_role (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='鐢ㄦ埛瑙掕壊鍏宠仈琛?;

-- ========================================
-- 4. 浜や粯椤圭洰鍙拌处涓庣珛椤?-- ========================================

-- 瀹㈡埛琛?DROP TABLE IF EXISTS customer;
CREATE TABLE customer (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '瀹㈡埛ID',
  customer_name VARCHAR(100) NOT NULL COMMENT '瀹㈡埛鍚嶇О',
  customer_code VARCHAR(50) COMMENT '瀹㈡埛缂栫爜',
  contact_person VARCHAR(50) COMMENT '鑱旂郴浜?,
  contact_phone VARCHAR(20) COMMENT '鑱旂郴鐢佃瘽',
  contact_email VARCHAR(100) COMMENT '鑱旂郴閭',
  address VARCHAR(255) COMMENT '鍦板潃',
  status TINYINT DEFAULT 1 COMMENT '鐘舵€侊細0-鍋滅敤 1-鍚敤',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  create_by BIGINT,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_by BIGINT,
  deleted TINYINT DEFAULT 0,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='瀹㈡埛琛?;

-- 椤圭洰琛?DROP TABLE IF EXISTS delivery_project;
CREATE TABLE delivery_project (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '椤圭洰ID',
  project_code VARCHAR(50) NOT NULL COMMENT '椤圭洰缂栧彿',
  project_name VARCHAR(100) NOT NULL COMMENT '椤圭洰鍚嶇О',
  customer_id BIGINT NOT NULL COMMENT '瀹㈡埛ID',
  contract_no VARCHAR(50) COMMENT '鍚堝悓缂栧彿',
  contract_amount DECIMAL(15,2) COMMENT '鍚堝悓閲戦',
  start_date DATE COMMENT '寮€濮嬫棩鏈?,
  end_date DATE COMMENT '缁撴潫鏃ユ湡',
  project_manager_id BIGINT COMMENT '椤圭洰缁忕悊ID',
  status TINYINT DEFAULT 0 COMMENT '鐘舵€侊細0-绔嬮」 1-杩涜涓?2-宸查獙鏀?3-宸插叧闂?,
  description TEXT COMMENT '椤圭洰鎻忚堪',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  create_by BIGINT,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_by BIGINT,
  deleted TINYINT DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_project_code (project_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='浜や粯椤圭洰琛?;

-- ========================================
-- 5. 浜や粯鐗╀笌鏂囨。绠＄悊
-- ========================================

-- 鏂囨。琛?DROP TABLE IF EXISTS project_document;
CREATE TABLE project_document (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '鏂囨。ID',
  project_id BIGINT NOT NULL COMMENT '椤圭洰ID',
  doc_name VARCHAR(200) NOT NULL COMMENT '鏂囨。鍚嶇О',
  doc_type VARCHAR(50) COMMENT '鏂囨。绫诲瀷',
  file_path VARCHAR(500) COMMENT '鏂囦欢璺緞',
  file_size BIGINT COMMENT '鏂囦欢澶у皬锛堝瓧鑺傦級',
  version VARCHAR(20) DEFAULT '1.0' COMMENT '鐗堟湰鍙?,
  status TINYINT DEFAULT 1 COMMENT '鐘舵€侊細0-鑽夌 1-姝ｅ紡',
  description TEXT COMMENT '鎻忚堪',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  create_by BIGINT,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_by BIGINT,
  deleted TINYINT DEFAULT 0,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='椤圭洰鏂囨。琛?;

-- ========================================
-- 6. 浜や粯璁″垝涓庤寖鍥寸鐞?-- ========================================

-- 閲岀▼纰戣〃
DROP TABLE IF EXISTS project_milestone;
CREATE TABLE project_milestone (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '閲岀▼纰慖D',
  project_id BIGINT NOT NULL COMMENT '椤圭洰ID',
  milestone_name VARCHAR(100) NOT NULL COMMENT '閲岀▼纰戝悕绉?,
  plan_date DATE COMMENT '璁″垝鏃ユ湡',
  actual_date DATE COMMENT '瀹為檯鏃ユ湡',
  status TINYINT DEFAULT 0 COMMENT '鐘舵€侊細0-鏈紑濮?1-杩涜涓?2-宸插畬鎴?,
  description TEXT COMMENT '鎻忚堪',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  create_by BIGINT,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_by BIGINT,
  deleted TINYINT DEFAULT 0,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='椤圭洰閲岀▼纰戣〃';

-- 浠诲姟琛紙WBS锛?DROP TABLE IF EXISTS project_task;
CREATE TABLE project_task (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '浠诲姟ID',
  project_id BIGINT NOT NULL COMMENT '椤圭洰ID',
  parent_id BIGINT DEFAULT 0 COMMENT '鐖朵换鍔D',
  task_name VARCHAR(200) NOT NULL COMMENT '浠诲姟鍚嶇О',
  assignee_id BIGINT COMMENT '璐熻矗浜篒D',
  start_date DATE COMMENT '寮€濮嬫棩鏈?,
  end_date DATE COMMENT '缁撴潫鏃ユ湡',
  progress INT DEFAULT 0 COMMENT '杩涘害锛?-100锛?,
  status TINYINT DEFAULT 0 COMMENT '鐘舵€侊細0-鏈紑濮?1-杩涜涓?2-宸插畬鎴?,
  priority TINYINT DEFAULT 2 COMMENT '浼樺厛绾э細1-楂?2-涓?3-浣?,
  description TEXT COMMENT '鎻忚堪',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  create_by BIGINT,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_by BIGINT,
  deleted TINYINT DEFAULT 0,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='椤圭洰浠诲姟琛?;

-- ========================================
-- 7. 闂涓庡彉鏇寸鐞?-- ========================================

-- 闂琛?DROP TABLE IF EXISTS project_issue;
CREATE TABLE project_issue (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '闂ID',
  project_id BIGINT NOT NULL COMMENT '椤圭洰ID',
  issue_code VARCHAR(50) COMMENT '闂缂栧彿',
  title VARCHAR(200) NOT NULL COMMENT '闂鏍囬',
  description TEXT COMMENT '闂鎻忚堪',
  issue_type TINYINT DEFAULT 1 COMMENT '绫诲瀷锛?-闂 2-椋庨櫓',
  priority TINYINT DEFAULT 2 COMMENT '浼樺厛绾э細1-楂?2-涓?3-浣?,
  status TINYINT DEFAULT 0 COMMENT '鐘舵€侊細0-寰呭鐞?1-澶勭悊涓?2-宸茶В鍐?3-宸插叧闂?,
  reporter_id BIGINT COMMENT '鎻愭姤浜篒D',
  assignee_id BIGINT COMMENT '澶勭悊浜篒D',
  resolve_time DATETIME COMMENT '瑙ｅ喅鏃堕棿',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  create_by BIGINT,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_by BIGINT,
  deleted TINYINT DEFAULT 0,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='椤圭洰闂琛?;

-- 鍙樻洿鐢宠琛?DROP TABLE IF EXISTS project_change;
CREATE TABLE project_change (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '鍙樻洿ID',
  project_id BIGINT NOT NULL COMMENT '椤圭洰ID',
  change_code VARCHAR(50) COMMENT '鍙樻洿缂栧彿',
  title VARCHAR(200) NOT NULL COMMENT '鍙樻洿鏍囬',
  description TEXT COMMENT '鍙樻洿鎻忚堪',
  change_type TINYINT DEFAULT 1 COMMENT '绫诲瀷锛?-鑼冨洿 2-鏃堕棿 3-鎴愭湰',
  status TINYINT DEFAULT 0 COMMENT '鐘舵€侊細0-寰呭鎵?1-宸叉壒鍑?2-宸叉嫆缁?,
  applicant_id BIGINT COMMENT '鐢宠浜篒D',
  approver_id BIGINT COMMENT '瀹℃壒浜篒D',
  approve_time DATETIME COMMENT '瀹℃壒鏃堕棿',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  create_by BIGINT,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_by BIGINT,
  deleted TINYINT DEFAULT 0,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='椤圭洰鍙樻洿琛?;

-- ========================================
-- 8. 瀹㈡埛鍗忓悓涓庨獙鏀剁‘璁?-- ========================================

-- 楠屾敹鍗曡〃
DROP TABLE IF EXISTS project_acceptance;
CREATE TABLE project_acceptance (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '楠屾敹ID',
  project_id BIGINT NOT NULL COMMENT '椤圭洰ID',
  acceptance_code VARCHAR(50) COMMENT '楠屾敹鍗曠紪鍙?,
  title VARCHAR(200) NOT NULL COMMENT '楠屾敹鏍囬',
  acceptance_date DATE COMMENT '楠屾敹鏃ユ湡',
  customer_sign VARCHAR(50) COMMENT '瀹㈡埛绛惧瓧浜?,
  status TINYINT DEFAULT 0 COMMENT '鐘舵€侊細0-寰呴獙鏀?1-宸查€氳繃 2-鏈€氳繃',
  result TEXT COMMENT '楠屾敹缁撴灉',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  create_by BIGINT,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_by BIGINT,
  deleted TINYINT DEFAULT 0,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='椤圭洰楠屾敹琛?;

-- ========================================
-- 9. 璧勬簮涓庡伐鏃剁鐞?-- ========================================

-- 宸ユ椂璁板綍琛?DROP TABLE IF EXISTS project_worklog;
CREATE TABLE project_worklog (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '宸ユ椂ID',
  project_id BIGINT NOT NULL COMMENT '椤圭洰ID',
  user_id BIGINT NOT NULL COMMENT '鐢ㄦ埛ID',
  work_date DATE NOT NULL COMMENT '宸ヤ綔鏃ユ湡',
  work_hours DECIMAL(5,2) NOT NULL COMMENT '宸ユ椂锛堝皬鏃讹級',
  task_id BIGINT COMMENT '浠诲姟ID',
  description TEXT COMMENT '宸ヤ綔鍐呭',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  create_by BIGINT,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_by BIGINT,
  deleted TINYINT DEFAULT 0,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='宸ユ椂璁板綍琛?;

-- ========================================
-- 11. 椤圭洰鍞悗绠＄悊
-- ========================================

-- 宸ュ崟琛?DROP TABLE IF EXISTS service_ticket;
CREATE TABLE service_ticket (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '宸ュ崟ID',
  ticket_code VARCHAR(50) NOT NULL COMMENT '宸ュ崟缂栧彿',
  project_id BIGINT NOT NULL COMMENT '椤圭洰ID',
  title VARCHAR(200) NOT NULL COMMENT '宸ュ崟鏍囬',
  description TEXT COMMENT '闂鎻忚堪',
  priority TINYINT DEFAULT 2 COMMENT '浼樺厛绾э細1-楂?2-涓?3-浣?,
  status TINYINT DEFAULT 0 COMMENT '鐘舵€侊細0-寰呭鐞?1-澶勭悊涓?2-宸茶В鍐?3-宸插叧闂?,
  reporter_id BIGINT COMMENT '鎻愪氦浜篒D',
  assignee_id BIGINT COMMENT '澶勭悊浜篒D',
  sla_deadline DATETIME COMMENT 'SLA鎴鏃堕棿',
  resolve_time DATETIME COMMENT '瑙ｅ喅鏃堕棿',
  satisfaction TINYINT COMMENT '婊℃剰搴︼細1-5鍒?,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  create_by BIGINT,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_by BIGINT,
  deleted TINYINT DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_ticket_code (ticket_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='宸ュ崟琛?;

-- ========================================
-- 鍒濆鍖栨暟鎹?-- ========================================

-- 鍒濆鍖栫鐞嗗憳鐢ㄦ埛锛堝瘑鐮侊細admin123锛屼娇鐢˙Crypt鍔犲瘑锛?INSERT INTO sys_user (username, password, real_name, status) 
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系统管理员', 1);

-- 鍒濆鍖栬鑹?INSERT INTO sys_role (role_code, role_name, description) VALUES 
('ADMIN', '绠＄悊鍛?, '绯荤粺绠＄悊鍛?),
('PM', '椤圭洰缁忕悊', '椤圭洰缁忕悊'),
('MEMBER', '椤圭洰鎴愬憳', '鏅€氶」鐩垚鍛?);

-- 鍒濆鍖栫敤鎴疯鑹插叧鑱?INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1);

-- 瀹屾垚
SELECT '鏁版嵁搴撳垵濮嬪寲瀹屾垚锛? AS message;
