# 项目账户模块部署指南

## 部署步骤

### 1. 执行数据库迁移

连接到MySQL数据库 (172.168.2.40)，执行以下SQL：

```sql
-- 修改account_type为字符串类型（重要！）
ALTER TABLE project_account MODIFY COLUMN `account_type` VARCHAR(20) NOT NULL DEFAULT 'OTHER';

-- 如果login_url字段不存在，执行下面这条（否则跳过）
ALTER TABLE project_account CHANGE COLUMN `access_url` `login_url` VARCHAR(500) DEFAULT NULL;

-- 如果creator_id字段不存在，执行下面这条（否则跳过）  
ALTER TABLE project_account ADD COLUMN `creator_id` BIGINT(20) DEFAULT NULL AFTER `remark`;
```

### 2. 上传文件到服务器

将以下3个Java文件复制到服务器：

- ProjectAccount.java → /root/xiangmu/java/src/main/java/com/delivery/management/entity/
- ProjectAccountController.java → /root/xiangmu/java/src/main/java/com/delivery/management/controller/
- SupplierContactController.java → /root/xiangmu/java/src/main/java/com/delivery/management/controller/

### 3. 重新编译部署

```bash
cd /root/xiangmu/java
mvn clean package -DskipTests
./deploy.sh
```

### 4. 验证

访问 http://120.27.247.61，测试：
- 项目账户列表加载
- 添加账户（选择账户类型）
- 点击供应商名称查看联系人

## 修复内容

1. ✅ 账户类型从数字改为字符串枚举（DATABASE, SYSTEM, APPLICATION, CLOUD, OTHER）
2. ✅ 添加供应商联系人API接口
3. ✅ 支持记录创建人信息
4. ✅ 统一字段命名（login_url）