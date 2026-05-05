package com.app.service;

import com.app.common.Constants;
import com.app.common.Result;
import com.app.entity.User;
import com.app.mapper.UserMapper;
import com.app.util.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    /**
     * 用户注册
     */
    public Result register(User user) {
        // 校验用户名和密码不能为空
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            return Result.error("用户名不能为空");
        }
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            return Result.error("密码不能为空");
        }

        // 校验用户名唯一性
        User existingUser = userMapper.selectByUsername(user.getUsername());
        if (existingUser != null) {
            return Result.error("用户名已存在");
        }

        // 设置默认角色和状态
        user.setRole(Constants.ROLE_EMPLOYEE);
        user.setStatus(Constants.STATUS_ENABLED);

        userMapper.insert(user);
        return Result.success();
    }

    /**
     * 用户登录
     */
    public Result login(String username, String password) {
        // 根据用户名查找用户
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            return Result.error("用户名或密码错误");
        }

        // 验证密码（明文比较）
        if (!user.getPassword().equals(password)) {
            return Result.error("用户名或密码错误");
        }

        // 检查账户状态
        if (user.getStatus() == Constants.STATUS_DISABLED) {
            return Result.error("账户已被冻结");
        }

        // 生成JWT令牌
        String token = JwtUtils.generateToken(user.getId(), user.getRole());

        // 返回前清除密码
        user.setPassword(null);

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("user", user);

        return Result.success(data);
    }

    /**
     * 退出登录（JWT无状态，服务端无需操作）
     */
    public void logout() {
        // JWT是无状态的，服务端不需要额外操作
        // 客户端删除本地存储的token即可
    }

    /**
     * 获取用户列表（分页，支持筛选）
     */
    public Result getUserList(String keyword, Integer role, Integer status, Integer pageNum, Integer pageSize) {
        // 获取符合条件的全部用户列表
        List<User> allUsers = userMapper.selectList(keyword, role, status);
        int total = allUsers.size();

        // 手动分页
        int fromIndex = (pageNum - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);

        List<User> pageList;
        if (fromIndex >= total) {
            pageList = new ArrayList<>();
        } else {
            pageList = allUsers.subList(fromIndex, toIndex);
        }

        // 清除密码字段
        for (User user : pageList) {
            user.setPassword(null);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("list", pageList);
        data.put("total", total);
        data.put("pageNum", pageNum);
        data.put("pageSize", pageSize);

        return Result.success(data);
    }

    /**
     * 新增或编辑用户
     */
    public Result saveUser(User user) {
        if (user.getId() == null) {
            // 新增用户：校验用户名唯一性
            User existingUser = userMapper.selectByUsername(user.getUsername());
            if (existingUser != null) {
                return Result.error("用户名已存在");
            }
            // 设置默认值
            if (user.getRole() == null) {
                user.setRole(Constants.ROLE_EMPLOYEE);
            }
            if (user.getStatus() == null) {
                user.setStatus(Constants.STATUS_ENABLED);
            }
            if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
                user.setPassword("123456");
            }
            userMapper.insert(user);
        } else {
            // 编辑用户
            userMapper.updateById(user);
        }
        return Result.success();
    }

    /**
     * 删除用户
     */
    public Result deleteUser(Long id) {
        userMapper.deleteById(id);
        return Result.success();
    }

    /**
     * 启用/禁用用户
     */
    public Result updateUserStatus(Long id, Integer status) {
        userMapper.updateStatus(id, status);
        return Result.success();
    }

    /**
     * 获取当前用户信息（排除密码字段）
     */
    public Result getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        user.setPassword(null);
        return Result.success(user);
    }

    /**
     * 更新个人信息（仅允许修改昵称、手机号、头像）
     */
    public Result updateUserInfo(Long userId, User user) {
        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setNickname(user.getNickname());
        updateUser.setPhone(user.getPhone());
        updateUser.setAvatar(user.getAvatar());
        userMapper.updateById(updateUser);
        return Result.success();
    }

    /**
     * 修改密码（旧密码验证）
     */
    public Result changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        // 验证旧密码（明文比较）
        if (!user.getPassword().equals(oldPassword)) {
            return Result.error("原密码错误");
        }
        userMapper.updatePassword(userId, newPassword);
        return Result.success();
    }

}
