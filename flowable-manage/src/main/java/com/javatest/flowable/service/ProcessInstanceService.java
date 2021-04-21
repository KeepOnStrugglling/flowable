package com.javatest.flowable.service;

import com.github.pagehelper.PageInfo;
import com.javatest.flowable.common.response.Result;
import com.javatest.flowable.entity.vo.*;
import org.flowable.engine.runtime.ProcessInstance;

/**
 * @author: Javen
 * @date: 2021/4/19 16:28
 * @description: 管理部署后生成的流程实例
 */
public interface ProcessInstanceService {

    /**
     * 启动流程
     */
    public Result<ProcessInstance> startProcessInstanceByKey(StartProcessInstanceVo startProcessInstanceVo);

    /**
     * 分页查询流程定义列表
     */
    PageInfo<ProcessInstanceVo> getPagerModel(ProcessInstanceQueryVo params);

    /**
     * 查询我发起的流程实例
     */
    PageInfo<ProcessInstanceVo> getMyProcessInstances(ProcessInstanceQueryVo params);

    /**
     * 获取流程图图片
     * @param processInstanceId 流程实例id
     */
    byte[] createImage(String processInstanceId);

    /**
     * 删除流程实例
     */
    Result deleteProcessInstanceById(String processInstanceId);

    /**
     * 激活或者挂起流程定义
     * @param id            需要操作的流程定义id
     * @param suspensionState   进行的操作 2-激活，1-挂起
     */
    Result suspendOrActivateProcessInstanceById(String id, int suspensionState);

    /**
     * 终止
     */
    Result stopProcessInstanceById(EndProcessVo params);

    /**
     * 撤回流程
     */
    Result revokeProcess(RevokeProcessVo revokeVo);
}
