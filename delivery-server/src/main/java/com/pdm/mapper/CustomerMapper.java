package com.pdm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pdm.entity.Customer;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CustomerMapper extends BaseMapper<Customer> {
}