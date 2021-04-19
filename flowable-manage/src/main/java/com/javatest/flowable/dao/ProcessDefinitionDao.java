package com.javatest.flowable.dao;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.javatest.flowable.entity.vo.ProcessDefinitionQueryVo;
import com.javatest.flowable.entity.vo.ProcessDefinitionVo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author: Javen
 * @date: 2021/4/8 16:29
 * @description: 管理部署后生成的流程定义
 */
@Repository
public interface ProcessDefinitionDao {

    /**
     * 条件查询流程定义列表
     */
    Page<ProcessDefinitionVo> getPagerModel(@Param("params") ProcessDefinitionQueryVo params);

    ProcessDefinitionVo getById(String processDefinitionId);
}
