package com.yupi.springbootinit.mapper;

import com.yupi.springbootinit.model.entity.Chart;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
* @author senxiyoung
* @description 针对表【chart(图表信息表)】的数据库操作Mapper
* @createDate 2024-07-03 15:19:18
* @Entity com.yupi.springbootinit.model.entity.Chart
*/
public interface ChartMapper extends BaseMapper<Chart> {

    List<Map<String ,Object>> queryChartData(String querySql);

    boolean createTable( @Param("createTableSql") String createTableSql);

    boolean insertData(@Param("insertDataSql") String insertDataSql);
}




