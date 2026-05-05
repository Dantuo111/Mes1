package com.app.entity;

import lombok.Data;
import java.util.Date;

@Data
public class QualityFeedback {
    private Long id;
    private Long userId;
    private Long workOrderId;
    private String title;
    private String description;
    private Integer severity;  // 0低 1中 2高
    private Integer status;    // 0待处理 1处理中 2已解决 3已关闭
    private String reply;
    private Date createTime;
    private Date updateTime;
    // 非数据库字段
    private String userName;
    private String workOrderProductName;
}
