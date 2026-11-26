package com.ape.smarthedge_risk_hub.controller;

import com.ape.smarthedge_risk_hub.common.ReParam;
import com.ape.smarthedge_risk_hub.common.Result;
import com.ape.smarthedge_risk_hub.pojo.SpotProduct;
import com.ape.smarthedge_risk_hub.service.ISpotProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/spotproduct")
public class SpotProductController {
    @Autowired
    private ISpotProductService spotProductService;
    @PostMapping("/save")
    public Result save(@RequestBody SpotProduct spotProduct){
        try {
            int i = spotProductService.insertSpotProduct(spotProduct);
            if (i > 0){
                return Result.success(spotProduct);
            } else {
                return Result.fail(500,"添加失败");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @GetMapping("/list")
    public Result<List<SpotProduct>> list(@RequestParam(value = "keyword", required = false) String keyword){
        try {
            List<SpotProduct> result;
            if (keyword != null && !keyword.trim().isEmpty()) {
                result = spotProductService.findSpotProductByKeyword(keyword.trim());
            } else {
                result = spotProductService.findAllSpotProductAndOriginDict();
            }
            return Result.success(result);
        } catch (Exception e) {
            return Result.fail(500, "查询失败: " + e.getMessage());
        }
    }
    @GetMapping("/page")
    public Result<List<SpotProduct>> selectPage(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "10") Integer size) {
        ReParam param = new ReParam();
        param.setKeyword(keyword);
        param.setPage(page);
        param.setSize(size);
        return spotProductService.selectPage(param);
    }
}
