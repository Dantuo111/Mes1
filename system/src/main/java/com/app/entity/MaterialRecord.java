package com.app.entity;

import lombok.Data;
import java.util.Date;

@Data
public class MaterialRecord {
    private Long id;
    private Long materialId;
    private Integer type;  // 0入库 1出库
    private Integer quantity;
    private Integer beforeQuantity;
    private Integer afterQuantity;
    private Date operateTime;
    private Date createTime;
    // 非数据库字段
    private String materialName;  // 物料名称 (from JOIN)
    private String materialCode;  // 物料编码 (from JOIN)
}
