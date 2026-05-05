package com.app.entity;

import lombok.Data;
import java.util.Date;

@Data
public class MaterialRequest {
    private Long id;
    private Long userId;
    private Long materialId;
    private Integer quantity;
    private String reason;
    private Integer status;  // 0待审批 1已批准 2已拒绝
    private String reply;
    private Date createTime;
    private Date updateTime;
    // 非数据库字段
    private String userName;
    private String materialName;
    private String materialCode;
}
