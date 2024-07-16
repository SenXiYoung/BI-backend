package com.yupi.springbootinit.bizmq;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.rabbitmq.client.Channel;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.constant.BiMqConstant;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.manager.AiManager;
import com.yupi.springbootinit.mapper.ChartMapper;
import com.yupi.springbootinit.model.entity.Chart;
import com.yupi.springbootinit.model.vo.BiResponse;
import com.yupi.springbootinit.service.ChartService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.annotation.RabbitListeners;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.Map;


@Component
@Slf4j
public class BiMessageComsumer {

    @Resource
    ChartService chartService;

    @Resource
    AiManager aiManager;

    @Resource
    ChartMapper chartMapper;


    @RabbitListener(queues = {BiMqConstant.BI_QUEUE_NAME},ackMode = "MANUAL")
    public void receiveMessage(String message , Channel channel , @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag){
        log.info("receiveMessage message = {}",message);
        if(StringUtils.isBlank(message)){
            try {
                channel.basicNack(deliveryTag,false,false);
            } catch (IOException e) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR,"消息为空");
            }
        }
        long chartId = Long.parseLong(message);
        Chart chart = chartService.getById(chartId);
        if(chart == null){
            try {
                channel.basicNack(deliveryTag,false,false);
            } catch (IOException e) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR,"图表为空");
            }
        }
        Chart updateChart = new Chart();
        updateChart.setStatus("running");
        updateChart.setId(chartId);
        boolean updateFResult = chartService.updateById(updateChart);
        if(!updateFResult){
            try {
                channel.basicNack(deliveryTag,false,false);
            } catch (IOException e) {
                handleChartUpdateError(updateChart.getId(),"更新图表执行中状态失败");
                throw new BusinessException(ErrorCode.SYSTEM_ERROR,"更新图表执行中状态失败");
            }
        }

        String userInput = userInput(chart);
        //调用AI接口
        String result = aiManager.doChat(1810587693997817857L,userInput);
        String[] splits = result.split("【【【【【");
        if(splits.length<3){
            try {
                channel.basicNack(deliveryTag,false,false);
            } catch (IOException e) {
                handleChartUpdateError(chart.getId(),"AI 生成错误");
                throw new BusinessException(ErrorCode.SYSTEM_ERROR,"AI 生成错误");
            }
        }
        String genChart = splits[1].trim();
        String genResult = splits[2].trim();
        Chart updateChartResult = new Chart();
        updateChartResult.setId(chart.getId());
        updateChartResult.setGenResult(genResult);
        updateChartResult.setGenChart(genChart);
        boolean updateSResult = chartService.updateById(updateChartResult);
        if(!updateSResult){
            try {
                channel.basicNack(deliveryTag,false,false);
            } catch (IOException e) {
                handleChartUpdateError(chart.getId(),"更新图标成功状态失败");
                throw new BusinessException(ErrorCode.SYSTEM_ERROR,"更新图标成功状态失败");
            }
        }
        try {
            channel.basicAck(deliveryTag,false);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"消息确认失败");
        }
    }


    private String userInput(Chart chart){
        String goal = chart.getGoal();
        String chartType = chart.getChartType();
        String chartDataId = chart.getChartDataId();
        StringBuilder chartName = new StringBuilder();
        //从表中获取数据 转化为CSV格式
        chartName.append("SELECT * FROM");
        chartName.append(" chart_").append(chartDataId);
        StringBuilder csvData = new StringBuilder();
        List<Map<String, Object>> mapList = chartMapper.queryChartData(chartName.toString());
        if(mapList == null){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"获取图表数据失败");
        }
        Map<String,Object> headerList = mapList.get(0);
        for(String header : headerList.keySet()){
            csvData.append(header).append(",");
        }
        csvData.append("\n");
        for(Map<String, Object> chartDataMap : mapList) {
            csvData.append(chartDataMap.values()).append(",").append("\n");
        }
        // 将所要发给ai的语句拼接起来
        StringBuilder userInput = new StringBuilder();
        // 目标
        userInput.append("分析需求：");
        String userGoal = goal;
        if(StringUtils.isNotBlank(chartType)){
            userGoal += ",请使用" + chartType;
        }
        userInput.append(userGoal).append("\n");
        //分析数据
        userInput.append("原始数据：");
        userInput.append(csvData).append("\n");
        return userInput.toString();
    }

    private void handleChartUpdateError(long chartId, String execMessage) {
        Chart updateChartResult = new Chart();
        updateChartResult.setId(chartId);
        updateChartResult.setStatus("failed");
        updateChartResult.setExecMessage("execMessage");
        boolean updateResult = chartService.updateById(updateChartResult);
        if (!updateResult) {
            log.error("更新图表失败状态失败" + chartId + "," + execMessage);
        }
    }
}
