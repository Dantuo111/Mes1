package com.app.entity;

import lombok.Data;
import java.util.Date;

@Data
public class ProductionPlan {
    private Long id;
    private String productName;
    private Integer plannedQuantity;
    private Date startTime;
    private Date endTime;
    private Integer priority;  // 0低 1中 2高
    private Integer status;    // 0待执行 1执行中 2已完成
    private Date createTime;
    private Date updateTime;
}
