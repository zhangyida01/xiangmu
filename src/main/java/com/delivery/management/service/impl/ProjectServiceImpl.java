package com.delivery.management.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.delivery.management.entity.Project;
import com.delivery.management.mapper.ProjectMapper;
import com.delivery.management.service.ProjectService;
import org.springframework.stereotype.Service;

@Service
public class ProjectServiceImpl extends ServiceImpl<ProjectMapper, Project> implements ProjectService {
}
