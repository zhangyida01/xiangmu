package com.delivery.management.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.delivery.management.entity.SupplierContact;
import com.delivery.management.mapper.SupplierContactMapper;
import com.delivery.management.service.SupplierContactService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SupplierContactServiceImpl extends ServiceImpl<SupplierContactMapper, SupplierContact> implements SupplierContactService {

    @Override
    public List<SupplierContact> getBySupplierId(Long supplierId) {
        LambdaQueryWrapper<SupplierContact> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SupplierContact::getSupplierId, supplierId);
        wrapper.orderByDesc(SupplierContact::getIsPrimary);
        wrapper.orderByDesc(SupplierContact::getCreateTime);
        return this.list(wrapper);
    }
}
