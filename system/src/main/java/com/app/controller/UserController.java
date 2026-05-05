package com.app.controller;

import com.app.common.Result;
import com.app.entity.User;
import com.app.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    // ==================== 公开接口（无需认证） ====================

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result register(@RequestBody User user) {
        return userService.register(user);
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result login(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");
        return userService.login(username, password);
    }

    // ==================== 需要登录的接口 ====================

    /**
     * 退出登录
     */
    @PostMapping("/logout")
    public Result logout() {
        userService.logout();
        return Result.success();
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/info")
    public Result info(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return userService.getUserInfo(userId);
    }

    /**
     * 更新个人信息
     */
    @PostMapping("/update")
    public Result update(@RequestBody User user, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return userService.updateUserInfo(userId, user);
    }

    /**
     * 修改密码
     */
    @PostMapping("/changePassword")
    public Result changePassword(@RequestBody Map<String, String> params, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");
        return userService.changePassword(userId, oldPassword, newPassword);
    }

    // ==================== 管理员接口 ====================

    /**
     * 用户列表（分页，支持筛选）
     */
    @GetMapping("/list")
    public Result list(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) Integer role,
                       @RequestParam(required = false) Integer status,
                       @RequestParam(defaultValue = "1") Integer pageNum,
                       @RequestParam(defaultValue = "10") Integer pageSize) {
        return userService.getUserList(keyword, role, status, pageNum, pageSize);
    }

    /**
     * 新增/编辑用户
     */
    @PostMapping("/save")
    public Result save(@RequestBody User user) {
        return userService.saveUser(user);
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Long id) {
        return userService.deleteUser(id);
    }

    /**
     * 启用/禁用用户
     */
    @PostMapping("/updateStatus")
    public Result updateStatus(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        Integer status = Integer.valueOf(params.get("status").toString());
        return userService.updateUserStatus(id, status);
    }
}
