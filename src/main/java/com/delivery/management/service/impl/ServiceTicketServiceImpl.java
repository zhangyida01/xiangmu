package com.delivery.management.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.delivery.management.entity.ServiceTicket;
import com.delivery.management.mapper.ServiceTicketMapper;
import com.delivery.management.service.ServiceTicketService;
import org.springframework.stereotype.Service;

@Service
public class ServiceTicketServiceImpl extends ServiceImpl<ServiceTicketMapper, ServiceTicket> implements ServiceTicketService {
}
