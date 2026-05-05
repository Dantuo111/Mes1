package com.app.controller;

import com.app.common.Constants;
import com.app.common.Result;
import com.app.entity.QualityFeedback;
import com.app.mapper.QualityFeedbackMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/qualityFeedback")
public class QualityFeedbackController {

    @Autowired
    private QualityFeedbackMapper qualityFeedbackMapper;

    /** 反馈列表 - 员工看自己的，管理员看全部 */
    @GetMapping("/list")
    public Result list(@RequestParam(required = false) Integer status,
                       @RequestParam(defaultValue = "1") Integer pageNum,
                       @RequestParam(defaultValue = "10") Integer pageSize,
                       HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Integer role = (Integer) request.getAttribute("role");
        Long queryUserId = (role != null && role == Constants.ROLE_ADMIN) ? null : userId;

        List<QualityFeedback> all = qualityFeedbackMapper.selectList(queryUserId, status);
        int total = all.size();
        int from = (pageNum - 1) * pageSize;
        int to = Math.min(from + pageSize, total);
        List<QualityFeedback> page = from >= total ? new ArrayList<>() : all.subList(from, to);

        Map<String, Object> data = new HashMap<>();
        data.put("list", page);
        data.put("total", total);
        data.put("pageNum", pageNum);
        data.put("pageSize", pageSize);
        return Result.success(data);
    }

    /** 员工提交反馈 */
    @PostMapping("/save")
    public Result save(@RequestBody QualityFeedback fb, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        fb.setUserId(userId);
        qualityFeedbackMapper.insert(fb);
        return Result.success();
    }

    /** 管理员处理反馈 */
    @PostMapping("/handle")
    public Result handle(@RequestBody QualityFeedback fb) {
        qualityFeedbackMapper.updateById(fb);
        return Result.success();
    }
}
