package com.app.controller;

import com.app.common.Constants;
import com.app.common.Result;
import com.app.entity.MaterialRequest;
import com.app.mapper.MaterialRequestMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/materialRequest")
public class MaterialRequestController {

    @Autowired
    private MaterialRequestMapper materialRequestMapper;

    /** 申领列表 - 员工看自己的，管理员看全部 */
    @GetMapping("/list")
    public Result list(@RequestParam(required = false) Integer status,
                       @RequestParam(defaultValue = "1") Integer pageNum,
                       @RequestParam(defaultValue = "10") Integer pageSize,
                       HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Integer role = (Integer) request.getAttribute("role");
        Long queryUserId = (role != null && role == Constants.ROLE_ADMIN) ? null : userId;

        List<MaterialRequest> all = materialRequestMapper.selectList(queryUserId, status);
        int total = all.size();
        int from = (pageNum - 1) * pageSize;
        int to = Math.min(from + pageSize, total);
        List<MaterialRequest> page = from >= total ? new ArrayList<>() : all.subList(from, to);

        Map<String, Object> data = new HashMap<>();
        data.put("list", page);
        data.put("total", total);
        data.put("pageNum", pageNum);
        data.put("pageSize", pageSize);
        return Result.success(data);
    }

    /** 员工提交申领 */
    @PostMapping("/save")
    public Result save(@RequestBody MaterialRequest req, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        req.setUserId(userId);
        materialRequestMapper.insert(req);
        return Result.success();
    }

    /** 管理员审批 */
    @PostMapping("/approve")
    public Result approve(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        Integer status = Integer.valueOf(params.get("status").toString());
        String reply = params.get("reply") != null ? params.get("reply").toString() : "";
        materialRequestMapper.updateStatus(id, status, reply);
        return Result.success();
    }
}
