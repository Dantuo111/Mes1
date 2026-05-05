package com.app.entity;

import lombok.Data;
import java.util.Date;

@Data
public class WorkOrder {
    private Long id;
    private Long planId;
    private String productName;
    private Integer quantity;
    private Long assigneeId;
    private String processRequirement;
    private Date deliveryDate;
    private Integer status;  // 0待派发 1待接收 2待开工 3执行中 4已完工 5已关闭
    private Integer actualQuantity;
    private Date completeTime;
    private Date createTime;
    private Date updateTime;
    // 非数据库字段
    private String assigneeName;      // 负责人姓名 (from JOIN)
    private String planProductName;   // 关联计划的产品名称 (from JOIN)
}
