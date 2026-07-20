package com.delivery.management.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.delivery.management.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("project_account")
public class ProjectAccount extends BaseEntity {
    
    /**
     * 椤圭洰ID锛堝閿叧鑱攑roject琛級
     */
    private Long projectId;
    
    /**
     * 璐︽埛鍚嶇О
     */
    private String accountName;
    
    /**
     * 璐︽埛绫诲瀷锛?-绯荤粺璐︽埛 2-鏁版嵁搴撹处鎴?3-浜戝钩鍙拌处鎴?4-FTP璐︽埛 5-閭璐︽埛 6-鍏朵粬
     */
    private Integer accountType;
    
    /**
     * 鐢ㄦ埛鍚?璐﹀彿
     */
    private String username;
    
    /**
     * 瀵嗙爜锛堝姞瀵嗗瓨鍌級
     */
    private String password;
    
    /**
     * 璁块棶鍦板潃/URL
     */
    private String accessUrl;
    
    /**
     * 绔彛
     */
    private Integer port;
    
    /**
     * 鐜绫诲瀷锛?-寮€鍙?2-娴嬭瘯 3-棰勫彂甯?4-鐢熶骇
     */
    private Integer environment;
    
    /**
     * 澶囨敞璇存槑
     */
    private String remark;
}
