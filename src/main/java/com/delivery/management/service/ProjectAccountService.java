package com.delivery.management.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.delivery.management.entity.ProjectAccount;

import java.util.List;

public interface ProjectAccountService extends IService<ProjectAccount> {
    
    /**
     * 鏍规嵁椤圭洰ID鏌ヨ璐︽埛鍒楄〃
     */
    List<ProjectAccount> getByProjectId(Long projectId);
}
