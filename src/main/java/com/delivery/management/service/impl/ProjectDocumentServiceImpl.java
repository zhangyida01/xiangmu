package com.delivery.management.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.delivery.management.entity.ProjectDocument;
import com.delivery.management.mapper.ProjectDocumentMapper;
import com.delivery.management.service.ProjectDocumentService;
import org.springframework.stereotype.Service;

@Service
public class ProjectDocumentServiceImpl extends ServiceImpl<ProjectDocumentMapper, ProjectDocument> 
        implements ProjectDocumentService {
}