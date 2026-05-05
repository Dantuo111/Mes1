package com.app.controller;

import com.app.common.Constants;
import com.app.common.Result;
import com.app.entity.WorkOrder;
import com.app.service.WorkOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/workOrder")
public class WorkOrderController {

    @Autowired
    private WorkOrderService workOrderService;

    /**
     * 工单列表（分页，支持状态筛选）
     * 管理员查看全部工单，普通员工仅查看自己负责的工单
     */
    @GetMapping("/list")
    public Result list(@RequestParam(required = false) Integer status,
                       @RequestParam(defaultValue = "1") Integer pageNum,
                       @RequestParam(defaultValue = "10") Integer pageSize,
                       HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Integer role = (Integer) request.getAttribute("role");

        Long assigneeId = null;
        if (role == null || role == Constants.ROLE_EMPLOYEE) {
            // 普通员工只能查看自己负责的工单
            assigneeId = userId;
        }
        // 管理员 assigneeId 为 null，查全部

        return workOrderService.getWorkOrderList(status, assigneeId, pageNum, pageSize);
    }

    /**
     * 工单详情
     */
    @GetMapping("/detail/{id}")
    public Result detail(@PathVariable Long id) {
        return workOrderService.getWorkOrderDetail(id);
    }

    /**
     * 新增/编辑工单
     */
    @PostMapping("/save")
    public Result save(@RequestBody WorkOrder workOrder) {
        return workOrderService.saveWorkOrder(workOrder);
    }

    /**
     * 删除工单
     */
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Long id) {
        return workOrderService.deleteWorkOrder(id);
    }

    /**
     * 派发工单（待派发 → 待接收）
     */
    @PostMapping("/dispatch/{id}")
    public Result dispatch(@PathVariable Long id) {
        return workOrderService.dispatch(id);
    }

    /**
     * 接收工单（待接收 → 待开工）
     */
    @PostMapping("/receive/{id}")
    public Result receive(@PathVariable Long id) {
        return workOrderService.receive(id);
    }

    /**
     * 开始执行工单（待开工 → 执行中）
     */
    @PostMapping("/start/{id}")
    public Result start(@PathVariable Long id) {
        return workOrderService.start(id);
    }

    /**
     * 完工上报（执行中 → 已完工），接收实际完成数量
     */
    @PostMapping("/complete/{id}")
    public Result complete(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        Integer actualQuantity = null;
        if (params != null && params.get("actualQuantity") != null) {
            actualQuantity = Integer.valueOf(params.get("actualQuantity").toString());
        }
        return workOrderService.complete(id, actualQuantity);
    }

    /**
     * 关闭工单（已完工 → 已关闭）
     */
    @PostMapping("/close/{id}")
    public Result close(@PathVariable Long id) {
        return workOrderService.close(id);
    }
}
