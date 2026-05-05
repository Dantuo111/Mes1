package com.app.controller;

import com.app.common.Result;
import com.app.entity.ProductionPlan;
import com.app.service.ProductionPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/productionPlan")
public class ProductionPlanController {

    @Autowired
    private ProductionPlanService productionPlanService;

    /**
     * 生产计划列表（分页，支持筛选）
     */
    @GetMapping("/list")
    public Result list(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) Integer status,
                       @RequestParam(defaultValue = "1") Integer pageNum,
                       @RequestParam(defaultValue = "10") Integer pageSize) {
        return productionPlanService.getPlanList(keyword, status, pageNum, pageSize);
    }

    /**
     * 生产计划详情
     */
    @GetMapping("/detail/{id}")
    public Result detail(@PathVariable Long id) {
        return productionPlanService.getPlanDetail(id);
    }

    /**
     * 新增/编辑生产计划
     */
    @PostMapping("/save")
    public Result save(@RequestBody ProductionPlan plan) {
        return productionPlanService.savePlan(plan);
    }

    /**
     * 删除生产计划
     */
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Long id) {
        return productionPlanService.deletePlan(id);
    }
}
