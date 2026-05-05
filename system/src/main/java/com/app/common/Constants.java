package com.app.common;

/**
 * 系统常量
 */
public class Constants {

    // 用户角色
    public static final int ROLE_EMPLOYEE = 0;  // 普通员工
    public static final int ROLE_ADMIN = 1;     // 管理员

    // 用户状态
    public static final int STATUS_DISABLED = 0;  // 禁用
    public static final int STATUS_ENABLED = 1;   // 启用

    // 生产计划状态
    public static final int PLAN_STATUS_PENDING = 0;      // 待执行
    public static final int PLAN_STATUS_IN_PROGRESS = 1;  // 执行中
    public static final int PLAN_STATUS_COMPLETED = 2;    // 已完成

    // 生产计划优先级
    public static final int PRIORITY_LOW = 0;     // 低
    public static final int PRIORITY_MEDIUM = 1;  // 中
    public static final int PRIORITY_HIGH = 2;    // 高

    // 工单状态
    public static final int WO_STATUS_PENDING_DISPATCH = 0;  // 待派发
    public static final int WO_STATUS_PENDING_RECEIVE = 1;   // 待接收
    public static final int WO_STATUS_PENDING_START = 2;     // 待开工
    public static final int WO_STATUS_IN_PROGRESS = 3;       // 执行中
    public static final int WO_STATUS_COMPLETED = 4;         // 已完工
    public static final int WO_STATUS_CLOSED = 5;            // 已关闭

    // 物料出入库类型
    public static final int MATERIAL_INBOUND = 0;   // 入库
    public static final int MATERIAL_OUTBOUND = 1;  // 出库
}
