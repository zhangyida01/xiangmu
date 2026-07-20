package com.delivery.management.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.delivery.management.entity.SupplierContact;

import java.util.List;

public interface SupplierContactService extends IService<SupplierContact> {
    List<SupplierContact> getBySupplierId(Long supplierId);
}
