package com.app.entity;

import lombok.Data;
import java.util.Date;

@Data
public class User {
    private Long id;
    private String username;
    private String password;
    private String nickname;
    private String phone;
    private String avatar;
    private Integer role;    // 0普通员工 1管理员
    private Integer status;  // 0禁用 1启用
    private Date createTime;
    private Date updateTime;
}
