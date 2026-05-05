package com.app.service;

import com.app.common.Constants;
import com.app.common.Result;
import com.app.entity.User;
import com.app.entity.WorkOrder;
import com.app.mapper.UserMapper;
import com.app.mapper.WorkOrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class WorkOrderService {

    @Autowired
    private WorkOrderMapper workOrderMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ProductionPlanService productionPlanService;

    /**
     * 获取工单列表（分页）
     * assigneeId 为 null 时查全部（管理员），否则仅查该员工负责的工单
     */
    public Result getWorkOrderList(Integer status, Long assigneeId, Integer pageNum, Integer pageSize) {
        List<WorkOrder> allOrders = workOrderMapper.selectList(status, assigneeId);
        int total = allOrders.size();

        // 手动分页
        int fromIndex = (pageNum - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);

        List<WorkOrder> pageList;
        if (fromIndex >= total) {
            pageList = new ArrayList<>();
        } else {
            pageList = allOrders.subList(fromIndex, toIndex);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("list", pageList);
        data.put("total", total);
        data.put("pageNum", pageNum);
        data.put("pageSize", pageSize);

        return Result.success(data);
    }

    /**
     * 获取工单详情
     */
    public Result getWorkOrderDetail(Long id) {
        WorkOrder workOrder = workOrderMapper.selectById(id);
        if (workOrder == null) {
            return Result.error("工单不存在");
        }
        return Result.success(workOrder);
    }

    /**
     * 新增或编辑工单
     */
    public Result saveWorkOrder(WorkOrder workOrder) {
        if (workOrder.getId() == null) {
            // 新增工单：校验负责人存在性
            if (workOrder.getAssigneeId() != null) {
                User assignee = userMapper.selectById(workOrder.getAssigneeId());
                if (assignee == null) {
                    return Result.error("负责人不存在");
                }
            }
            // 设置默认状态为"待派发"
            workOrder.setStatus(Constants.WO_STATUS_PENDING_DISPATCH);
            workOrderMapper.insert(workOrder);
        } else {
            // 编辑工单
            workOrderMapper.updateById(workOrder);
        }
        return Result.success();
    }

    /**
     * 删除工单
     */
    public Result deleteWorkOrder(Long id) {
        workOrderMapper.deleteById(id);
        return Result.success();
    }

    /**
     * 派发工单（0 待派发 → 1 待接收）
     */
    public Result dispatch(Long id) {
        return changeStatus(id, Constants.WO_STATUS_PENDING_DISPATCH, Constants.WO_STATUS_PENDING_RECEIVE);
    }

    /**
     * 接收工单（1 待接收 → 2 待开工）
     */
    public Result receive(Long id) {
        return changeStatus(id, Constants.WO_STATUS_PENDING_RECEIVE, Constants.WO_STATUS_PENDING_START);
    }

    /**
     * 开始执行工单（2 待开工 → 3 执行中）
     */
    public Result start(Long id) {
        return changeStatus(id, Constants.WO_STATUS_PENDING_START, Constants.WO_STATUS_IN_PROGRESS);
    }

    /**
     * 完工上报（3 执行中 → 4 已完工），记录实际数量和完工时间
     */
    public Result complete(Long id, Integer actualQuantity) {
        WorkOrder workOrder = workOrderMapper.selectById(id);
        if (workOrder == null) {
            return Result.error("工单不存在");
        }
        if (workOrder.getStatus() != Constants.WO_STATUS_IN_PROGRESS) {
            return Result.error("状态流转不合法");
        }

        // 更新状态、实际数量和完工时间
        WorkOrder update = new WorkOrder();
        update.setId(id);
        update.setStatus(Constants.WO_STATUS_COMPLETED);
        update.setActualQuantity(actualQuantity);
        update.setCompleteTime(new Date());
        workOrderMapper.updateById(update);

        // 触发生产计划状态自动更新
        productionPlanService.updatePlanStatusByWorkOrder(workOrder.getPlanId());

        return Result.success();
    }

    /**
     * 关闭工单（4 已完工 → 5 已关闭）
     */
    public Result close(Long id) {
        return changeStatus(id, Constants.WO_STATUS_COMPLETED, Constants.WO_STATUS_CLOSED);
    }

    /**
     * 通用状态流转方法
     * @param id 工单ID
     * @param expectedCurrentStatus 期望的当前状态
     * @param newStatus 目标状态
     */
    private Result changeStatus(Long id, int expectedCurrentStatus, int newStatus) {
        WorkOrder workOrder = workOrderMapper.selectById(id);
        if (workOrder == null) {
            return Result.error("工单不存在");
        }
        if (workOrder.getStatus() != expectedCurrentStatus) {
            return Result.error("状态流转不合法");
        }

        workOrderMapper.updateStatus(id, newStatus);

        // 每次状态变更后触发生产计划状态自动更新
        productionPlanService.updatePlanStatusByWorkOrder(workOrder.getPlanId());

        return Result.success();
    }
}
