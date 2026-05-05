package com.app.entity;

import lombok.Data;
import java.util.Date;

@Data
public class QualityInspection {
    private Long id;
    private Long workOrderId;
    private Date inspectionDate;
    private String inspector;
    private Integer inspectionQuantity;
    private Integer qualifiedQuantity;
    private Integer unqualifiedQuantity;
    private String unqualifiedReason;
    private Double passRate;
    private Date createTime;
    private Date updateTime;
    // 非数据库字段
    private String workOrderProductName;  // 关联工单的产品名称
}
