package com.delivery.management.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delivery.management.common.Result;
import com.delivery.management.entity.ServiceTicket;
import com.delivery.management.service.ServiceTicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

@RestController
@RequestMapping("/ticket")
public class ServiceTicketController {

    @Autowired
    private ServiceTicketService serviceTicketService;

    /**
     * 工单列表（分页）
     */
    @GetMapping("/list")
    public Result<IPage<ServiceTicket>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer priority) {
        
        Page<ServiceTicket> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<ServiceTicket> wrapper = new LambdaQueryWrapper<>();
        
        if (projectId != null) {
            wrapper.eq(ServiceTicket::getProjectId, projectId);
        }
        
        if (status != null) {
            wrapper.eq(ServiceTicket::getStatus, status);
        }
        
        if (priority != null) {
            wrapper.eq(ServiceTicket::getPriority, priority);
        }
        
        wrapper.orderByDesc(ServiceTicket::getCreateTime);
        
        IPage<ServiceTicket> result = serviceTicketService.page(pageParam, wrapper);
        return Result.success(result);
    }

    /**
     * 工单详情
     */
    @GetMapping("/detail/{id}")
    public Result<ServiceTicket> detail(@PathVariable Long id) {
        ServiceTicket ticket = serviceTicketService.getById(id);
        if (ticket == null) {
            return Result.fail("工单不存在");
        }
        return Result.success(ticket);
    }

    /**
     * 创建工单
     */
    @PostMapping("/add")
    public Result<String> add(@RequestBody ServiceTicket ticket) {
        // 自动生成工单编号：TK + yyyyMMddHHmmss + 4位随机数
        if (!StringUtils.hasText(ticket.getTicketCode())) {
            String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
            String random = String.format("%04d", new Random().nextInt(10000));
            ticket.setTicketCode("TK" + timestamp + random);
        }
        
        // 根据优先级自动计算SLA截止时间
        if (ticket.getSlaDeadline() == null && ticket.getPriority() != null) {
            long hours = 24; // 默认24小时
            if (ticket.getPriority() == 1) {
                hours = 4; // 高优先级4小时
            } else if (ticket.getPriority() == 2) {
                hours = 8; // 中优先级8小时
            }
            ticket.setSlaDeadline(new Date(System.currentTimeMillis() + hours * 3600 * 1000));
        }
        
        boolean success = serviceTicketService.save(ticket);
        return success ? Result.success("创建成功") : Result.fail("创建失败");
    }

    /**
     * 更新工单
     */
    @PutMapping("/update")
    public Result<String> update(@RequestBody ServiceTicket ticket) {
        if (ticket.getId() == null) {
            return Result.fail("工单ID不能为空");
        }
        
        boolean success = serviceTicketService.updateById(ticket);
        return success ? Result.success("更新成功") : Result.fail("更新失败");
    }

    /**
     * 处理工单（标记为已解决）
     */
    @PutMapping("/resolve")
    public Result<String> resolve(@RequestBody ServiceTicket ticket) {
        if (ticket.getId() == null) {
            return Result.fail("工单ID不能为空");
        }
        
        ServiceTicket updateTicket = new ServiceTicket();
        updateTicket.setId(ticket.getId());
        updateTicket.setStatus(2); // 已解决
        updateTicket.setResolveTime(new Date());
        
        if (ticket.getSatisfaction() != null) {
            updateTicket.setSatisfaction(ticket.getSatisfaction());
        }
        
        boolean success = serviceTicketService.updateById(updateTicket);
        return success ? Result.success("工单已解决") : Result.fail("操作失败");
    }

    /**
     * 删除工单
     */
    @DeleteMapping("/delete/{id}")
    public Result<String> delete(@PathVariable Long id) {
        boolean success = serviceTicketService.removeById(id);
        return success ? Result.success("删除成功") : Result.fail("删除失败");
    }
}