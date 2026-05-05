package com.app.service;

import com.app.common.Constants;
import com.app.common.Result;
import com.app.entity.ProductionPlan;
import com.app.entity.WorkOrder;
import com.app.mapper.ProductionPlanMapper;
import com.app.mapper.WorkOrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProductionPlanService {

    @Autowired
    private ProductionPlanMapper productionPlanMapper;

    @Autowired
    private WorkOrderMapper workOrderMapper;

    /**
     * 获取生产计划列表（分页，支持筛选）
     */
    public Result getPlanList(String keyword, Integer status, Integer pageNum, Integer pageSize) {
        // 获取符合条件的全部计划列表
        List<ProductionPlan> allPlans = productionPlanMapper.selectList(keyword, status);
        int total = allPlans.size();

        // 手动分页
        int fromIndex = (pageNum - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);

        List<ProductionPlan> pageList;
        if (fromIndex >= total) {
            pageList = new ArrayList<>();
        } else {
            pageList = allPlans.subList(fromIndex, toIndex);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("list", pageList);
        data.put("total", total);
        data.put("pageNum", pageNum);
        data.put("pageSize", pageSize);

        return Result.success(data);
    }

    /**
     * 获取生产计划详情
     */
    public Result getPlanDetail(Long id) {
        ProductionPlan plan = productionPlanMapper.selectById(id);
        if (plan == null) {
            return Result.error("生产计划不存在");
        }
        return Result.success(plan);
    }

    /**
     * 新增或编辑生产计划
     */
    public Result savePlan(ProductionPlan plan) {
        // 时间范围校验：开始时间不能晚于结束时间
        if (plan.getStartTime() != null && plan.getEndTime() != null) {
            if (plan.getStartTime().after(plan.getEndTime())) {
                return Result.error("开始时间不能晚于结束时间");
            }
        }

        if (plan.getId() == null) {
            // 新增计划：设置默认状态为"待执行"
            plan.setStatus(Constants.PLAN_STATUS_PENDING);
            productionPlanMapper.insert(plan);
        } else {
            // 编辑计划
            productionPlanMapper.updateById(plan);
        }
        return Result.success();
    }

    /**
     * 删除生产计划
     */
    public Result deletePlan(Long id) {
        // 检查是否有关联工单
        List<WorkOrder> orders = workOrderMapper.selectByPlanId(id);
        if (orders != null && !orders.isEmpty()) {
            return Result.error("该计划下存在关联工单，无法删除");
        }
        productionPlanMapper.deleteById(id);
        return Result.success();
    }

    /**
     * 根据工单状态自动更新生产计划状态
     * 当工单状态变更时由 WorkOrderService 调用
     *
     * 逻辑：
     * - 若存在任一工单状态为"执行中"(3)，则计划状态更新为"执行中"(1)
     * - 若所有工单状态均为"已关闭"(5)，则计划状态更新为"已完成"(2)
     */
    public void updatePlanStatusByWorkOrder(Long planId) {
        if (planId == null) {
            return;
        }

        List<WorkOrder> workOrders = workOrderMapper.selectByPlanId(planId);
        if (workOrders == null || workOrders.isEmpty()) {
            return;
        }

        // 检查是否存在任一工单状态为"执行中"(3)
        boolean hasInProgress = false;
        boolean allClosed = true;

        for (WorkOrder wo : workOrders) {
            if (wo.getStatus() == Constants.WO_STATUS_IN_PROGRESS) {
                hasInProgress = true;
            }
            if (wo.getStatus() != Constants.WO_STATUS_CLOSED) {
                allClosed = false;
            }
        }

        if (allClosed) {
            // 所有工单已关闭 → 计划状态更新为"已完成"(2)
            productionPlanMapper.updateStatus(planId, Constants.PLAN_STATUS_COMPLETED);
        } else if (hasInProgress) {
            // 存在执行中的工单 → 计划状态更新为"执行中"(1)
            productionPlanMapper.updateStatus(planId, Constants.PLAN_STATUS_IN_PROGRESS);
        }
    }
}
