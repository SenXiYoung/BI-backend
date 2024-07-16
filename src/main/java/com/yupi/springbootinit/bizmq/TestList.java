package com.yupi.springbootinit.bizmq;


import com.yupi.springbootinit.mapper.ChartMapper;
import com.yupi.springbootinit.service.ChartService;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Component
public class TestList {

    @Resource
    private ChartService chartService;

    @Resource
    private ChartMapper chartMapper;

    public void listTest(){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("SELECT * FROM");
        stringBuilder.append(" chart_06aea84d1825401d910c16dc790ebc6c");
        StringBuilder stringBuilder1 = new StringBuilder();
        List<Map<String, Object>> mapList = chartMapper.queryChartData(stringBuilder.toString());
        Map<String,Object> headerList = mapList.get(0);
        for(String header : headerList.keySet()){
            stringBuilder1.append(header).append(",");
        }
        stringBuilder1.append("\n");
        for(Map<String, Object> chartDataMap : mapList) {
            stringBuilder1.append(chartDataMap.values()).append(",").append("\n");
        }
        System.out.println(stringBuilder1);
    }
}
