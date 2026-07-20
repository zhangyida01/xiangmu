package com.delivery.management.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.delivery.management.entity.Supplier;
import com.delivery.management.mapper.SupplierMapper;
import com.delivery.management.service.SupplierService;
import org.springframework.stereotype.Service;

@Service
public class SupplierServiceImpl extends ServiceImpl<SupplierMapper, Supplier> implements SupplierService {
}
