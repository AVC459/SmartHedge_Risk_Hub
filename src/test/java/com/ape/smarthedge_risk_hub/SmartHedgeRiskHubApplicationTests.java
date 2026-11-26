package com.ape.smarthedge_risk_hub;

import com.ape.smarthedge_risk_hub.common.ReParam;
import com.ape.smarthedge_risk_hub.common.Result;
import com.ape.smarthedge_risk_hub.pojo.SpotProduct;
import com.ape.smarthedge_risk_hub.service.ISpotProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class SmartHedgeRiskHubApplicationTests {
    @Autowired
    private ISpotProductService spotProductService;
    @Test
    public void testSelectPage() {
        // 测试分页查询功能
        ReParam param = new ReParam();
        param.setKeyword(null); // 查询所有
        Result<List<SpotProduct>> listResult = spotProductService.selectPage(param);
        // 获取并打印实际的数据
        List<SpotProduct> dataList = listResult.getData();
        for(SpotProduct product : dataList) {
            System.out.println(product);
        }

    }


}
