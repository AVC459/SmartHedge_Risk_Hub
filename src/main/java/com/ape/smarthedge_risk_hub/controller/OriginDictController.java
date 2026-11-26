package com.ape.smarthedge_risk_hub.controller;

import com.ape.smarthedge_risk_hub.common.Result;
import com.ape.smarthedge_risk_hub.pojo.OriginDict;
import com.ape.smarthedge_risk_hub.service.IOriginDictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/origindict")
public class OriginDictController {

    @Autowired
    private IOriginDictService originDictService;

    /**
     * 查询全部产地
     */
    @GetMapping("/list")
    public Result<List<OriginDict>> list() {
        try {
            return Result.success(originDictService.findAllOriginDict());
        } catch (Exception e) {
            return Result.fail(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 分页查询产地，支持按产地名称/简称模糊查询
     */
    @GetMapping("/page")
    public Result<List<OriginDict>> selectPage(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "10") Integer size) {
        try {
            List<OriginDict> all = originDictService.findAllOriginDict();
            List<OriginDict> filtered = new ArrayList<>();
            if (keyword != null && !keyword.trim().isEmpty()) {
                String kw = keyword.trim();
                for (OriginDict o : all) {
                    boolean hit = (o.getOriginName() != null && o.getOriginName().contains(kw))
                            || (o.getOriginShortName() != null && o.getOriginShortName().contains(kw));
                    if (hit) {
                        filtered.add(o);
                    }
                }
            } else {
                filtered.addAll(all);
            }
            int start = (page - 1) * size;
            int end = Math.min(start + size, filtered.size());
            List<OriginDict> rows = start >= filtered.size() ? new ArrayList<>() : filtered.subList(start, end);
            return Result.success(rows, filtered.size());
        } catch (Exception e) {
            return Result.fail(500, "查询失败: " + e.getMessage());
        }
    }

}
