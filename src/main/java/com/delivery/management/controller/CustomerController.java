package com.delivery.management.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delivery.management.common.Result;
import com.delivery.management.entity.Customer;
import com.delivery.management.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/customer")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @GetMapping("/list")
    public Result<Page<Customer>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        
        Page<Customer> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Customer> wrapper = new LambdaQueryWrapper<>();
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(Customer::getCompanyName, keyword)
                    .or().like(Customer::getCustomerCode, keyword)
                    .or().like(Customer::getContactPerson, keyword)
                    .or().like(Customer::getContactPhone, keyword));
        }
        
        if (status != null) {
            wrapper.eq(Customer::getStatus, status);
        }
        
        wrapper.orderByDesc(Customer::getCreateTime);
        Page<Customer> result = customerService.page(pageParam, wrapper);
        
        return Result.success(result);
    }

    @GetMapping("/detail/{id}")
    public Result<Customer> detail(@PathVariable Long id) {
        Customer customer = customerService.getById(id);
        if (customer == null) {
            return Result.fail("Customer not found");
        }
        return Result.success(customer);
    }

    @PostMapping("/add")
    public Result<String> add(@RequestBody Customer customer) {
        boolean success = customerService.save(customer);
        return success ? Result.success("Add success") : Result.fail("Add failed");
    }

    @PutMapping("/update")
    public Result<String> update(@RequestBody Customer customer) {
        if (customer.getId() == null) {
            return Result.fail("Customer ID cannot be null");
        }
        boolean success = customerService.updateById(customer);
        return success ? Result.success("Update success") : Result.fail("Update failed");
    }

    @DeleteMapping("/delete/{id}")
    public Result<String> delete(@PathVariable Long id) {
        boolean success = customerService.removeById(id);
        return success ? Result.success("Delete success") : Result.fail("Delete failed");
    }
    
    @GetMapping("/all")
    public Result<java.util.List<Customer>> all() {
        LambdaQueryWrapper<Customer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Customer::getStatus, 1);
        wrapper.orderByDesc(Customer::getCreateTime);
        return Result.success(customerService.list(wrapper));
    }
}
