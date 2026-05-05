package com.app.controller;

import com.app.common.Constants;
import com.app.common.Result;
import com.app.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ProductionPlanMapper productionPlanMapper;

    @Autowired
    private WorkOrderMapper workOrderMapper;

    @Autowired
    private MaterialMapper materialMapper;

    /**
     * 总览数据（用户数、计划数、工单数、物料数）
     */
    @GetMapping("/overview")
    public Result overview() {
        Map<String, Object> data = new HashMap<>();
        data.put("userCount", userMapper.countAll());
        data.put("planCount", productionPlanMapper.countAll());
        data.put("workOrderCount", workOrderMapper.countAll());
        data.put("materialCount", materialMapper.countAll());
        return Result.success(data);
    }

    /**
     * 工单状态分布
     */
    @GetMapping("/workOrderStatus")
    public Result workOrderStatus() {
        Map<String, Object> data = new HashMap<>();
        data.put("pendingDispatch", workOrderMapper.countByStatus(Constants.WO_STATUS_PENDING_DISPATCH));
        data.put("pendingReceive", workOrderMapper.countByStatus(Constants.WO_STATUS_PENDING_RECEIVE));
        data.put("pendingStart", workOrderMapper.countByStatus(Constants.WO_STATUS_PENDING_START));
        data.put("inProgress", workOrderMapper.countByStatus(Constants.WO_STATUS_IN_PROGRESS));
        data.put("completed", workOrderMapper.countByStatus(Constants.WO_STATUS_COMPLETED));
        data.put("closed", workOrderMapper.countByStatus(Constants.WO_STATUS_CLOSED));
        return Result.success(data);
    }

    /**
     * 生产计划完成趋势
     */
    @GetMapping("/planTrend")
    public Result planTrend() {
        Map<String, Object> data = new HashMap<>();
        data.put("pending", productionPlanMapper.countAll() - countPlanByStatus(Constants.PLAN_STATUS_IN_PROGRESS) - countPlanByStatus(Constants.PLAN_STATUS_COMPLETED));
        data.put("inProgress", countPlanByStatus(Constants.PLAN_STATUS_IN_PROGRESS));
        data.put("completed", countPlanByStatus(Constants.PLAN_STATUS_COMPLETED));
        return Result.success(data);
    }

    private int countPlanByStatus(int status) {
        // 使用selectList并过滤来统计
        return productionPlanMapper.selectList(null, status).size();
    }

    /**
     * 库存预警数量
     */
    @GetMapping("/warningCount")
    public Result warningCount() {
        int count = materialMapper.selectWarningList().size();
        Map<String, Object> data = new HashMap<>();
        data.put("warningCount", count);
        return Result.success(data);
    }
}
