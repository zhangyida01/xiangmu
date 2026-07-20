package com.delivery.management.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.delivery.management.entity.ProjectAccount;
import com.delivery.management.mapper.ProjectAccountMapper;
import com.delivery.management.service.ProjectAccountService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectAccountServiceImpl extends ServiceImpl<ProjectAccountMapper, ProjectAccount> implements ProjectAccountService {

    @Override
    public List<ProjectAccount> getByProjectId(Long projectId) {
        LambdaQueryWrapper<ProjectAccount> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProjectAccount::getProjectId, projectId);
        wrapper.orderByDesc(ProjectAccount::getCreateTime);
        return this.list(wrapper);
    }
}
