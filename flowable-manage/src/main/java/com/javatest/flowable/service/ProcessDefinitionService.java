package com.javatest.flowable.service;

import com.github.pagehelper.PageInfo;
import com.javatest.flowable.common.response.Result;
import com.javatest.flowable.entity.vo.ProcessDefinitionQueryVo;
import com.javatest.flowable.entity.vo.ProcessDefinitionVo;

/**
 * @author: Javen
 * @date: 2021/4/8 16:18
 * @description: 管理部署后生成的流程定义
 */
public interface ProcessDefinitionService {

    Result suspendOrActivateProcessDefinitionById(String id, int suspensionState);

    PageInfo<ProcessDefinitionVo> getPagerModel(ProcessDefinitionQueryVo params);

    ProcessDefinitionVo getById(String id);
}
