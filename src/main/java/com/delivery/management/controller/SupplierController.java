package com.delivery.management.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delivery.management.common.Result;
import com.delivery.management.entity.Supplier;
import com.delivery.management.entity.SupplierContact;
import com.delivery.management.service.SupplierContactService;
import com.delivery.management.service.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/supplier")
public class SupplierController {

    @Autowired
    private SupplierService supplierService;
    
    @Autowired
    private SupplierContactService supplierContactService;

    @GetMapping("/list")
    public Result<Page<Supplier>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer supplierType) {
        
        Page<Supplier> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Supplier> wrapper = new LambdaQueryWrapper<>();
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(Supplier::getSupplierName, keyword)
                    .or().like(Supplier::getSupplierCode, keyword));
        }
        
        if (supplierType != null) {
            wrapper.eq(Supplier::getSupplierType, supplierType);
        }
        
        wrapper.orderByDesc(Supplier::getCreateTime);
        Page<Supplier> result = supplierService.page(pageParam, wrapper);
        
        return Result.success(result);
    }

    @GetMapping("/all")
    public Result<List<Supplier>> all() {
        LambdaQueryWrapper<Supplier> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Supplier::getStatus, 1);
        wrapper.orderByDesc(Supplier::getCreateTime);
        return Result.success(supplierService.list(wrapper));
    }

    @PostMapping("/add")
    public Result<String> add(@RequestBody Supplier supplier) {
        boolean success = supplierService.save(supplier);
        return success ? Result.success("Add success") : Result.fail("Add failed");
    }

    @PutMapping("/update")
    public Result<String> update(@RequestBody Supplier supplier) {
        if (supplier.getId() == null) {
            return Result.fail("Supplier ID cannot be null");
        }
        boolean success = supplierService.updateById(supplier);
        return success ? Result.success("Update success") : Result.fail("Update failed");
    }

    @DeleteMapping("/delete/{id}")
    public Result<String> delete(@PathVariable Long id) {
        boolean success = supplierService.removeById(id);
        return success ? Result.success("Delete success") : Result.fail("Delete failed");
    }
    
    @GetMapping("/contact/list/{supplierId}")
    public Result<List<SupplierContact>> listContacts(@PathVariable Long supplierId) {
        List<SupplierContact> contacts = supplierContactService.getBySupplierId(supplierId);
        return Result.success(contacts);
    }
    
    @PostMapping("/contact/add")
    public Result<String> addContact(@RequestBody SupplierContact contact) {
        boolean success = supplierContactService.save(contact);
        return success ? Result.success("Add success") : Result.fail("Add failed");
    }
    
    @PutMapping("/contact/update")
    public Result<String> updateContact(@RequestBody SupplierContact contact) {
        if (contact.getId() == null) {
            return Result.fail("Contact ID cannot be null");
        }
        boolean success = supplierContactService.updateById(contact);
        return success ? Result.success("Update success") : Result.fail("Update failed");
    }
    
    @DeleteMapping("/contact/delete/{id}")
    public Result<String> deleteContact(@PathVariable Long id) {
        boolean success = supplierContactService.removeById(id);
        return success ? Result.success("Delete success") : Result.fail("Delete failed");
    }
}
