package com.delivery.management.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delivery.management.common.Result;
import com.delivery.management.entity.SupplierContact;
import com.delivery.management.service.SupplierContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/supplier-contact")
public class SupplierContactController {

    @Autowired
    private SupplierContactService supplierContactService;

    @GetMapping("/list")
    public Result<Page<SupplierContact>> list(
            @RequestParam(required = false) Long supplierId,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<SupplierContact> page = new Page<>(current, size);
        QueryWrapper<SupplierContact> wrapper = new QueryWrapper<>();
        if (supplierId != null) {
            wrapper.eq("supplier_id", supplierId);
        }
        wrapper.orderByDesc("create_time");
        Page<SupplierContact> result = supplierContactService.page(page, wrapper);
        return Result.success(result);
    }

    @GetMapping("/supplier/{supplierId}")
    public Result<List<SupplierContact>> listBySupplierId(@PathVariable Long supplierId) {
        QueryWrapper<SupplierContact> wrapper = new QueryWrapper<>();
        wrapper.eq("supplier_id", supplierId);
        wrapper.orderByDesc("create_time");
        List<SupplierContact> contacts = supplierContactService.list(wrapper);
        return Result.success(contacts);
    }

    @GetMapping("/{id}")
    public Result<SupplierContact> detail(@PathVariable Long id) {
        SupplierContact contact = supplierContactService.getById(id);
        if (contact == null) {
            return Result.fail("联系人不存在");
        }
        return Result.success(contact);
    }

    @PostMapping
    public Result<String> add(@RequestBody SupplierContact contact) {
        boolean success = supplierContactService.save(contact);
        return success ? Result.success("添加成功") : Result.fail("添加失败");
    }

    @PutMapping
    public Result<String> update(@RequestBody SupplierContact contact) {
        if (contact.getId() == null) {
            return Result.fail("联系人ID不能为空");
        }
        boolean success = supplierContactService.updateById(contact);
        return success ? Result.success("修改成功") : Result.fail("修改失败");
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        boolean success = supplierContactService.removeById(id);
        return success ? Result.success("删除成功") : Result.fail("删除失败");
    }
}