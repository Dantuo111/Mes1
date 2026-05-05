package com.app.entity;

import lombok.Data;
import java.util.Date;

@Data
public class Material {
    private Long id;
    private String code;
    private String name;
    private String spec;
    private String unit;
    private Integer stockQuantity;
    private Integer safetyStock;
    private Date createTime;
    private Date updateTime;
}
