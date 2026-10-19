package com.pdm.controller;

import com.pdm.common.ApiResult;
import com.pdm.common.Audit;
import com.pdm.common.PageResult;
import com.pdm.common.RequiresPerm;
import com.pdm.common.Role;
import com.pdm.entity.Customer;
import com.pdm.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping
    public ApiResult<PageResult<Customer>> page(@RequestParam(defaultValue = "1") long page,
                                                @RequestParam(defaultValue = "20") long size,
                                                @RequestParam(required = false) String keyword) {
        return ApiResult.ok(customerService.page(page, size, keyword));
    }

    @GetMapping("/all")
    public ApiResult<List<Customer>> all() {
        return ApiResult.ok(customerService.all());
    }

    @GetMapping("/{id}")
    public ApiResult<Customer> get(@PathVariable Long id) {
        return ApiResult.ok(customerService.get(id));
    }

    @PostMapping
    @RequiresPerm(roles = {Role.ADMIN, Role.USER})
    @Audit(module = "CUSTOMER", action = "CREATE", targetType = "customer")
    public ApiResult<Long> create(@Valid @RequestBody Customer c) {
        return ApiResult.ok(customerService.create(c));
    }

    @PutMapping("/{id}")
    @RequiresPerm(roles = {Role.ADMIN, Role.USER})
    @Audit(module = "CUSTOMER", action = "UPDATE", targetType = "customer")
    public ApiResult<Void> update(@PathVariable Long id, @Valid @RequestBody Customer c) {
        customerService.update(id, c);
        return ApiResult.ok();
    }

    @DeleteMapping("/{id}")
    @RequiresPerm(roles = {Role.ADMIN})
    public ApiResult<Void> delete(@PathVariable Long id) {
        customerService.delete(id);
        return ApiResult.ok();
    }
}