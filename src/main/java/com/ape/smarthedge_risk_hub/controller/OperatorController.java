package com.ape.smarthedge_risk_hub.controller;

import com.ape.smarthedge_risk_hub.common.Result;
import com.ape.smarthedge_risk_hub.pojo.Operator;
import com.ape.smarthedge_risk_hub.service.IOperatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/operator")
public class OperatorController {
    @Autowired
    private IOperatorService operatorService;

    /**
     * 查询所有经营者
     */
    @GetMapping("/list")
    public Result<List<Operator>> list() {
        try {
            List<Operator> operators = operatorService.findAllOperators();
            return Result.success(operators);
        } catch (Exception e) {
            return Result.fail(500, "查询失败: " + e.getMessage());
        }
    }

}
