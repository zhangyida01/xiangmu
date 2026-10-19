package com.pdm.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pdm.common.BizException;
import com.pdm.common.PageResult;
import com.pdm.entity.Customer;
import com.pdm.entity.Project;
import com.pdm.mapper.CustomerMapper;
import com.pdm.mapper.ProjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerMapper customerMapper;
    private final ProjectMapper projectMapper;

    public PageResult<Customer> page(long page, long size, String keyword) {
        LambdaQueryWrapper<Customer> w = new LambdaQueryWrapper<>();
        w.and(keyword != null && !keyword.isBlank(), q -> q
                        .like(Customer::getName, keyword).or()
                        .like(Customer::getShortName, keyword).or()
                        .like(Customer::getContactName, keyword))
                .orderByDesc(Customer::getId);
        return PageResult.of(customerMapper.selectPage(new Page<>(page, size), w));
    }

    public List<Customer> all() {
        return customerMapper.selectList(new LambdaQueryWrapper<Customer>().orderByAsc(Customer::getName));
    }

    public Customer get(Long id) {
        Customer c = customerMapper.selectById(id);
        if (c == null) {
            throw BizException.notFound("客户");
        }
        return c;
    }

    public Long create(Customer c) {
        c.setId(null);
        customerMapper.insert(c);
        return c.getId();
    }

    public void update(Long id, Customer c) {
        get(id);
        c.setId(id);
        customerMapper.updateById(c);
    }

    public void delete(Long id) {
        get(id);
        Long used = projectMapper.selectCount(new LambdaQueryWrapper<Project>().eq(Project::getCustomerId, id));
        if (used != null && used > 0) {
            throw BizException.conflict("该客户下还有 " + used + " 个项目，不能删除");
        }
        customerMapper.deleteById(id);
    }
}